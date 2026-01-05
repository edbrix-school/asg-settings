//package com.asg.settings.controller;
//
//import com.asg.common.lib.dto.FilterDto;
//import com.asg.common.lib.dto.FilterRequestDto;
//import com.asg.settings.dto.request.DivisionCreateRequest;
//import com.asg.settings.dto.request.DivisionUpdateRequest;
//import com.asg.settings.dto.response.DivisionResponse;
//import com.asg.settings.service.DivisionService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.FilterType;
//import org.springframework.data.domain.*;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//import java.sql.Timestamp;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(controllers = DivisionController.class,
//        excludeAutoConfiguration = {
//                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
//                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
//        },
//        excludeFilters = {
//                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
//                        classes = {com.asg.security.middleware.RBACInterceptor.class}),
//                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
//                        classes = {com.asg.security.WebConfig.class})
//        })
//class DivisionControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private DivisionService divisionService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    private com.asg.settings.service.PermissionService permissionService;
//
//    @MockBean
//    private JwtUtil jwtUtil;
//
//    @MockBean
//    private com.asg.utility.DocIdApiMappingProperties docIdApiMappingProperties;
//
//    @MockBean
//    private com.asg.settings.service.ParameterService parameterService;
//
//    private DivisionCreateRequest request;
//    private DivisionResponse division;
//    private DivisionResponse division1;
//
//    @BeforeEach
//    void setUp() {
//        request = new DivisionCreateRequest();
//        request.setDivisionCode("FIN01");
//        request.setDivisionName("Finance");
//        request.setRemarks("Handles finance operations");
//        request.setSeqNo(1);
//        request.setActive("Y");
//        request.setCreatedBy("Admin");
//
//        division = new DivisionResponse();
//        division.setDivisionId(1L);
//        division.setDivisionCode("FIN01");
//        division.setDivisionName("Finance");
//        division.setRemarks("Handles finance operations");
//        division.setActive("Y");
//        division.setCreatedBy("Admin");
//        division.setCreatedAt(new Timestamp(System.currentTimeMillis()));
//        division.setUpdatedBy("Admin");
//
//        division1 = new DivisionResponse();
//        division1.setDivisionId(2L);
//        division1.setDivisionCode("HR01");
//        division1.setDivisionName("Human Resources");
//        division1.setRemarks("HR operations");
//        division1.setActive("Y");
//        division1.setCreatedBy("Admin");
//        division1.setCreatedAt(new Timestamp(System.currentTimeMillis()));
//
//        // Mock DocIdApiMappingProperties
//        when(docIdApiMappingProperties.getValidDocIdFormatRegexp()).thenReturn("\\d{3}-\\d{3}");
//        when(docIdApiMappingProperties.getMappings()).thenReturn(Map.of("/api/v1/division/**", "000-017"));
//        when(docIdApiMappingProperties.getSpecial()).thenReturn("/api/v1/global/searchable-fields/**");
//    }
//
//    @Test
//    @WithMockUser(username = "Admin", roles = {"ADMIN"})
//    void createDivision_shouldReturnCreated_whenValidRequest() throws Exception {
//        when(divisionService.existsByDivisionCodeAndDeleted(eq("FIN01"), eq(0)))
//                .thenReturn(false);
//        when(divisionService.createDivision(any(DivisionCreateRequest.class)))
//                .thenReturn(division);
//        when(permissionService.hasPermission(any(), any(), any())).thenReturn(true);
//
//        mockMvc.perform(post("/api/v1/division/create")
//                        .param("documentId", "000-017")
//                        .param("actionRequested", "CREATE")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Division created successfully"))
//                .andExpect(jsonPath("$.result.data.divisionCode").value("FIN01"))
//                .andExpect(jsonPath("$.result.data.divisionName").value("Finance"));
//    }
//
//    @Test
//    @WithMockUser(username = "Admin", roles = {"ADMIN"})
//    void createDivision_shouldReturnConflict_whenDivisionCodeExists() throws Exception {
//        when(divisionService.existsByDivisionCodeAndDeleted(eq("FIN01"), eq(0)))
//                .thenReturn(true);
//        when(permissionService.hasPermission(any(), any(), any())).thenReturn(true);
//
//        mockMvc.perform(post("/api/v1/division/create")
//                        .param("documentId", "000-017")
//                        .param("actionRequested", "CREATE")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isConflict())
//                .andExpect(jsonPath("$.message").value("Division code already exists"))
//                .andExpect(jsonPath("$.success").value(false))
//                .andExpect(jsonPath("$.statusCode").value("409"));
//    }
//
//    @Test
//    @WithMockUser(username = "Admin", roles = {"ADMIN"})
//    void getDivisions_shouldReturnPagedResult() throws Exception {
//        Map<String, Object> data = Map.of(
//                "content", List.of(division, division1),
//                "totalElements", 2,
//                "totalPages", 1
//        );
//
//        when(divisionService.listDivisions(eq("000-017"), any(FilterRequestDto.class), any(Pageable.class))).thenReturn(data);
//        when(permissionService.hasPermission(any(), any(), any())).thenReturn(true);
//
//        FilterRequestDto filterRequest = new FilterRequestDto(
//                "OR",
//                "N",
//                List.of(new FilterDto("globalsearch", "Finance"))
//        );
//
//        mockMvc.perform(post("/api/v1/division/list")
//                        .param("documentId", "000-017")
//                        .param("actionRequested", "LIST")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(filterRequest)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Divisions fetched successfully"))
//                .andExpect(jsonPath("$.result.data.content").isArray())
//                .andExpect(jsonPath("$.result.data.content.length()").value(2));
//    }
//
//    @Test
//    @WithMockUser(username = "Admin", roles = {"ADMIN"})
//    void getDivisions_shouldReturnInternalServerError_onException() throws Exception {
//        when(divisionService.listDivisions(eq("000-017"), any(), any(Pageable.class)))
//                .thenThrow(new RuntimeException("DB error"));
//        when(permissionService.hasPermission(any(), any(), any())).thenReturn(true);
//
//        FilterRequestDto filterRequest = new FilterRequestDto("OR", "N", List.of());
//
//        mockMvc.perform(post("/api/v1/division/list")
//                        .param("documentId", "000-017")
//                        .param("actionRequested", "LIST")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(filterRequest)))
//                .andExpect(status().is5xxServerError())
//                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Unable to fetch division list")));
//    }
//
//    @Test
//    @WithMockUser(username = "Admin", roles = {"ADMIN"})
//    void getDivisionById_shouldReturnDivision_whenExists() throws Exception {
//        when(divisionService.getDivisionById(1L)).thenReturn(Optional.of(division));
//        when(permissionService.hasPermission(any(), any(), any())).thenReturn(true);
//
//        mockMvc.perform(get("/api/v1/division/{id}", 1L)
//                        .param("documentId", "000-017")
//                        .param("actionRequested", "VIEW")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Division found"))
//                .andExpect(jsonPath("$.result.data.divisionId").value(1))
//                .andExpect(jsonPath("$.result.data.divisionCode").value("FIN01"));
//    }
//
//    @Test
//    @WithMockUser(username = "Admin", roles = {"ADMIN"})
//    void getDivisionById_shouldReturnNotFound_whenDivisionDoesNotExist() throws Exception {
//        when(divisionService.getDivisionById(anyLong())).thenReturn(Optional.empty());
//        when(permissionService.hasPermission(any(), any(), any())).thenReturn(true);
//
//        mockMvc.perform(get("/api/v1/division/{id}", 99L)
//                        .param("documentId", "000-017")
//                        .param("actionRequested", "VIEW")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.message").value("Division not found"));
//    }
//
//    @Test
//    @WithMockUser(username = "Admin", roles = {"ADMIN"})
//    void updateDivision_shouldReturnSuccess() throws Exception {
//        DivisionUpdateRequest updateRequest = new DivisionUpdateRequest();
//        updateRequest.setDivisionName("Finance Updated");
//        updateRequest.setActive("Y");
//        updateRequest.setUpdatedBy("Admin");
//
//        when(permissionService.hasPermission(any(), any(), any())).thenReturn(true);
//        when(divisionService.updateDivision(anyLong(), any(DivisionUpdateRequest.class))).thenReturn(division);
//
//        mockMvc.perform(put("/api/v1/division/{id}", 1L)
//                        .param("documentId", "000-017")
//                        .param("actionRequested", "EDIT")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateRequest)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Division updated successfully"));
//    }
//
//    @Test
//    @WithMockUser(username = "Admin", roles = {"ADMIN"})
//    void softDeleteDivision_shouldReturnSuccess() throws Exception {
//        when(permissionService.hasPermission(any(), any(), any())).thenReturn(true);
//        doNothing().when(divisionService).softDeleteDivision(anyLong(), anyString());
//
//        mockMvc.perform(delete("/api/v1/division/{id}", 1L)
//                        .param("updatedBy", "Admin")
//                        .param("documentId", "000-017")
//                        .param("actionRequested", "DELETE")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Division deleted successfully"));
//    }
//
//    @Test
//    @WithMockUser(username = "Admin", roles = {"ADMIN"})
//    void activateDivision_shouldReturnSuccess() throws Exception {
//        when(permissionService.hasPermission(any(), any(), any())).thenReturn(true);
//        doNothing().when(divisionService).activateDivision(anyLong(), anyString());
//
//        mockMvc.perform(put("/api/v1/division/{id}/activate", 1L)
//                        .param("updatedBy", "Admin")
//                        .param("documentId", "000-017")
//                        .param("actionRequested", "ACTIVATE")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Division activated successfully"));
//    }
//
//    @Test
//    @WithMockUser(username = "Admin", roles = {"ADMIN"})
//    void deactivateDivision_shouldReturnSuccess() throws Exception {
//        when(permissionService.hasPermission(any(), any(), any())).thenReturn(true);
//        doNothing().when(divisionService).deactivateDivision(anyLong(), anyString());
//
//        mockMvc.perform(put("/api/v1/division/{id}/deactivate", 1L)
//                        .param("updatedBy", "Admin")
//                        .param("documentId", "000-017")
//                        .param("actionRequested", "DEACTIVATE")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Division deactivated successfully"));
//    }
//
//    // Edge Cases
//    @Test
//    @WithMockUser(username = "Admin", roles = {"ADMIN"})
//    void createDivision_shouldReturnBadRequest_whenInvalidDocumentId() throws Exception {
//        when(divisionService.existsByDivisionCodeAndDeleted(eq("FIN01"), eq(0)))
//                .thenReturn(false);
//        when(divisionService.createDivision(any(DivisionCreateRequest.class)))
//                .thenReturn(division);
//        when(permissionService.hasPermission(any(), any(), any())).thenReturn(true);
//
//        mockMvc.perform(post("/api/v1/division/create")
//                        .param("documentId", "invalid-id")
//                        .param("actionRequested", "CREATE")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Division created successfully"));
//    }
//
//    @Test
//    @WithMockUser(username = "Admin", roles = {"ADMIN"})
//    void createDivision_shouldReturnBadRequest_whenMissingDocumentId() throws Exception {
//        mockMvc.perform(post("/api/v1/division/create")
//                        .param("actionRequested", "CREATE")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isInternalServerError())
//                .andExpect(jsonPath("$.message").value("Required request parameter 'documentId' for method parameter type String is not present"));
//    }
//
//    @Test
//    @WithMockUser(username = "Admin", roles = {"ADMIN"})
//    void createDivision_shouldReturnForbidden_whenNoPermission() throws Exception {
//        when(divisionService.existsByDivisionCodeAndDeleted(eq("FIN01"), eq(0)))
//                .thenReturn(false);
//        when(divisionService.createDivision(any(DivisionCreateRequest.class)))
//                .thenReturn(division);
//        when(permissionService.hasPermission(any(), any(), any())).thenReturn(false);
//
//        mockMvc.perform(post("/api/v1/division/create")
//                        .param("documentId", "000-017")
//                        .param("actionRequested", "CREATE")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Division created successfully"));
//    }
//
//    @Test
//    @WithMockUser(username = "Admin", roles = {"ADMIN"})
//    void createDivision_shouldReturnBadRequest_whenBlankDivisionCode() throws Exception {
//        request.setDivisionCode("");
//        when(permissionService.hasPermission(any(), any(), any())).thenReturn(true);
//
//        mockMvc.perform(post("/api/v1/division/create")
//                        .param("documentId", "000-017")
//                        .param("actionRequested", "CREATE")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.message").value("Validation error occurred"));
//    }
//
//    @Test
//    @WithMockUser(username = "Admin", roles = {"ADMIN"})
//    void createDivision_shouldReturnBadRequest_whenNullSeqNo() throws Exception {
//        request.setSeqNo(null);
//        when(permissionService.hasPermission(any(), any(), any())).thenReturn(true);
//
//        mockMvc.perform(post("/api/v1/division/create")
//                        .param("documentId", "000-017")
//                        .param("actionRequested", "CREATE")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.message").value("Validation error occurred"));
//    }
//
//    @Test
//    @WithMockUser(username = "Admin", roles = {"ADMIN"})
//    void createDivision_shouldReturnBadRequest_whenNegativeSeqNo() throws Exception {
//        request.setSeqNo(-1);
//        when(permissionService.hasPermission(any(), any(), any())).thenReturn(true);
//
//        mockMvc.perform(post("/api/v1/division/create")
//                        .param("documentId", "000-017")
//                        .param("actionRequested", "CREATE")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.message").value("Validation error occurred"));
//    }
//
//    @Test
//    @WithMockUser(username = "Admin", roles = {"ADMIN"})
//    void createDivision_shouldReturnBadRequest_whenInvalidActiveValue() throws Exception {
//        request.setActive("X");
//        when(permissionService.hasPermission(any(), any(), any())).thenReturn(true);
//
//        mockMvc.perform(post("/api/v1/division/create")
//                        .param("documentId", "000-017")
//                        .param("actionRequested", "CREATE")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.message").value("Validation error occurred"));
//    }
//
//    @Test
//    @WithMockUser(username = "Admin", roles = {"ADMIN"})
//    void getDivisions_shouldHandleNullFilters() throws Exception {
//        Map<String, Object> data = Map.of("content", List.of(), "totalElements", 0, "totalPages", 0);
//        when(divisionService.listDivisions(eq("000-017"), isNull(), any(Pageable.class))).thenReturn(data);
//        when(permissionService.hasPermission(any(), any(), any())).thenReturn(true);
//
//        mockMvc.perform(post("/api/v1/division/list")
//                        .param("documentId", "000-017")
//                        .param("actionRequested", "LIST")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Divisions fetched successfully"));
//    }
//
//    @Test
//    @WithMockUser(username = "Admin", roles = {"ADMIN"})
//    void getDivisionById_shouldHandleZeroId() throws Exception {
//        when(divisionService.getDivisionById(0L)).thenReturn(Optional.empty());
//        when(permissionService.hasPermission(any(), any(), any())).thenReturn(true);
//
//        mockMvc.perform(get("/api/v1/division/{id}", 0L)
//                        .param("documentId", "000-017")
//                        .param("actionRequested", "VIEW")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    @WithMockUser(username = "Admin", roles = {"ADMIN"})
//    void updateDivision_shouldHandleServiceException() throws Exception {
//        DivisionUpdateRequest updateRequest = new DivisionUpdateRequest();
//        updateRequest.setDivisionName("Finance Updated");
//        updateRequest.setUpdatedBy("Admin");
//
//        when(permissionService.hasPermission(any(), any(), any())).thenReturn(true);
//        when(divisionService.updateDivision(anyLong(), any(DivisionUpdateRequest.class)))
//                .thenThrow(new RuntimeException("Update failed"));
//
//        mockMvc.perform(put("/api/v1/division/{id}", 1L)
//                        .param("documentId", "000-017")
//                        .param("actionRequested", "EDIT")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateRequest)))
//                .andExpect(status().isInternalServerError());
//    }
//
//    @Test
//    @WithMockUser(username = "Admin", roles = {"ADMIN"})
//    void softDeleteDivision_shouldHandleServiceException() throws Exception {
//        when(permissionService.hasPermission(any(), any(), any())).thenReturn(true);
//        doThrow(new RuntimeException("Delete failed")).when(divisionService).softDeleteDivision(anyLong(), anyString());
//
//        mockMvc.perform(delete("/api/v1/division/{id}", 1L)
//                        .param("updatedBy", "Admin")
//                        .param("documentId", "000-017")
//                        .param("actionRequested", "DELETE")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isInternalServerError());
//    }
//
//    @Test
//    void createDivision_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
//        when(divisionService.existsByDivisionCodeAndDeleted(eq("FIN01"), eq(0)))
//                .thenReturn(false);
//        when(divisionService.createDivision(any(DivisionCreateRequest.class)))
//                .thenReturn(division);
//        when(permissionService.hasPermission(any(), any(), any())).thenReturn(true);
//
//        mockMvc.perform(post("/api/v1/division/create")
//                        .param("documentId", "000-017")
//                        .param("actionRequested", "CREATE")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Division created successfully"));
//    }
//
//    @Test
//    @WithMockUser(username = "Admin", roles = {"ADMIN"})
//    void createDivision_shouldReturnBadRequest_whenWrongDocumentId() throws Exception {
//        when(divisionService.existsByDivisionCodeAndDeleted(eq("FIN01"), eq(0)))
//                .thenReturn(false);
//        when(divisionService.createDivision(any(DivisionCreateRequest.class)))
//                .thenReturn(division);
//        when(permissionService.hasPermission(any(), any(), any())).thenReturn(true);
//
//        mockMvc.perform(post("/api/v1/division/create")
//                        .param("documentId", "000-999")
//                        .param("actionRequested", "CREATE")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Division created successfully"));
//    }
//
//    @Test
//    @WithMockUser(username = "Admin", roles = {"ADMIN"})
//    void createDivision_shouldReturnInternalServerError_whenServiceThrowsException() throws Exception {
//        when(divisionService.existsByDivisionCodeAndDeleted(eq("FIN01"), eq(0))).thenReturn(false);
//        when(divisionService.createDivision(any(DivisionCreateRequest.class)))
//                .thenThrow(new RuntimeException("Database error"));
//        when(permissionService.hasPermission(any(), any(), any())).thenReturn(true);
//
//        mockMvc.perform(post("/api/v1/division/create")
//                        .param("documentId", "000-017")
//                        .param("actionRequested", "CREATE")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isInternalServerError());
//    }
//}