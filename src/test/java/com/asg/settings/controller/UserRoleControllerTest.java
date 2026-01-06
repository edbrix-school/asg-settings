package com.asg.settings.controller;

import com.asg.common.lib.dto.UserRoleRightsDetDto;
import com.asg.common.lib.dto.UserRoleRightsDto;
import com.asg.common.lib.dto.UserRolesDto;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.settings.dto.RightUpdateEntry;
import com.asg.settings.dto.RolePermissionError;
import com.asg.settings.dto.UserRoleRequestDto;
import com.asg.settings.dto.request.LoadDefaultRightsRequest;
import com.asg.settings.dto.request.RightsUpdateRequest;
import com.asg.settings.dto.request.RolePermissionEntry;
import com.asg.settings.dto.request.RolePermissionRequest;
import com.asg.settings.dto.response.RolePermissionResponse;
import com.asg.settings.exceptions.GlobalExceptionHandler;
import com.asg.settings.service.RolePermissionService;
import com.asg.settings.service.UserRoleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserRoleControllerTest {

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private RolePermissionService rolePermissionService;

    @InjectMocks
    private UserRoleController userRoleController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userRoleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void getRolePermissions_ShouldReturnPermissions() throws Exception {
        UserRoleRightsDto dto = new UserRoleRightsDto();
        dto.setUserRolePoid(1L);
        dto.setDetRowId(10L);
        dto.setDocId("DOC01");
        dto.setRights("READ");
        dto.setCreatedBy("Admin");
        dto.setCreatedDate(LocalDateTime.now());

        when(rolePermissionService.getUserRoleRights(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/v1/user-roles/1/permissions")
                        .param("documentId", "000-019")
                        .param("actionRequested", "LIST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Permissions fetched successfully"));
    }

    @Test
    void getRolePermissions_ShouldReturnEmptyList() throws Exception {
        when(rolePermissionService.getUserRoleRights(1L)).thenReturn(List.of());

        mockMvc.perform(get("/v1/user-roles/1/permissions")
                        .param("documentId", "000-019")
                        .param("actionRequested", "LIST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Permissions fetched successfully"));
    }

    @Test
    void getRolePermissions_ShouldReturn500_WhenServiceThrows() throws Exception {
        when(rolePermissionService.getUserRoleRights(1L)).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/v1/user-roles/1/permissions")
                        .param("documentId", "000-019")
                        .param("actionRequested", "LIST"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void addPermissions_ShouldReturnSuccess() throws Exception {
        RolePermissionEntry entry = new RolePermissionEntry();
        entry.setDetRowId(1L);
        entry.setDocId("DOC01");
        entry.setRights("111000");
        entry.setCreatedBy("admin");
        entry.setLastModifiedBy("admin");

        RolePermissionRequest request = new RolePermissionRequest();
        request.setRoleId(1L);
        request.setPermissions(List.of(entry));

        RolePermissionResponse response = new RolePermissionResponse("SUCCESS", "Permissions added successfully", List.of());

        when(rolePermissionService.addPermissions(any(RolePermissionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/v1/user-roles/permissions")
                        .param("documentId", "000-019")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Successfully added permissions"));
    }

    @Test
    void addPermissions_ShouldReturnConflict_WhenDuplicateEntry() throws Exception {
        RolePermissionEntry entry = new RolePermissionEntry();
        entry.setDetRowId(1L);
        entry.setDocId("DOC01");
        entry.setRights("111000");
        entry.setCreatedBy("admin");
        entry.setLastModifiedBy("admin");

        RolePermissionRequest request = new RolePermissionRequest();
        request.setRoleId(1L);
        request.setPermissions(List.of(entry));

        RolePermissionResponse response = new RolePermissionResponse("FAILURE", "Duplicate entries found",
                List.of(new RolePermissionError("DOC01", "Duplicate entry for docId")));

        when(rolePermissionService.addPermissions(any(RolePermissionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/v1/user-roles/permissions")
                        .param("documentId", "000-019")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void addPermissions_ShouldReturn500_WhenOtherFailure() throws Exception {
        RolePermissionEntry entry = new RolePermissionEntry();
        entry.setDetRowId(1L);
        entry.setDocId("DOC01");
        entry.setRights("111000");
        entry.setCreatedBy("admin");
        entry.setLastModifiedBy("admin");

        RolePermissionRequest request = new RolePermissionRequest();
        request.setRoleId(1L);
        request.setPermissions(List.of(entry));

        RolePermissionResponse response = new RolePermissionResponse("FAILURE", "Database error",
                List.of(new RolePermissionError("DOC01", "Database connection failed")));

        when(rolePermissionService.addPermissions(any(RolePermissionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/v1/user-roles/permissions")
                        .param("documentId", "000-019")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void addPermissions_ShouldReturn400_WhenInvalidRequest() throws Exception {
        mockMvc.perform(post("/v1/user-roles/permissions")
                        .param("documentId", "000-019")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updatePermissions_ShouldReturnSuccess() throws Exception {
        RightUpdateEntry entry = new RightUpdateEntry();
        entry.setDetRowId(1L);
        entry.setDocId("DOC01");
        entry.setRights("111000");
        entry.setLastModifiedBy("admin");

        RightsUpdateRequest request = new RightsUpdateRequest();
        request.setRightsUpdateList(List.of(entry));

        RolePermissionResponse response = new RolePermissionResponse("SUCCESS", "Updated successfully", List.of());

        lenient().when(rolePermissionService.updatePermissions(eq(1L), any(RightsUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/v1/user-roles/1/rights")
                        .param("documentId", "000-019")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User Role Permissions Updated successfully"));
    }

    @Test
    void updatePermissions_ShouldReturn500_WhenServiceThrows() throws Exception {
        RightUpdateEntry entry = new RightUpdateEntry();
        entry.setDetRowId(1L);
        entry.setDocId("DOC01");
        entry.setRights("111000");
        entry.setLastModifiedBy("admin");

        RightsUpdateRequest request = new RightsUpdateRequest();
        request.setRightsUpdateList(List.of(entry));

        lenient().when(rolePermissionService.updatePermissions(eq(1L), any(RightsUpdateRequest.class)))
                .thenThrow(new RuntimeException("Update failed"));

        mockMvc.perform(put("/v1/user-roles/1/rights")
                        .param("documentId", "000-019")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void loadDefaultRights_ShouldReturnSuccess() throws Exception {
        LoadDefaultRightsRequest request = new LoadDefaultRightsRequest();
        request.setLoginUserPoid(1L);
        request.setUserRolePoid(2L);

        when(rolePermissionService.loadDefaultRights(1L, 2L)).thenReturn("SUCCESS");

        mockMvc.perform(post("/v1/user-roles/load-default-rights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Default rights loaded successfully"));
    }

    @Test
    void loadDefaultRights_ShouldReturnBadRequest_WhenMissingLoginUserPoid() throws Exception {
        LoadDefaultRightsRequest request = new LoadDefaultRightsRequest();
        request.setUserRolePoid(2L);

        mockMvc.perform(post("/v1/user-roles/load-default-rights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void loadDefaultRights_ShouldReturnBadRequest_WhenMissingUserRolePoid() throws Exception {
        LoadDefaultRightsRequest request = new LoadDefaultRightsRequest();
        request.setLoginUserPoid(1L);

        mockMvc.perform(post("/v1/user-roles/load-default-rights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void loadDefaultRights_ShouldReturn500_WhenServiceReturnsError() throws Exception {
        LoadDefaultRightsRequest request = new LoadDefaultRightsRequest();
        request.setLoginUserPoid(1L);
        request.setUserRolePoid(2L);

        when(rolePermissionService.loadDefaultRights(1L, 2L)).thenReturn("ERROR: Database connection failed");

        mockMvc.perform(post("/v1/user-roles/load-default-rights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void loadDefaultRights_ShouldReturn500_WhenServiceReturnsNull() throws Exception {
        LoadDefaultRightsRequest request = new LoadDefaultRightsRequest();
        request.setLoginUserPoid(1L);
        request.setUserRolePoid(2L);

        when(rolePermissionService.loadDefaultRights(1L, 2L)).thenReturn(null);

        mockMvc.perform(post("/v1/user-roles/load-default-rights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void addUserRoles_ShouldReturnSuccess() throws Exception {
        UserRoleRequestDto request = new UserRoleRequestDto();
        request.setUserRoleId("ACC_BANKS");
        request.setUserRoleName("Account Banks Users");
        request.setCompanyPoid(1L);
        request.setGroupPoid(1L);
        request.setActive("Y");

        UserRolesDto response = new UserRolesDto();
        response.setUserRoleId("ACC_BANKS");
        response.setUserRoleName("Account Banks Users");

        when(userRoleService.addUserRoles(any(UserRoleRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/v1/user-roles")
                        .param("documentId", "000-019")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User Role created successfully"));
    }

    @Test
    void addUserRoles_ShouldReturn400_WhenInvalidRequest() throws Exception {
        mockMvc.perform(post("/v1/user-roles")
                        .param("documentId", "000-019")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void addUserRoles_ShouldReturn500_WhenServiceThrows() throws Exception {
        UserRoleRequestDto request = new UserRoleRequestDto();
        request.setUserRoleId("ACC_BANKS");
        request.setUserRoleName("Account Banks Users");
        request.setCompanyPoid(1L);
        request.setGroupPoid(1L);
        request.setActive("Y");

        when(userRoleService.addUserRoles(any(UserRoleRequestDto.class)))
                .thenThrow(new RuntimeException("Role creation failed"));

        mockMvc.perform(post("/v1/user-roles")
                        .param("documentId", "000-019")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getUserRoles_ShouldReturnSuccess() throws Exception {
        Map<String, Object> response = Map.of("content", List.of(), "totalElements", 0);

        when(userRoleService.listRoles(eq("000-019"), any(), any(Pageable.class))).thenReturn(response);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getDocumentId).thenReturn("000-019");
            
            mockMvc.perform(post("/v1/user-roles/list")
                            .param("documentId", "000-019")
                            .param("actionRequested", "VIEW")
                            .param("page", "0")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Users roles fetched successfully"));
        }
    }

    @Test
    void getUserRoles_ShouldReturn500_WhenServiceThrows() throws Exception {
        when(userRoleService.listRoles(eq("000-019"), any(), any(Pageable.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/v1/user-roles/list")
                        .param("documentId", "000-019")
                        .param("actionRequested", "VIEW")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getRolePermissionsGrouping_ShouldReturnSuccess() throws Exception {
        UserRoleRightsDetDto response = new UserRoleRightsDetDto();

        when(rolePermissionService.getUserRoleRightsDetByRolePoid(1L)).thenReturn(response);

        mockMvc.perform(get("/v1/user-roles/1")
                        .param("documentId", "000-019")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Successfully fetched the User Roles Details"));
    }

    @Test
    void getRolePermissionsGrouping_ShouldReturn500_WhenServiceThrows() throws Exception {
        when(rolePermissionService.getUserRoleRightsDetByRolePoid(1L))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/v1/user-roles/1")
                        .param("documentId", "000-019")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateUserRoleByRolePoid_ShouldReturnSuccess() throws Exception {
        UserRoleRequestDto request = new UserRoleRequestDto();
        request.setUserRoleId("ACC_BANKS");
        request.setUserRoleName("Account Banks Users Updated");
        request.setCompanyPoid(1L);
        request.setGroupPoid(1L);
        request.setActive("Y");

        UserRolesDto response = new UserRolesDto();
        response.setUserRoleId("ACC_BANKS");
        response.setUserRoleName("Account Banks Users Updated");

        when(userRoleService.updateUserRoleByUserRolePoId(eq(1L), any(UserRoleRequestDto.class))).thenReturn(response);

        mockMvc.perform(put("/v1/user-roles/1")
                        .param("documentId", "000-019")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User Role Updated successfully"));
    }

    @Test
    void updateUserRoleByRolePoid_ShouldReturn400_WhenInvalidRequest() throws Exception {
        mockMvc.perform(put("/v1/user-roles/1")
                        .param("documentId", "000-019")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateUserRoleByRolePoid_ShouldReturn500_WhenServiceThrows() throws Exception {
        UserRoleRequestDto request = new UserRoleRequestDto();
        request.setUserRoleId("ACC_BANKS");
        request.setUserRoleName("Account Banks Users Updated");
        request.setCompanyPoid(1L);
        request.setGroupPoid(1L);
        request.setActive("Y");

        when(userRoleService.updateUserRoleByUserRolePoId(eq(1L), any(UserRoleRequestDto.class)))
                .thenThrow(new RuntimeException("Update failed"));

        mockMvc.perform(put("/v1/user-roles/1")
                        .param("documentId", "000-019")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testSoftDeleteUserRole_Success() throws Exception {
        doNothing().when(userRoleService).softDeleteUserRole(1L);

        mockMvc.perform(delete("/v1/user-roles/1")
                        .param("documentId", "000-004")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User Role soft deleted successfully"));

        verify(userRoleService).softDeleteUserRole(1L);
    }

    @Test
    void testSoftDeleteUserRole_ResourceNotFoundException() throws Exception {
        doThrow(new ResourceNotFoundException("User Role", "userRolePoid", 1L))
                .when(userRoleService).softDeleteUserRole(1L);

        mockMvc.perform(delete("/v1/user-roles/1")
                        .param("documentId", "000-004")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));

        verify(userRoleService).softDeleteUserRole(1L);
    }

    @Test
    void testSoftDeleteUserRole_ServiceException() throws Exception {
        doThrow(new RuntimeException("Database error"))
                .when(userRoleService).softDeleteUserRole(1L);

        mockMvc.perform(delete("/v1/user-roles/1")
                        .param("documentId", "000-004")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to soft delete user role: Database error"));

        verify(userRoleService).softDeleteUserRole(1L);
    }

    @Test
    void testSoftDeleteUserRole_MissingDocumentId() throws Exception {
        doNothing().when(userRoleService).softDeleteUserRole(1L);
        
        mockMvc.perform(delete("/v1/user-roles/1")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User Role soft deleted successfully"));
    }

    @Test
    void testSoftDeleteUserRole_MissingActionRequested() throws Exception {
        doNothing().when(userRoleService).softDeleteUserRole(1L);
        
        mockMvc.perform(delete("/v1/user-roles/1")
                        .param("documentId", "000-004"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User Role soft deleted successfully"));
    }

    @Test
    void testSoftDeleteUserRole_InvalidUserRolePoid() throws Exception {
        mockMvc.perform(delete("/v1/user-roles/invalid")
                        .param("documentId", "000-004")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isBadRequest());
    }
}
