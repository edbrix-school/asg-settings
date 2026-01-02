package com.asg.settings.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.exception.AsgException;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.settings.dto.*;
import com.asg.settings.dto.request.CreatePasswordRequest;
import com.asg.settings.dto.request.FavoriteMenuRequest;
import com.asg.settings.dto.request.ResetPasswordRequest;
import com.asg.settings.dto.response.UserPermissionsResponse;
import com.asg.common.lib.entity.Company;
import com.asg.settings.entity.FavoriteMenuEntity;
import com.asg.settings.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.asg.common.lib.dto.response.ApiResponse.*;

@RestController
@RequestMapping("/v1/users")

@Slf4j
@Validated
public class UserController {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private UserService userService;

    @Autowired
    private FavoriteMenuService favoriteMenuService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private LoggingService loggingService;

    @Operation(summary = "Get User List By Role Poid")
    @GetMapping
    public ResponseEntity<?> getUserListByRolePoid(@RequestParam("userRoleId") Long userRoleId) {

        UserResponse usersDto = userService.getUserDetailsByRolePoid(userRoleId);
        return success("User list fetched successfully", usersDto);

    }

    @Operation(
            summary = "Reset Password for the User",
            description = "Reset password for an existing user based on the request payload."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = """
                    ### Request Body
                        Reset password for an existing user.
                        - **userId:** User ID of the user resetting the password. Required.
                    
                    ### Notes
                        - userId is required for successful password reset.
                        - User must be active and have valid email.
                    """,
            content = @Content(
                    examples = {
                            @ExampleObject(
                                    name = "Reset Password Example",
                                    value = """
                                            {
                                                "userId": "john_doe"
                                            }
                                            """
                            )
                    }
            )
    )
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            String result = userService.resetUserPassword(request.getUserId());
            if (!StringUtils.isBlank(result) && result.toUpperCase().contains("TRUE")) {
                loggingService.createLogSummaryEntry(LogDetailsEnum.PASSWORD_RESET, UserContext.getDocumentId(), userService.getUserPoidByUserId(request.getUserId()).toString());
                log.info("Password reset successful for userId: {}", request.getUserId());
                return success("Password reset successful", request);
            } else {
                log.warn("Password reset failed for userId: {} | DB function returned: {}", request.getUserId(), result);
                throw new AsgException("Password reset failed: " + result, 400);
            }
        } catch (AsgException e) {
            return badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Error resetting password for userId {}: {}", request.getUserId(), e.getMessage());
            return internalServerError("Failed to reset password: " + e.getMessage());
        }
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @Operation(
            summary = "List Users with Search and Sort",
            description = "Provide search filters. Valid `searchField` values: GLOBALSEARCH or (USER_ID, USER_NAME, USER_EMAIL, USER_MOBILE). Sorting default on userPoid, desc." +
                    "Will be searched in all available fields given in list_of_records_sql or main_table field in doc_master table." +
                    "Sorting will be applied as specified in list_of_records_sql in doc_master table." +
                    "Display fields for showing columns can be customized through list_of_display_columns_and_types field in doc_master."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = """
                    - ### Filters:
                      Use either:
                      1. A single `GLOBALSEARCH` filter, OR
                      2. Any combination of specific fields (USER_ID, USER_NAME, USER_EMAIL, USER_MOBILE).
                      3. operator field will either have "AND" or "OR", if not given will be considered as "OR",
                         not required for GLOBALSEARCH, for non GLOBALSEARCH need to give only 1 time
                      4. isDeleted when 'N' or null, will search and return non deleted records, 'Y' will check and return deleted records
                      5. sort will be default on primary key ascending if specified in db field otherwise you can override it giving the field name and the direction.
                    
                    - #### Global Search:
                      Apply one search term across multiple fields.
                      No operator need to send in this case.
                      • { "searchField": "GLOBALSEARCH", "searchValue": "mohammed" }
                    
                    - #### Single Field, Single Value:
                      Search one field with one value.
                      • { "searchField": "USER_ID", "searchValue": "DEVUSER2" },
                    
                    - #### Single Field, Multiple Values:
                      Provide multiple values for the same field, separated by `|`.
                      • { "searchField": "USER_ID", "searchValue": "DEVUSER2|TESTUSER1" }
                    
                    - #### Multiple Different Fields, Single or Multiple Value:
                      Provide one or multiple search values across multiple fields.
                      • { "searchField": "USER_ID", "searchValue": "DEVUSER2" },
                      • { "searchField": "USER_NAME", "searchValue": "DEVUSER2|SELVA" }
                      • "operator" :  "AND"/"OR"
                    
                    - ### Sorting:
                      Defaults to whatever specified in list_of_records_sql field mostly primary key ascending.
                      To override, pass `sort=<field>,ASC|DESC` in query params.
                      Examples:
                      • sort=USER_NAME,ASC
                      • sort=USER_ID,DESC
                    
                    """,
            content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = FilterDto.class)),
                    examples = {
                            @ExampleObject(
                                    name = "User Filters",
                                    value = """
                                            {
                                            "operator": "AND",
                                            "isDeleted": "Y",
                                            "filters": [
                                               { "searchField": "GLOBALSEARCH", "searchValue": "mohammed" },
                                               { "searchField": "USER_ID", "searchValue": "NANDA|SOUMYA" },
                                               { "searchField": "USER_NAME", "searchValue": "KUMAR"},
                                               { "searchField": "USER_EMAIL", "searchValue": "ssundaram@hexalytics.com"},
                                               { "searchField": "USER_MOBILE", "searchValue": null}
                                            ]
                                            }
                                            """
                            )
                    }
            )
    )
    @PostMapping("/list")
    public ResponseEntity<?> listUsers(
            @ParameterObject Pageable pageable,
            @RequestBody(required = false) FilterRequestDto filters
    ) {
        try {
            Map<String, Object> users = userService.listUsers(UserContext.getDocumentId(), filters, pageable);

            return success("Users list fetched successfully", users);

        } catch (Exception e) {
            return internalServerError("Unable to fetch user list: " + e.getMessage());
        }
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @Operation(
            summary = "Get User Details",
            description = """
                        Fetch user details by `userPoid`.
                    
                        ### Request Paramters
                        - **userPoid:**  User's Primary Key
                    
                    """
    )
    @GetMapping("/userdetails")
    public ResponseEntity<?> getUserDetails(@RequestParam(required = false) Long userPoid) {
        try {
            if (userPoid == null) {
                throw new RuntimeException("Userpoid is needed");
            }
            UserDto userDetails = userService.getUserDetails(userPoid);
            loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(), userPoid.toString());
            return success("User details fetched successfully", userDetails);
        } catch (Exception ex) {
            return internalServerError("Unable to fetch user details: " + ex.getMessage());
        }
    }

    @GetMapping("/company")
    public ResponseEntity<?> getUserCompanies(@RequestParam(required = false) Long userPoid) {
        try {
            List<Company> companies = companyService.getCompanies(userPoid);
            return success("Company list fetched successfully", companies);
        } catch (Exception ex) {
            return internalServerError("Unable to fetch company list: " + ex.getMessage());
        }
    }

    @GetMapping("/menu")
    public ResponseEntity<?> getUserMenus(@RequestParam(required = false) Long userPoid) {
        try {
            List<MenuItemDto> userMenuList = userService.getUserMenu(userPoid);
            String userId = UserContext.getUserId();
            List<PermissionDto> permissions = permissionService.getUserPermissions(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("menus", userMenuList);
            response.put("permissions", permissions);

            return success("User menu and permissions fetched successfully", response);
        } catch (Exception ex) {
            return internalServerError("Unable to fetch user menu and permissions: " + ex.getMessage());
        }
    }

    @GetMapping("/favorite-menu/favoriteList")
    public ResponseEntity<?> getFavoriteMenuList(@RequestParam(required = false) Long userPoid,
                                                 @Parameter(hidden = true) @RequestParam(required = false, defaultValue = "") String userId) {
        try {
            String processedUserId = (!userId.isEmpty()) ? userId.toUpperCase() : null;

            List<FavoriteMenuEntity> favorites = favoriteMenuService.getFavoriteList(userPoid, processedUserId);
            return success("success", favorites);
        } catch (Exception e) {
            return internalServerError("Error fetching favorite menus: " + e.getMessage());
        }
    }

    @GetMapping("/favorite-menu/unAssignedFavoriteList")
    public ResponseEntity<?> getFavoriteMenusAvailable(@RequestParam(required = false) Long userPoid,
                                                       @RequestParam(required = false) String userId,
                                                       @RequestParam(required = false) String search,
                                                       @ParameterObject Pageable pageable) {
        try {
            Map<String, Object> favorites = favoriteMenuService.getUnassignedFavList(userPoid, userId.toUpperCase(), search, pageable);
            return success("success", favorites);
        } catch (Exception e) {
            return internalServerError("Error fetching favorite menus: " + e.getMessage());
        }
    }

    @PostMapping("/favorite-menu/add")
    public ResponseEntity<?> addFavoriteMenu(@RequestBody FavoriteMenuRequest request) {
        try {
            if (request == null || request.getUserId() == null ||
                    request.getMenuGroup() == null || request.getSelectedDocIds() == null) {
                throw new RuntimeException("Missing or invalid fields: userId, userPoid, menuGroup, or selectedDocIds");
            }
            String result = favoriteMenuService.addFavoriteMenu(request);
            return success("Favorite menu added successfully", result);
        } catch (Exception e) {
            return internalServerError(e.getMessage());
        }
    }

    @PostMapping("/favorite-menu/remove")
    public ResponseEntity<?> removeFavoriteMenus(@RequestBody FavoriteMenuRequest request) {

        try {
            if (request == null || request.getUserId() == null ||
                    request.getMenuGroup() == null || request.getSelectedDocIds() == null) {
                throw new RuntimeException("Missing or invalid fields: userId, userPoid, menuGroup, or selectedDocIds");
            }
            String result = favoriteMenuService.removeFavoriteMenus(request.getUserId(), request.getUserPoid(), request.getMenuGroup(), request.getSelectedDocIds());
            return success("Favorite menu removed successfully", result);
        } catch (Exception e) {
            return internalServerError(e.getMessage());
        }
    }

    @Operation(
            summary = "Create User",
            description = "Create new user"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = """
                    
                    
                    ### Request Body
                        Provide user details.
                        - **userPoid:** If `null`, a new user will be created; otherwise, the existing user will be updated.
                        - Other fields as applicable for user setup.
                    
                    """
    )
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest userDto) {
        String userPoid = userService.createUser(userDto);
        userService.createNewUserPassword(Long.valueOf(userPoid));
        loggingService.createLogSummaryEntry(LogDetailsEnum.PASSWORD_EMAIL_SENT, UserContext.getDocumentId(), userPoid);
        Map<String, Object> data = new HashMap<>();
        data.put("userPoid", userPoid);
        return success("User created successfully", data);
    }

    @Operation(
            summary = "Update User",
            description = "Update user based on UserPoid"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = """
                    
                    
                    ### Request Body
                        Provide user details.
                        - **userPoid:**  user will be updated.
                        - Other fields as applicable for user setup.
                    
                    """
    )
    @PostMapping("/update")
    public ResponseEntity<?> updateUser(@Valid @RequestBody CreateUserRequest userDto) {
        String userPoid = userService.updateUser(userDto);
        Map<String, Object> data = new HashMap<>();
        data.put("userPoid", userPoid);
        return success("User updated successfully", data);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @Operation(
            summary = "Get Logged in User Permissions",
            description = """
                        Fetch logged in user permissions.
                    
                    """
    )
    @GetMapping("/permissions")
    public ResponseEntity<?> getPermissions(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return unauthorized("Unauthorized to get permissions");
            }

            String userId = UserContext.getUserId() != null ? UserContext.getUserId() : null;

            List<PermissionDto> permissions = permissionService.getUserPermissions(userId);

            return success("Permissions for this user fetched successfully.", new UserPermissionsResponse(userId, permissions));
        } catch (Exception ex) {
            return internalServerError("Failed to fetch permissions ->  " + ex.getMessage());
        }
    }

    @GetMapping("/user-exists-by-user-name")
    public ResponseEntity<?> isUserExistsByUserName(
            @Parameter(
                    description = "Username to check",
                    example = "john_doe",
                    required = true
            )
            @RequestParam String userName,

            @Parameter(
                    description = "userPoid to check",
                    example = "3347",
                    required = false
            )
            @RequestParam(required = false) Long userPoid) {

        if (StringUtils.isBlank(userName)) {
            return success("User does not exists by name", false);
        }
        Boolean isExists = userService.isUserExistsByUserNameAndUserPoid(userName, userPoid);
        if (isExists) {
            return success("User exists by name", true);
        }
        return success("User does not exists by name", false);
    }

    @GetMapping("/user-exists-by-user-id")
    public ResponseEntity<?> isUserExistsByUserId(
            @Parameter(
                    description = "UserId to check",
                    example = "john_doe",
                    required = true
            )
            @RequestParam String userId,

            @Parameter(
                    description = "userPoid to check",
                    example = "3347",
                    required = false
            )
            @RequestParam(required = false) Long userPoid) {

        if (StringUtils.isBlank(userId)) {
            return success("User does not exists by userId", false);
        }
        boolean isExists = userService.isUserExistsByUserIdAndUserPoid(userId, userPoid);
        if (isExists) {
            return success("User exists by userId", true);
        }
        return success("User does not exists by userId", false);
    }

    @GetMapping("/user-exists-by-user-email")
    public ResponseEntity<?> isUserExistsByUserEmail(
            @Parameter(
                    description = "User email to check",
                    example = "john@company.com",
                    required = true
            )
            @RequestParam String userEmail,

            @Parameter(
                    description = "userPoid to check",
                    example = "3347",
                    required = false
            )
            @RequestParam(required = false) Long userPoid) {

        if (StringUtils.isBlank(userEmail)) {
            return success("Email ID not assigned to other user", false);
        }
        Boolean isExists = userService.isUserExistsByUserEmailAndUserPoid(userEmail, userPoid);
        if (isExists) {
            return success("Email ID is already assigned to another user. Please change Email ID or Authentication method", true);
        }
        return success("Email ID not assigned to other user", false);
    }

    @Operation(
            summary = "Create New User Password",
            description = "Creates a new password for an existing user and sends it via email."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = """
                    ### Request Body
                    Provide the `userPoid` for the user to create a new password.
                    - **userPoid:** Required, the unique ID of the user.
                    """,
            content = @Content(
                    examples = @ExampleObject(
                            name = "Create Password Example",
                            value = """
                                    {
                                        "userPoid": 1234
                                    }
                                    """
                    )
            )
    )
    @PostMapping("/create-password")
    public ResponseEntity<?> createUserPassword(
            @Valid @RequestBody CreatePasswordRequest request) {
        try {
            loggingService.createLogSummaryEntry(LogDetailsEnum.PASSWORD_EMAIL_SENT, null, request.getUserPoid().toString());
            return userService.createNewUserPassword(request.getUserPoid());
        } catch (Exception e) {
            log.error("Error creating new password for userPoid {}: {}", request.getUserPoid(), e.getMessage());
            return internalServerError("Failed to create password: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Soft Delete User",
            description = "Deactivate a user by setting ACTIVE='N' and DELETED='Y' in GLOBAL_USERS table."
    )
    @DeleteMapping("/{userPoid}")
    public ResponseEntity<?> softDeleteUser(
            @PathVariable @NotNull @Min(1) Long userPoid) {
        try {
            userService.softDeleteUser(userPoid);
            loggingService.createLogSummaryEntry(LogDetailsEnum.DELETED, null, userPoid.toString());
            return success("User marked as deleted and deactivated successfully", null);
        } catch (ResourceNotFoundException e) {
            log.error("Error deactivating user with userPoid {}: {}", userPoid, e.getMessage());
            return notFound(e.getMessage());
        } catch (Exception e) {
            log.error("Error deactivating user with userPoid {}: {}", userPoid, e.getMessage());
            return internalServerError("Failed to deactivate user: " + e.getMessage());
        }
    }
}
