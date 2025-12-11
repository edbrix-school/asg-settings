package com.asg.settings.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.settings.dto.UserRoleRequestDto;
import com.asg.settings.dto.UserRoleRightsDetDto;
import com.asg.settings.dto.UserRoleRightsDto;
import com.asg.settings.dto.UserRolesDto;
import com.asg.settings.dto.request.LoadDefaultRightsRequest;
import com.asg.settings.dto.request.RightsUpdateRequest;
import com.asg.settings.dto.request.RolePermissionRequest;
import com.asg.settings.dto.response.LoadDefaultRightsResponse;
import com.asg.settings.dto.response.RolePermissionResponse;
import com.asg.settings.entity.RoleEntity;
import com.asg.settings.service.RolePermissionService;
import com.asg.settings.service.UserRoleService;
import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.asg.common.lib.dto.response.ApiResponse.*;

@RestController
@RequestMapping("/v1/user-roles")
@RequiredArgsConstructor
@Tag(name = "user-roles-controller", description = "APIs for managing user roles")
public class UserRoleController {

    private final UserRoleService userRoleService;

    private final RolePermissionService userPermissionService;

    @Operation(summary = "Fetch permissions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched the permissions"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{userRolePoid}/permissions")
    public ResponseEntity<?> getRolePermissions(@PathVariable Long userRolePoid) {
        List<UserRoleRightsDto> userRoleRightsDtos = userPermissionService.getUserRoleRights(userRolePoid);
        return success("Permissions fetched successfully", userRoleRightsDtos);
    }

    @Operation(
            summary = "Add Permissions",
            description = "Add new permission to the existing user based on the request payload."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,

            description = """
                    ### Request Body
                        Add permissions to the user by providing the required details.
                        - **roleId:** ID of the role. Required.
                        - **permissions:** Parameters to be added. This field is mandatory.
                    
                    ### Notes
                        - All fields are required for successful add permission.
                    """,

            content = @Content(
                    schema = @Schema(implementation = RoleEntity.class),
                    examples = {
                            @ExampleObject(
                                    name = "Add Permissions Example",
                                    value = """
                                            {
                                                "roleId": "222",
                                                "permissions": [
                                                    {
                                                        "detRowId": "1",
                                                        "docId": "300-200",
                                                        "rights": "000000",
                                                        "createdBy": "test",
                                                        "lastModifiedBy": "test"
                                                    },
                                                    {
                                                        "detRowId": "2",
                                                        "docId": "200-100",
                                                        "rights": "11111",
                                                        "createdBy": "test",
                                                        "lastModifiedBy": "test"
                                                    }
                                                ]
                                            }
                                            """
                            )
                    }
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully added permissions"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "409", description = "Conflict"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/permissions")
    public ResponseEntity<?> addPermissions(
            @Valid @RequestBody RolePermissionRequest request) {

        RolePermissionResponse response = userPermissionService.addPermissions(request);
        if (!response.getStatus().equals("FAILURE")) {
            return success("Successfully added permissions", response);
        } else {
            if (response.getErrors().stream().allMatch(e -> "Duplicate entry for docId".equals(e.getError()))) {
                return error(response.getMessage(), 409, response.getErrors());
            } else {
                return error(response.getMessage(), 500, response.getErrors());
            }
        }
    }

    @Operation(
            summary = "Update Permission",
            description = "Updating the permission for the docId. This endpoint allows you to update the permission. The updated details are provided in the request body. The response indicates the success or failure of the update operation."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,

            description = """
                    ### Request Body
                        Update the permissions by providing the required details.
                        - **rightsUpdateList:** Permissions to update. This field is mandatory.
                    
                    ### Notes
                        - All fields are required for successful permissions update.
                    """,

            content = @Content(
                    examples = {
                            @ExampleObject(
                                    name = "Update Permissions Example",
                                    value = """
                                            {
                                                "rightsUpdateList": [
                                                    {
                                                        "detRowId": "1",
                                                        "docId": "300-200",
                                                        "rights": "111000",
                                                        "lastModifiedBy": "test-user"
                                                    },
                                                    {
                                                        "detRowId": "2",
                                                        "docId": "200-100",
                                                        "rights": "110100",
                                                        "lastModifiedBy": "test-user"
                                                    }
                                                ]
                                            }
                                            """
                            )
                    }
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated permissions"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{roleId}/rights")
    public ResponseEntity<?> updatePermissions(
            @PathVariable Long roleId,
            @Valid @RequestBody RightsUpdateRequest request) {
        RolePermissionResponse response = userPermissionService.updatePermissions(roleId, request);
        if ("SUCCESS".equals(response.getStatus())) {
            return success("User Role Permissions Updated successfully", response);
        } else {
            return error(response.getMessage(), 500, response.getErrors());
        }
    }

    @Operation(summary = "Load Default and New Rights", description = "Calls PROC_GLOB_USRRLS_RIGHTS_DEF to load system default rights for a role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully loaded default rights"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/load-default-rights")
    public ResponseEntity<?> loadDefaultRights(@RequestBody LoadDefaultRightsRequest request) {
        if (request.getLoginUserPoid() == null || request.getUserRolePoid() == null) {
            return badRequest("Missing required fields: loginUserPoid, userRolePoid");
        }

        String status = userPermissionService.loadDefaultRights(request.getLoginUserPoid(), request.getUserRolePoid());

        if (status == null || status.contains("ERROR")) {
            return internalServerError("Failed to load default rights: " + status);
        }

        return success("Default rights loaded successfully", new LoadDefaultRightsResponse(status));

    }

    @Operation(summary = "Add User roles",
            description = "create a new user role based on the request payload.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = """
                    ### Request Parameters
                        - **userPoid:** User's Primary Key
                    
                    ### Request Body
                        Provide task details.  
                        - **userRoleId:**  "userRoleId must contain only uppercase letters and underscores, no spaces  
                        - **userRoleName:** userRoleName is Mandatory.  
                        - **userRoleName2:** userRoleName2 is Not Mandatory.  
                        - **groupPoid:** groupPoid is Mandatory.  
                        - **active:** active is Mandatory.  
                        - **companyPoid:** companyPoid is Not Mandatory.
                    """,
            content = @Content(
                    schema = @Schema(implementation = RoleEntity.class),
                    examples = {
                            @ExampleObject(
                                    name = "Task Create",
                                    value = """
                                            {
                                            "userRoleId": "ACC_BANKS",
                                            "userRoleName": "Account Banks Users",
                                            "userRoleName2": "Banking Module Role",
                                            "groupPoid": 1,
                                            "active": "Y",
                                            "companyPoid": 2001
                                            }
                                            """
                            )
                    }
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created the new Role"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<?> addUserRoles(@Valid @RequestBody UserRoleRequestDto userRoleRequestDto) {
        UserRolesDto userRolesDto = userRoleService.addUserRoles(userRoleRequestDto);
        return success("User Role created successfully", userRolesDto);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @Operation(
            summary = "List User Roles with Search and Sort",
            description = "Provide search filters. Valid `searchField` values: GLOBALSEARCH or (USER_ROLE_ID, USER_ROLE_NAME). Sorting default on userRolePoid, desc." +
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
                      2. Any combination of specific fields (USER_ROLE_ID, USER_ROLE_NAME).
                      3. operator field will either have "AND" or "OR", if not given will be considered as "OR"
                      4. isDeleted when 'N' or null, will search and return non deleted records, 'Y' will check and return deleted records
                    """,
            content = @Content(
                    schema = @Schema(implementation = FilterRequestDto.class),
                    examples = {
                            @ExampleObject(
                                    name = "User Role Filters",
                                    value = """
                                            {
                                                "operator":"OR",
                                                "isDeleted":"N",
                                                "filters":[
                                                    {
                                                        "searchField":"USER_ROLE_ID",
                                                        "searchValue":"PDA_MNGR"
                                                    },
                                                    {
                                                        "searchField":"USER_ROLE_NAME",
                                                        "searchValue":"ADMIN ROLES"
                                                    }
                                                ]
                                            }
                                            """
                            )
                    }
            )
    )
    @PostMapping("/list")
    public ResponseEntity<?> getUserRoles(
            @ParameterObject Pageable pageable,
            @RequestBody(required = false) FilterRequestDto filters
    ) {
        try {
            Map<String, Object> users = userRoleService.listRoles(UserContext.getDocumentId(), filters, pageable);

            return success("Users roles fetched successfully", users);

        } catch (Exception e) {
            return internalServerError("Unable to fetch user list: " + e.getMessage());
        }
    }

    @Operation(summary = "Fetch User Roles Details along with modules and permissions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched the User Roles Details"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{userRolePoid}")
    public ResponseEntity<?> getRolePermissionsGrouping(@Parameter(
                                                                description = "Document identifier",
                                                                example = "233",
                                                                required = true)
                                                        @PathVariable Long userRolePoid) {


        UserRoleRightsDetDto userRoleRightsDetDto = userPermissionService.getUserRoleRightsDetByRolePoid(userRolePoid);
        return success("Successfully fetched the User Roles Details", userRoleRightsDetDto);
    }

    @Operation(
            summary = "Update existing User Role by userRolePoid",
            description = """
                    Update an existing User Role based on the request payload.
                    
                    ### Path Parameter
                    - **userRolePoid:** Unique identifier (Primary Key) of the User Role to update.
                    
                    ### Request Body
                    - **userRoleId:** . Only uppercase letters and underscores, no spaces.
                    - **userRoleName:** Mandatory.
                    - **userRoleName2:** Optional.
                    - **groupPoid:** Mandatory.  
                    - **active:** Mandatory.  
                    - **companyPoid:** Optional.
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "User Role object to update.",
            content = @Content(
                    schema = @Schema(implementation = RoleEntity.class),
                    examples = {
                            @ExampleObject(
                                    name = "Update User Role Example",
                                    value = """
                                            {
                                              "userRoleId": "ACC_BANKS",
                                              "userRoleName": "Account Banks Users Updated",
                                              "userRoleName2": "Banking Module Role",
                                              "groupPoid": 1,
                                              "active": "Y",
                                              "companyPoid": 2001
                                            }
                                            """
                            )
                    }
            )
    )
    @PutMapping("/{userRolePoid}")
    public ResponseEntity<?> updateUserRoleByRolePoid(@PathVariable Long userRolePoid,
                                                      @Valid @RequestBody UserRoleRequestDto userRoleRequestDto) {
        UserRolesDto UserRolesDto = userRoleService.updateUserRoleByUserRolePoId(userRolePoid, userRoleRequestDto);
        return success("User Role Updated successfully", UserRolesDto);
    }

    @Operation(
            summary = "Soft Delete User Role",
            description = """
                    Soft deletes a user role based on the provided parameters.
                    
                    ### Request Parameters
                        - **userRolePoid:** User Role's Primary Key
                    """
    )
    @DeleteMapping("/{userRolePoid}")
    public ResponseEntity<?> softDeleteUserRole(
            @PathVariable Long userRolePoid) {
        try {
            userRoleService.softDeleteUserRole(userRolePoid);
            return success("User Role soft deleted successfully", Map.of("userRolePoid", userRolePoid));
        } catch (Exception e) {
            return internalServerError("Failed to soft delete user role: " + e.getMessage());
        }
    }
    // CHECK ROLE ID EXISTS
    // CHECK ROLE ID EXISTS
    @GetMapping("/role-id-exists")
    public ResponseEntity<?> isRoleIdExists(
            @Parameter(description = "User Role ID to check", required = true)
            @RequestParam String userRoleId,

            @Parameter(description = "User Role Poid for excluding current record", required = false)
            @RequestParam(required = false) Long userRolePoid) {

        if (StringUtils.isBlank(userRoleId)) {
            return success("User role does not exists by userRoleId", false);
        }

        boolean isExists = userRoleService.existsByRoleId(userRoleId, userRolePoid);

        if (isExists) {
            return success("User role exists by userRoleId", true);
        }

        return success("User role does not exists by userRoleId", false);
    }


    // CHECK ROLE NAME EXISTS
    @GetMapping("/role-name-exists")
    public ResponseEntity<?> isRoleNameExists(
            @Parameter(description = "User Role Name to check", required = true)
            @RequestParam String userRoleName,

            @Parameter(description = "User Role Poid for excluding current record", required = false)
            @RequestParam(required = false) Long userRolePoid) {

        if (StringUtils.isBlank(userRoleName)) {
            return success("User role does not exists by userRoleName", false);
        }

        boolean isExists = userRoleService.existsByRoleName(userRoleName, userRolePoid);

        if (isExists) {
            return success("User role exists by userRoleName", true);
        }

        return success("User role does not exists by userRoleName", false);
    }

}
