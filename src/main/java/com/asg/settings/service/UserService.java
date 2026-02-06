package com.asg.settings.service;

import com.asg.common.lib.dto.*;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.exception.AsgException;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.utility.ASGHelperUtils;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.common.lib.utility.ValidationUtil;
import com.asg.settings.dto.*;
import com.asg.settings.entity.*;
import com.asg.settings.entity.key.CompanyEntityKey;
import com.asg.settings.entity.key.UserRolesEntityKey;
import com.asg.settings.repository.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.asg.common.lib.dto.response.ApiResponse.*;

@Service
@Slf4j
public class UserService {

    public static final String IS_DELETED = "isDeleted";
    public static final String IS_UPDATED = "isUpdated";
    public static final String IS_CREATED = "isCreated";
    public static final String NO_CHANGE = "noChange";

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserRoleRepository userRoleRepository;

    @Autowired
    UsersCompanyRepository usersCompanyRepository;

    @Autowired
    LocationMasterRepository locationMasterRepository;

    @Autowired
    CompanyService companyService;

    @Autowired
    MenuRepository menuRepository;

    @Autowired
    RoleService roleService;

    @Autowired
    DataSource dataSource;

    @Autowired
    DocumentSearchService documentService;

    @Autowired
    LoggingService loggingService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DocumentDeleteService documentDeleteService;

    public UserResponse getUserDetailsByRolePoid(Long userRolePoid) {

        List<UserEntity> users = userRepository.fetchUserRoleByRolePoid(userRolePoid);
        UserResponse userResponse = new UserResponse();
        userResponse.setUsers(users);
        userResponse.setTotalRecords(users.size());
        userResponse.setUserRolePoid(userRolePoid);
        userResponse.setMessage("Success");
        userResponse.setStatus("200");
        return userResponse;

    }


    public User findByEmailAddress(String email) {
        try {
            Optional<User> userDetails = userRepository.findByActiveEmail(email.trim());
            return userDetails.orElse(null);
        } catch (Exception ex) {
            log.error("Exception while fetching user with email {}: {}", email, ex.getMessage(), ex);
            return null;
        }
    }

    public Map<String, Object> listUsers(String docId, FilterRequestDto request, Pageable pageable) {

        String operator = documentService.resolveOperator(request);
        String isDeleted = documentService.resolveIsDeleted(request);
        List<FilterDto> filters = documentService.resolveFilters(request);

        RawSearchResult raw = documentService.search(docId, filters, operator, pageable, isDeleted,
                "USER_NAME",   // label
                "USER_POID");    // value);

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    public UserDto getUserDetails(Long userPoid) {
        User user = userRepository.findById(userPoid)
                .orElseThrow(() -> new RuntimeException("User not found or user not active."));

        List<UserAuthRoleDto> roles = roleService.getUserAuthRoles(userPoid);

        // Sort roles by detRowId - create mutable copy first
        if (roles != null) {
            roles = new ArrayList<>(roles);
            roles.sort(Comparator.comparing(UserAuthRoleDto::getDetRowId, Comparator.nullsLast(Comparator.naturalOrder())));
        }

        List<UserCompanyDto> companies = companyService.getUsersCompanies(userPoid);
//        Country country = companyService.getCountryForCompany(user.getDefaultCompanyPoid());
        String countryCode = companyService.getCountryCodeForCompany(user.getDefaultCompanyPoid());

        CompanyDto defaultCompany = companyService.getCompany(user.getDefaultCompanyPoid());
        DetailsDto defaultCompanyDto = null;
        if (defaultCompany != null) {
            defaultCompanyDto = new DetailsDto(defaultCompany.getCompanyPoid(),
                    defaultCompany.getCompanyCode(), defaultCompany.getCompanyName(),
                    defaultCompany.getCompanyPoid(), defaultCompany.getCompanyName(), defaultCompany.getSeqNo());
        }

        LocationMasterEntity defaultLocationEntity = locationMasterRepository.findByLocationPoid(user.getDefaultLocationPoid());
        DetailsDto defaultLocationDto = null;
        if (defaultLocationEntity != null) {
            defaultLocationDto = new DetailsDto(defaultLocationEntity.getLocationPoid(), defaultLocationEntity.getLocationCode(),
                    defaultLocationEntity.getLocationName(), defaultLocationEntity.getLocationPoid(), defaultLocationEntity.getLocationName(), defaultLocationEntity.getSeqNo());
        }

        return new UserDto(
                user.getUserId(),
                user.getUserPoid(),
                user.getUserName(),
                user.getUserName(), // label
                user.getUserPoid(), // value
                user.getGroupPoid(),
                defaultCompanyDto,
                user.getCreatedDate(),
                roles,
                companies,
//                country != null ? country.getCountryCode() : null,
                countryCode,
                user.getUserMobile(),
                user.getEmail(),
                defaultLocationDto,
                user.getExpiryDate(),
                user.getSeqno(),
                user.getActive(),
                user.getAuthenticationMethod() != null && user.getAuthenticationMethod().equalsIgnoreCase("Y") ? "Microsoft" : "General",
                user.getUserLocked(),
                user.getUserLockedReason(),
                user.getResetPasswordNextLogin(),
                user.getCreatedBy(),
                user.getCreatedDate() != null ? new Date(user.getCreatedDate().getTime()) : null,
                user.getLastModifiedBy(),
                user.getLastModifiedDate() != null ? Timestamp.valueOf(user.getLastModifiedDate()) : null
        );
    }

    public List<MenuItemDto> getUserMenu(Long userPoid) {
        List<Object[]> results = menuRepository.findMenuItemsByUserPoid(userPoid);

        List<MenuItemDto> menuItems = results.stream().map(row -> new MenuItemDto(
                (String) row[0], // MENU_ID
                (String) row[1], // MENU_NAME
                ((Number) row[2]).intValue(), // MENU_LEVEL
                (String) row[3], // MENU_GROUP
                (String) row[4], // TASKFLOW_URL
                (String) row[5], // USER_ID
                (String) row[6], // DOC_TYPE
                (String) row[7], // MODULE_ID
                row[8] != null ? row[8].toString() : null, // HIDE_IN_MAIN_MENU
                new ArrayList<>()
        )).collect(Collectors.toList());

        return buildMenuHierarchy(menuItems);
    }


    private List<MenuItemDto> buildMenuHierarchy(List<MenuItemDto> menuItems) {

        // Step 1: Group by level
        Map<Integer, List<MenuItemDto>> levels = menuItems.stream()
                .collect(Collectors.groupingBy(MenuItemDto::getMenuLevel));

        List<MenuItemDto> level0 = levels.getOrDefault(0, List.of());
        List<MenuItemDto> level1 = levels.getOrDefault(1, List.of());
        List<MenuItemDto> level2 = levels.getOrDefault(2, List.of());

        // Step 2: Map level 1 by menuGroup
        Map<String, List<MenuItemDto>> level1Map = level1.stream()
                .collect(Collectors.groupingBy(MenuItemDto::getMenuGroup));

        Map<String, List<MenuItemDto>> level2Map = level2.stream()
                .collect(Collectors.groupingBy(MenuItemDto::getMenuGroup));

        // Step 3: Attach level 2 to level 1
        for (MenuItemDto l1 : level1) {
            List<MenuItemDto> children = level2Map.getOrDefault(l1.getMenuId(), List.of());
            l1.getChildren().addAll(children);
        }

        // Step 4: Attach level 1 to level 0
        for (MenuItemDto l0 : level0) {
            List<MenuItemDto> children = level1Map.getOrDefault(l0.getMenuId(), List.of());
            l0.getChildren().addAll(children);
        }

        return level0;
    }

    @Transactional
    public String createUser(CreateUserRequest userDetails) {


        if (userDetails.getSeqNo() != null && userDetails.getSeqNo().toString().length() > 5) {
            throw new InputMismatchException("SeqNo should not be more than 5 digits");
        }
        Boolean isUserIdTaken = userRepository.existsByUserIdIgnoreCase(userDetails.getUserId());
        if (isUserIdTaken) {
            throw new InputMismatchException("User Id already mapped to another user");
        }
        Boolean isUserNameTaken = isUserExistsByUserNameAndUserPoid(userDetails.getUserName(), null);
        if (isUserNameTaken) {
            throw new InputMismatchException("User Name already mapped to another user");
        }
        User user = new User();
        userDetails.setUserPoid(null);

        try {
            // Save user & assign POID
            insertUserToTable(userDetails, user);

            // Create summary log
            String docId = UserContext.getDocumentId();
            loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, docId, user.getUserPoid().toString());

            return user.getUserPoid().toString();
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("GLOBAL_USERS_UK_USERID")) {
                throw new InputMismatchException("User ID '" + userDetails.getUserId() + "' already exists");
            }
            throw e;
        }
    }

    @Transactional
    public String updateUser(CreateUserRequest userDetails) {


        if (userDetails.getSeqNo() != null && userDetails.getSeqNo().toString().length() > 5) {
            throw new InputMismatchException("SeqNo should not be more than 5 digits");
        }
        Optional<User> optionalUser = userRepository.findByUserPoid(userDetails.getUserPoid());
        if (optionalUser.isEmpty()) {
            throw new AsgException("User not found for the user poid : " + userDetails.getUserPoid(), 400);
        }

        // Fetch existing user
        User user = optionalUser.get();

        // ---- Create copy for logging ----
        User oldUser = new User();
        BeanUtils.copyProperties(user, oldUser);

        if (!user.getUserId().equalsIgnoreCase(userDetails.getUserId())) {
            throw new InputMismatchException("User Id cannot be updated");
        }

        Boolean isUserNameTaken = isUserExistsByUserNameAndUserPoid(userDetails.getUserName(), userDetails.getUserPoid());
        if (isUserNameTaken) {
            throw new InputMismatchException("User Name already mapped to another user");
        }

        try {
            insertUserToTable(userDetails, user);
            loggingService.logChanges(oldUser, user, User.class, UserContext.getDocumentId(), user.getUserPoid().toString(),
                    LogDetailsEnum.MODIFIED, "USER_POID");
            return user.getUserPoid().toString();
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("GLOBAL_USERS_UK_USERID")) {
                throw new InputMismatchException("User ID '" + userDetails.getUserId() + "' already exists");
            }
            throw e;
        }
    }

    @Transactional
    void insertUserToTable(CreateUserRequest userDetails, User user) {
        List<UserCompanyDto> usersCompanyList = userDetails.getUserCompanies();
        List<UserCompanyDto> filteredCompanies = usersCompanyList.stream()
                .filter(company -> company.actionType().equalsIgnoreCase(NO_CHANGE)
                        || company.actionType().equalsIgnoreCase(IS_CREATED)
                        || company.actionType().equalsIgnoreCase(IS_UPDATED))
                .toList();

        if (filteredCompanies.isEmpty()) {
            throw new InputMismatchException("Atleast one company should be selected in the detail table...");
        }

        boolean matchFound = filteredCompanies.stream()
                .anyMatch(company -> Objects.equals(userDetails.getDefaultCompanyPoid(), company.companyId()));


        if (!matchFound) {
            throw new InputMismatchException(userDetails.getDefaultCompanyPoid() + " Default company should be available in the companies list");
        }

        if (StringUtils.isNotBlank(userDetails.getAuthenticationMethod()) && "Y".equalsIgnoreCase(userDetails.getAuthenticationMethod())) {
            boolean existsByEmail;
            if (userDetails.getUserPoid() != null) {
                existsByEmail = userRepository.existsByEmailIgnoreCaseAndAuthenticationMethodAndUserPoidNot(userDetails.getUserEmail(), userDetails.getAuthenticationMethod(), userDetails.getUserPoid());
            } else {
                existsByEmail = userRepository.existsByEmailIgnoreCaseAndAuthenticationMethod(userDetails.getUserEmail(), userDetails.getAuthenticationMethod());
            }

            if (existsByEmail) {
                throw new AsgException("Email ID is already assigned to another user. Please change Email ID or Authentication method", 409);
            }
        }

        user.setUserId(userDetails.getUserId());
        user.setUserName(userDetails.getUserName());
        user.setUserMobile(userDetails.getUserMobile());
        user.setUserLocked(userDetails.getUserLocked());
        user.setActive(userDetails.getActive());
        user.setEmail(userDetails.getUserEmail());
        user.setGroupPoid(1L);
        user.setDefaultCompanyPoid(userDetails.getDefaultCompanyPoid());
        user.setDefaultLocationPoid(userDetails.getDefaultLocationPoid());
        user.setSeqno(userDetails.getSeqNo());
        user.setAuthenticationMethod(userDetails.getAuthenticationMethod());
        user.setUserLocked(userDetails.getUserLocked());
        user.setUserLockedReason(userDetails.getUserLockedReason());
        user.setResetPasswordNextLogin(userDetails.getResetPwdNextLogin());
        user.setExpiryDate(userDetails.getExpiryDate());
       /* user.setCreatedDate(Timestamp.from(Instant.now()));
        Timestamp now = Timestamp.from(Instant.now());
        user = userRepository.saveAndFlush(user);*/


        Timestamp now = Timestamp.from(Instant.now());
        String currentUser = ASGHelperUtils.getCurrentUser();

        if (user.getUserPoid() == null) {
            // --- Creating new user ---
            user.setCreatedBy(currentUser);
            user.setCreatedDate(now);
            user.setLastModifiedBy(currentUser);
            user.setLastModifiedDate(now.toLocalDateTime());
        } else {
            // --- Updating existing user ---
            user.setLastModifiedBy(currentUser);
            user.setLastModifiedDate(now.toLocalDateTime());
        }
        boolean isNewUser = user.getUserPoid() == null;
        user = userRepository.saveAndFlush(user);
        //update the roles for the user
        User finalUser = user;

        if (null != userDetails.getUserRoles()) {
            userDetails.getUserRoles().forEach(role -> {
                if (IS_CREATED.equalsIgnoreCase(role.actionType())) {
                    addUserRole(role, finalUser);
                } else if (IS_UPDATED.equalsIgnoreCase(role.actionType())) {
                    updateUserRole(role, finalUser);
                } else if (IS_DELETED.equalsIgnoreCase(role.actionType())) {
                    deleteUserRole(role, finalUser);
                }
                // if NOCHANGE -> no change is required
            });
        }
        //----------------------


        //updating the companies for the users
        if (null != userDetails.getUserCompanies()) {
            userDetails.getUserCompanies().forEach(company -> {

                if (IS_CREATED.equalsIgnoreCase(company.actionType())) {
                    addUserCompany(company, finalUser);
                } else if (IS_UPDATED.equalsIgnoreCase(company.actionType())) {
                    updateUserCompany(company, finalUser);
                } else if (IS_DELETED.equalsIgnoreCase(company.actionType())) {
                    deleteUserCompany(company, finalUser);
                }

            });
        }
        //--------------------------------------------


        //TODO send email


    }

    private void addUserCompany(UserCompanyDto company, User finalUser) {
        UsersCompanyEntity companyPresent = usersCompanyRepository.findById_UserPoidAndId_CompanyPoid(finalUser.getUserPoid(), company.companyId());
        if (null == companyPresent) {
            UsersCompanyEntity userCompany = new UsersCompanyEntity();
            userCompany.setExpiryDate(company.expiryDate());
            CompanyEntityKey companyEntityKey = new CompanyEntityKey();
            companyEntityKey.setCompanyPoid(company.companyId());
            companyEntityKey.setUserPoid(finalUser.getUserPoid());
            companyEntityKey.setDetRowId(getNextDetRowIdForUserCompanies(finalUser.getUserPoid()));

            userCompany.setId(companyEntityKey);
            usersCompanyRepository.save(userCompany);
            String logDetail = String.format("Row Created on User Company with DetRowId %s ", userCompany.getId().getDetRowId());
            loggingService.createLogSummaryEntry(UserContext.getDocumentId(), finalUser.getUserPoid().toString(), logDetail);

        } else {
            throw new InputMismatchException("You are attempting to update the same company multiple times. Please review your selection. companyId ->" + company.companyId());
        }
    }

    private void deleteUserCompany(UserCompanyDto company, User finalUser) {
        UsersCompanyEntity companyPresent = usersCompanyRepository.findById_UserPoidAndId_CompanyPoid(finalUser.getUserPoid(), company.companyId());
        if (null != company) {
            usersCompanyRepository.deleteById_UserPoidAndId_CompanyPoid(finalUser.getUserPoid(), company.companyId());
            loggingService.logDelete(companyPresent, UserContext.getDocumentId(), finalUser.getUserPoid().toString());
        } else {
            throw new InputMismatchException("Cannot delete a company that  is not assigned to the user, companyId  -> " + company.companyId());
        }
    }

    private void updateUserCompany(UserCompanyDto company, User finalUser) {
        UsersCompanyEntity companyPresent = usersCompanyRepository.findById_UserPoidAndId_CompanyPoid(finalUser.getUserPoid(), company.companyId());
        if (null != company) {
            companyPresent.setExpiryDate(company.expiryDate());
            usersCompanyRepository.save(companyPresent);
        } else {
            throw new InputMismatchException("Cannot update a company that is not assigned to the user, companyId   -> " + company.companyId());
        }
    }

    private void addUserRole(UserRoleDto role, User finalUser) {
        UserRolesEntity rolePresent = userRoleRepository.getUserRolesEntitiesById_UserPoidAndUserRolePoid(finalUser.getUserPoid(), role.userRolePoId());

        if (null == rolePresent) {
            UserRolesEntity userRole = new UserRolesEntity();
            userRole.setExpiryDate(role.expiryDate());
            userRole.setUserRolePoid(role.userRolePoId());
            UserRolesEntityKey userRolesEntityKey = new UserRolesEntityKey();
            userRolesEntityKey.setUserPoid(finalUser.getUserPoid());
            userRolesEntityKey.setDetRowId(getNextDetRowIdForUserRole(finalUser.getUserPoid()));

            userRole.setId(userRolesEntityKey);

            userRoleRepository.save(userRole);
            String logDetail = String.format("Row Created on User Role with DetRowId %s ", userRole.getId().getDetRowId());
            loggingService.createLogSummaryEntry(UserContext.getDocumentId(), finalUser.getUserPoid().toString(), logDetail);

        } else {
            throw new InputMismatchException("You are attempting to update the same role multiple times. Please review your selection. userRoleId -> " + role.userRoleId());
        }

    }

    private void deleteUserRole(UserRoleDto role, User finalUser) {
        UserRolesEntity rolePresent = userRoleRepository.getUserRolesEntitiesById_UserPoidAndUserRolePoid(finalUser.getUserPoid(), role.userRolePoId());
        if (null != rolePresent) {
            userRoleRepository.deleteByUserRolePoidAndId_UserPoid(role.userRolePoId(), finalUser.getUserPoid());
            loggingService.logDelete(rolePresent, UserContext.getDocumentId(), finalUser.getUserPoid().toString());
        } else {
            throw new InputMismatchException("Cannot delete a role that  is not assigned to the user, userRoleId  -> " + role.userRoleId());
        }
    }

    private void updateUserRole(UserRoleDto role, User finalUser) {
        UserRolesEntity rolePresent = userRoleRepository.findById_UserPoidAndId_DetRowId(finalUser.getUserPoid(), role.detRowId());
        if (null != rolePresent) {
            Date oldExpiryDate = rolePresent.getExpiryDate();
            rolePresent.setExpiryDate(role.expiryDate());
            userRoleRepository.save(rolePresent);

            String docId = UserContext.getDocumentId();
            String detail = String.format("KeyId = USER_POID:%s DET_ROW_ID:%s", finalUser.getUserId(), rolePresent.getId().getDetRowId());
            loggingService.logSimpleFieldChange(UserRolesEntity.class, docId, finalUser.getUserPoid().toString(), "ExpiryDate", oldExpiryDate != null ? oldExpiryDate.toString() : null, rolePresent.getExpiryDate() != null ? rolePresent.getExpiryDate().toString() : null, detail);

        } else {
            throw new InputMismatchException("Cannot update a role that is not assigned to the user, userRoleId   -> " + role.userRoleId());
        }
    }

    public Long getNextDetRowIdForUserRole(Long userPoid) {
        Long max = userRoleRepository.findMaxDetRowIdByUserPoid(userPoid);
        return (max == null) ? 1L : max + 1;
    }

    public Long getNextDetRowIdForUserCompanies(Long userPoid) {
        Long max = usersCompanyRepository.findMaxDetRowIdByUserPoid(userPoid);
        return (max == null) ? 1L : max + 1;
    }

    public Boolean isUserExistsByUserNameAndUserPoid(String userName, Long userPoid) {
        if (userPoid == null) {
            return userRepository.existsByUserNameIgnoreCase(userName);
        }
        return userRepository.existsByUserNameIgnoreCaseAndUserPoidNot(userName, userPoid);
    }

    public Boolean isUserExistsByUserIdAndUserPoid(String userId, Long userPoid) {
        if (userPoid == null) {
            return userRepository.existsByUserIdIgnoreCase(userId);
        }
        return userRepository.existsByUserIdIgnoreCaseAndUserPoidNot(userId, userPoid);
    }

    public Boolean isUserExistsByUserEmailAndUserPoid(String userEmail, Long userPoid) {
        if (userPoid == null) {
            return userRepository.existsByEmailIgnoreCaseAndAuthenticationMethod(userEmail, "Y");
        }
        return userRepository.existsByEmailIgnoreCaseAndAuthenticationMethodAndUserPoidNot(userEmail, "Y", userPoid);
    }

    public String getUserNameByUserPoid(Long userPoid) {
        if (userPoid == null) {
            return null;
        }
        return userRepository.findUserNameByUserPoid(userPoid);
    }

    @Transactional
    public void softDeleteUser(Long userPoid, DeleteReasonDto deleteReasonDto) {
        User user = userRepository.findById(userPoid)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userPoid", userPoid));
        
        documentDeleteService.deleteDocument(
                userPoid,
                "GLOBAL_USERS",
                "USER_POID",
                deleteReasonDto,
                null
        );
    }

    public Long getUserPoidByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return null;
        }
        Long poid = userRepository.findUserPoidByUserId(userId);
        if (poid == null) {
            throw new RuntimeException("User not found for userId: " + userId);
        }
        return poid;
    }

    public ResponseEntity<?> createNewUserPassword(Long userPoid) {
        try {
            if (userPoid == null) {
                return badRequest("userPoid is required");
            }

            User user = userRepository.findById(userPoid)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "userPoid", userPoid));

            String email = user.getEmail();
            if (StringUtils.isBlank(email) || email.trim().equals(".")) {
                return badRequest("User's email must not be empty.");
            }

            if (!"Y".equalsIgnoreCase(user.getActive())) {
                return badRequest("User is not active. Cannot create password.");
            }

            String newPassword = generateRandomPassword(10);
            String hashedPassword = getSecureString(newPassword, "salt");

            String procedure = "{ ? = call FUNC_USER_PSWD_CREATE(?, ?, ?) }";

            String result = jdbcTemplate.execute(procedure, (CallableStatementCallback<String>) cs -> {
                cs.registerOutParameter(1, java.sql.Types.VARCHAR);
                cs.setLong(2, userPoid);
                cs.setString(3, newPassword);
                cs.setString(4, hashedPassword);
                cs.execute();
                return cs.getString(1);
            });

            if (StringUtils.isNotBlank(result) && result.toUpperCase().startsWith("TRUE")) {
                log.info("Password created for userPoid: {} and email sent to: {}", userPoid, email);
                return success("Password created and email sent.", result);
            } else {
                log.warn("Password creation failed for userPoid: {} | DB function returned: {}", userPoid, result);
                return internalServerError(result);
            }

        } catch (Exception e) {
            log.error("Exception while creating password for userPoid {}: {}", userPoid, e.getMessage(), e);
            return internalServerError(e.getMessage());
        }
    }

    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIndex = (int) (Math.random() * chars.length());
            password.append(chars.charAt(randomIndex));
        }
        return password.toString();
    }

    public static String getSecureString(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    public String resetUserPassword(String userId) {
        try {
            User user = validateActiveUser(userId);

            String newPassword = generateRandomPassword(10);
            String hashedPassword = getSecureString(newPassword, "salt");

            String function = "{ ? = call FUNC_USER_PSWD_RESET(?, ?, ?) }";

            return jdbcTemplate.execute(function, (CallableStatementCallback<String>) cs -> {
                cs.registerOutParameter(1, java.sql.Types.VARCHAR);
                cs.setLong(2, user.getUserPoid());
                cs.setString(3, newPassword);
                cs.setString(4, hashedPassword);
                cs.execute();
                return cs.getString(1);
            });
        } catch (AsgException e) {
            throw e;
        } catch (Exception e) {
            log.error("Exception while resetting password for userId {}: {}", userId, e.getMessage(), e);
            throw new AsgException("Exception while resetting password: " + e.getMessage(), 500);
        }
    }

    private User validateActiveUser(String userId) {
        User user = userRepository.findByActiveUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        if (!"Y".equalsIgnoreCase(user.getActive())) {
            throw new AsgException("User is not active", 400);
        }

        if ("Y".equalsIgnoreCase(user.getDeleted())) {
            throw new AsgException("User is deleted", 400);
        }

        String email = user.getEmail();
        ValidationUtil.validateEmailRequiredField(email);

        return user;
    }

}
