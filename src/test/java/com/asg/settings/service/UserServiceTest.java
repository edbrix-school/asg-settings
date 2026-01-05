package com.asg.settings.service;

import com.asg.common.lib.dto.CompanyDto;
import com.asg.common.lib.dto.TimeZoneDto;
import com.asg.common.lib.dto.UserRoleDto;
import com.asg.common.lib.exception.AsgException;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.settings.dto.CreateUserRequest;
import com.asg.settings.dto.UserCompanyDto;
import com.asg.settings.dto.UserDto;
import com.asg.settings.dto.UserResponse;
import com.asg.settings.entity.LocationMasterEntity;
import com.asg.settings.entity.User;
import com.asg.settings.entity.UserEntity;
import com.asg.settings.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private UsersCompanyRepository usersCompanyRepository;
    @Mock
    private LocationMasterRepository locationMasterRepository;
    @Mock
    private CompanyService companyService;
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private RoleService roleService;
    @Mock
    private DataSource dataSource;
    @Mock
    private DocumentService documentService;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserEntity userEntity;
    private LocationMasterEntity locationEntity;
    private CreateUserRequest createUserRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserPoid(1L);
        user.setUserId("TEST_USER");
        user.setUserName("Test User");
        user.setEmail("test@example.com");
        user.setUserMobile("1234567890");
        user.setDefaultCompanyPoid(1L);
        user.setDefaultLocationPoid(1L);
        user.setGroupPoid(1L);
        user.setCreatedDate(Timestamp.from(Instant.now()));
        user.setExpiryDate(new java.sql.Date(System.currentTimeMillis()));
        user.setSeqno(1);
        user.setActive("Y");
        user.setAuthenticationMethod("N");
        user.setUserLocked("N");
        user.setUserLockedReason("");
        user.setResetPasswordNextLogin("N");

        userEntity = new UserEntity(BigDecimal.valueOf(1L), "TEST_USER", "Test User", "test@example.com");

        locationEntity = new LocationMasterEntity();
        locationEntity.setLocationPoid(1L);
        locationEntity.setLocationCode("LOC001");
        locationEntity.setLocationName("Test Location");
        locationEntity.setSeqNo(1);

        createUserRequest = new CreateUserRequest();
        createUserRequest.setUserId("NEW_USER");
        createUserRequest.setUserName("New User");
        createUserRequest.setUserEmail("new@example.com");
        createUserRequest.setDefaultCompanyPoid(1L);
        createUserRequest.setSeqNo(1);
        createUserRequest.setAuthenticationMethod("Y"); // Microsoft authentication by default
        createUserRequest.setUserCompanies(Arrays.asList(
                new UserCompanyDto(1L, "Company", "US", "State", new java.sql.Date(System.currentTimeMillis()),
                        "N", new TimeZoneDto(1L, "UTC", "UTC"), "DD/MM/YYYY", "Y", "isCreated")
        ));
        createUserRequest.setUserRoles(Arrays.asList(
                new UserRoleDto(1L, "ROLE1", "Test Role", new java.sql.Date(System.currentTimeMillis()), "N", "Y", "isCreated")
        ));
    }

    @Test
    void getUserDetailsByRolePoid_ShouldReturnUserResponse() {
        List<UserEntity> users = Arrays.asList(userEntity);
        when(userRepository.fetchUserRoleByRolePoid(1L)).thenReturn(users);

        UserResponse result = userService.getUserDetailsByRolePoid(1L);

        assertNotNull(result);
        assertEquals(1, result.getTotalRecords());
        assertEquals(1L, result.getUserRolePoid());
        assertEquals("Success", result.getMessage());
        assertEquals("200", result.getStatus());
    }

    @Test
    void findByEmailAddress_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findByActiveEmail("test@example.com")).thenReturn(Optional.of(user));

        User result = userService.findByEmailAddress("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void findByEmailAddress_ShouldReturnNull_WhenUserNotFound() {
        when(userRepository.findByActiveEmail("test@example.com")).thenReturn(Optional.empty());

        User result = userService.findByEmailAddress("test@example.com");

        assertNull(result);
    }

    @Test
    void getUserDetails_ShouldReturnUserDto_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleService.getUserAuthRoles(1L)).thenReturn(Collections.emptyList());
        when(companyService.getUsersCompanies(1L)).thenReturn(Collections.emptyList());
//        when(companyService.getCountryForCompany(1L)).thenReturn(null);
        when(companyService.getCountryCodeForCompany(1L)).thenReturn(null);
        when(companyService.getCompany(1L)).thenReturn(createCompanyDto());
        when(locationMasterRepository.findByLocationPoid(1L)).thenReturn(locationEntity);

        UserDto result = userService.getUserDetails(1L);

        assertNotNull(result);
        assertEquals("TEST_USER", result.userId());
        assertEquals("Test User", result.userName());
        assertEquals(1L, result.userPoid());
    }

    @Test
    void getUserDetails_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserDetails(1L));
    }

    @Test
    void createUser_ShouldThrowException_WhenUserIdAlreadyExists() throws SQLException {
        when(userRepository.existsByUserIdIgnoreCase("NEW_USER")).thenReturn(true);

        assertThrows(InputMismatchException.class, () -> userService.createUser(createUserRequest));
    }

    @Test
    void createUser_ShouldReturnUserPoid_WhenValidRequest() throws SQLException {
        when(userRepository.existsByUserIdIgnoreCase("NEW_USER")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCaseAndAuthenticationMethod("new@example.com", "Y")).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User userArg = invocation.getArgument(0);
            userArg.setUserPoid(1L);
            return userArg;
        });
        when(usersCompanyRepository.findMaxDetRowIdByUserPoid(anyLong())).thenReturn(1L);
        when(userRoleRepository.findMaxDetRowIdByUserPoid(anyLong())).thenReturn(1L);

        String result = userService.createUser(createUserRequest);

        assertEquals("1", result);
        verify(userRepository).saveAndFlush(any(User.class));
    }

    @Test
    void createUser_ShouldThrowException_WhenSeqNoTooLong() {
        createUserRequest.setSeqNo(123456);

        assertThrows(InputMismatchException.class, () -> userService.createUser(createUserRequest));
    }

    @Test
    void createUser_ShouldThrowException_WhenNoCompanies() {
        createUserRequest.setUserCompanies(Collections.emptyList());
        when(userRepository.existsByUserIdIgnoreCase("NEW_USER")).thenReturn(false);

        assertThrows(InputMismatchException.class, () -> userService.createUser(createUserRequest));
    }

    @Test
    void createUser_ShouldThrowException_WhenDefaultCompanyNotInList() {
        createUserRequest.setDefaultCompanyPoid(999L);
        when(userRepository.existsByUserIdIgnoreCase("NEW_USER")).thenReturn(false);

        assertThrows(InputMismatchException.class, () -> userService.createUser(createUserRequest));
    }

    @Test
    void createUser_ShouldThrowException_WhenEmailAlreadyExists() {
        when(userRepository.existsByUserIdIgnoreCase("NEW_USER")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCaseAndAuthenticationMethod("new@example.com", "Y")).thenReturn(true);

        assertThrows(AsgException.class, () -> userService.createUser(createUserRequest));
    }

    @Test
    void updateUser_ShouldThrowException_WhenEmailAlreadyExists() {
        createUserRequest.setUserPoid(1L);
        createUserRequest.setUserId("TEST_USER");
        when(userRepository.findByUserPoid(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailIgnoreCaseAndAuthenticationMethodAndUserPoidNot("new@example.com", "Y", 1L)).thenReturn(true);

        assertThrows(AsgException.class, () -> userService.updateUser(createUserRequest));
    }

    @Test
    void updateUser_ShouldReturnUserPoid_WhenValidRequest() throws SQLException {
        createUserRequest.setUserPoid(1L);
        createUserRequest.setUserId("TEST_USER");
        when(userRepository.findByUserPoid(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailIgnoreCaseAndAuthenticationMethodAndUserPoidNot("new@example.com", "Y", 1L)).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User userArg = invocation.getArgument(0);
            userArg.setUserPoid(1L);
            return userArg;
        });
        when(usersCompanyRepository.findMaxDetRowIdByUserPoid(anyLong())).thenReturn(1L);
        when(userRoleRepository.findMaxDetRowIdByUserPoid(anyLong())).thenReturn(1L);

        String result = userService.updateUser(createUserRequest);

        assertEquals("1", result);
        verify(userRepository).saveAndFlush(any(User.class));
    }

    @Test
    void updateUser_ShouldThrowException_WhenUserNotFound() {
        createUserRequest.setUserPoid(1L);
        when(userRepository.findByUserPoid(1L)).thenReturn(Optional.empty());

        assertThrows(AsgException.class, () -> userService.updateUser(createUserRequest));
    }

    @Test
    void updateUser_ShouldThrowException_WhenUserIdChanged() {
        createUserRequest.setUserPoid(1L);
        createUserRequest.setUserId("DIFFERENT_USER");
        when(userRepository.findByUserPoid(1L)).thenReturn(Optional.of(user));

        assertThrows(InputMismatchException.class, () -> userService.updateUser(createUserRequest));
    }

    @Test
    void softDeleteUser_ShouldUpdateUserFields_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.softDeleteUser(1L);

        assertEquals("N", user.getActive());
        assertEquals("Y", user.getDeleted());
        verify(userRepository).save(user);
    }

    @Test
    void softDeleteUser_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.softDeleteUser(1L));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void isUserExistsByUserNameAndUserPoid_ShouldReturnTrue_WhenUserExists() {
        when(userRepository.existsByUserNameIgnoreCaseAndUserPoidNot("testUser", 1L)).thenReturn(true);

        Boolean result = userService.isUserExistsByUserNameAndUserPoid("testUser", 1L);

        assertTrue(result);
    }

    @Test
    void isUserExistsByUserNameAndUserPoid_ShouldReturnFalse_WhenUserPoidIsNull() {
        when(userRepository.existsByUserNameIgnoreCase("testUser")).thenReturn(false);

        Boolean result = userService.isUserExistsByUserNameAndUserPoid("testUser", null);

        assertFalse(result);
    }

    @Test
    void isUserExistsByUserIdAndUserPoid_ShouldReturnTrue_WhenUserExists() {
        when(userRepository.existsByUserIdIgnoreCaseAndUserPoidNot("testUser", 1L)).thenReturn(true);

        Boolean result = userService.isUserExistsByUserIdAndUserPoid("testUser", 1L);

        assertTrue(result);
    }

    @Test
    void isUserExistsByUserIdAndUserPoid_ShouldReturnFalse_WhenUserPoidIsNull() {
        when(userRepository.existsByUserIdIgnoreCase("testUser")).thenReturn(false);

        Boolean result = userService.isUserExistsByUserIdAndUserPoid("testUser", null);

        assertFalse(result);
    }

    @Test
    void isUserExistsByUserEmailAndUserPoid_ShouldReturnTrue_WhenUserExists() {
        when(userRepository.existsByEmailIgnoreCaseAndAuthenticationMethodAndUserPoidNot("test@example.com", "Y", 1L)).thenReturn(true);

        Boolean result = userService.isUserExistsByUserEmailAndUserPoid("test@example.com", 1L);

        assertTrue(result);
    }

    @Test
    void isUserExistsByUserEmailAndUserPoid_ShouldReturnFalse_WhenUserPoidIsNull() {
        when(userRepository.existsByEmailIgnoreCaseAndAuthenticationMethod("test@example.com", "Y")).thenReturn(false);

        Boolean result = userService.isUserExistsByUserEmailAndUserPoid("test@example.com", null);

        assertFalse(result);
    }

    @Test
    void isUserExistsByUserEmailAndUserPoid_ShouldReturnTrue_WhenUserPoidIsNullAndEmailExists() {
        when(userRepository.existsByEmailIgnoreCaseAndAuthenticationMethod("test@example.com", "Y")).thenReturn(true);

        Boolean result = userService.isUserExistsByUserEmailAndUserPoid("test@example.com", null);

        assertTrue(result);
    }

    @Test
    void getNextDetRowIdForUserRole_ShouldReturnOne_WhenNoExistingRoles() {
        when(userRoleRepository.findMaxDetRowIdByUserPoid(1L)).thenReturn(null);

        Long result = userService.getNextDetRowIdForUserRole(1L);

        assertEquals(1L, result);
    }

    @Test
    void getNextDetRowIdForUserRole_ShouldReturnIncremented_WhenExistingRoles() {
        when(userRoleRepository.findMaxDetRowIdByUserPoid(1L)).thenReturn(5L);

        Long result = userService.getNextDetRowIdForUserRole(1L);

        assertEquals(6L, result);
    }

    @Test
    void getNextDetRowIdForUserCompanies_ShouldReturnOne_WhenNoExistingCompanies() {
        when(usersCompanyRepository.findMaxDetRowIdByUserPoid(1L)).thenReturn(null);

        Long result = userService.getNextDetRowIdForUserCompanies(1L);

        assertEquals(1L, result);
    }

    @Test
    void getNextDetRowIdForUserCompanies_ShouldReturnIncremented_WhenExistingCompanies() {
        when(usersCompanyRepository.findMaxDetRowIdByUserPoid(1L)).thenReturn(3L);

        Long result = userService.getNextDetRowIdForUserCompanies(1L);

        assertEquals(4L, result);
    }

    @Test
    void createUser_ShouldValidateEmailForMicrosoftAuth() throws SQLException {
        createUserRequest.setAuthenticationMethod("Y"); // Microsoft authentication
        when(userRepository.existsByUserIdIgnoreCase("NEW_USER")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCaseAndAuthenticationMethod("new@example.com", "Y")).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User userArg = invocation.getArgument(0);
            userArg.setUserPoid(1L);
            return userArg;
        });
        when(usersCompanyRepository.findMaxDetRowIdByUserPoid(anyLong())).thenReturn(1L);
        when(userRoleRepository.findMaxDetRowIdByUserPoid(anyLong())).thenReturn(1L);

        String result = userService.createUser(createUserRequest);

        assertEquals("1", result);
        verify(userRepository).existsByEmailIgnoreCaseAndAuthenticationMethod("new@example.com", "Y");
    }

    @Test
    void updateUser_ShouldValidateEmailForMicrosoftAuth() throws SQLException {
        createUserRequest.setUserPoid(1L);
        createUserRequest.setUserId("TEST_USER");
        createUserRequest.setAuthenticationMethod("Y"); // Microsoft authentication
        when(userRepository.findByUserPoid(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailIgnoreCaseAndAuthenticationMethodAndUserPoidNot("new@example.com", "Y", 1L)).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User userArg = invocation.getArgument(0);
            userArg.setUserPoid(1L);
            return userArg;
        });
        when(usersCompanyRepository.findMaxDetRowIdByUserPoid(anyLong())).thenReturn(1L);
        when(userRoleRepository.findMaxDetRowIdByUserPoid(anyLong())).thenReturn(1L);

        String result = userService.updateUser(createUserRequest);

        assertEquals("1", result);
        verify(userRepository).existsByEmailIgnoreCaseAndAuthenticationMethodAndUserPoidNot("new@example.com", "Y", 1L);
    }

    private CompanyDto createCompanyDto() {
        CompanyDto dto = new CompanyDto();
        dto.setCompanyPoid(1L);
        dto.setCompanyCode("TEST_COMPANY");
        dto.setCompanyName("Test Company");
        dto.setSeqNo(1);
        return dto;
    }
}
