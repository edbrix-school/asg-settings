package com.asg.settings.controller;

//import com.asg.dto.CreateCompanyRequest;
//import com.asg.dto.UpdateCompanyRequest;
//import com.asg.dto.masters.CompanyDto;
//import com.asg.dto.masters.FilterRequestDto;
//import com.asg.entity.Company;
//import com.asg.exceptions.GlobalExceptionHandler;
//import com.asg.exceptions.ResourceNotFoundException;
//import com.asg.security.exception.ValidationException;
//import com.asg.service.CompanyService;
import com.asg.common.lib.dto.CompanyDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.entity.Company;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.settings.dto.request.CreateCompanyRequest;
import com.asg.settings.dto.request.UpdateCompanyRequest;
import com.asg.settings.exceptions.GlobalExceptionHandler;
import com.asg.settings.service.CompanyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CompanyControllerTest {

    @Mock
    private CompanyService companyService;

    @InjectMocks
    private CompanyController companyController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Company company;
    private CompanyDto companyDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(companyController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();

        company = createValidCompany();
        companyDto = new CompanyDto();
        companyDto.setCompanyPoid(1L);
        companyDto.setCompanyCode("TEST");
        companyDto.setCompanyName("Test Company");
    }

    private Company createValidCompany() {
        Company company = new Company();
        company.setCompanyPoid(1L);
        company.setCompanyCode("TEST");
        company.setCompanyName("Test Company");
        company.setEmail("test@company.com");
        company.setContactPerson("John Doe");
        company.setFinancialPeriodStart(Date.valueOf("2024-01-01"));
        company.setFinancialPeriodEnd(Date.valueOf("2024-12-31"));
        company.setReportPeriodStart(Date.valueOf("2024-01-01"));
        company.setReportPeriodEnd(Date.valueOf("2024-12-31"));
        company.setTransPeriodStart(Date.valueOf("2024-01-01"));
        company.setTransPeriodEnd(Date.valueOf("2024-12-31"));
        company.setSubmissionPeriod(30L);
        return company;
    }

    @Test
    void getCompanyList_Success() throws Exception {
        FilterRequestDto filters = new FilterRequestDto("OR", "N", null);
        Map<String, Object> companies = new HashMap<>();
        companies.put("content", new Object[]{});
        when(companyService.listCompanies(anyString(), any(), any())).thenReturn(companies);

        mockMvc.perform(post("/api/v1/companies/list")
                        .param("documentId", "000-002")
                        .param("actionRequested", "VIEW")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filters)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Companies list fetched successfully"));

        verify(companyService).listCompanies(anyString(), any(), any());
    }

    @Test
    void getCompanyList_ServiceException() throws Exception {
        when(companyService.listCompanies(anyString(), any(), any())).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/api/v1/companies/list")
                        .param("documentId", "000-002")
                        .param("actionRequested", "VIEW")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to list company: Service error"));
    }

    @Test
    void getCompanyDetails_Success() throws Exception {
        when(companyService.getCompany(1L)).thenReturn(companyDto);

        mockMvc.perform(get("/api/v1/companies/details")
                        .param("companyPoid", "1")
                        .param("documentId", "000-002")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Company details fetched successfully"))
                .andExpect(jsonPath("$.result.data.companyPoid").value(1));

        verify(companyService).getCompany(1L);
    }

    @Test
    void getCompanyDetails_ServiceException() throws Exception {
        when(companyService.getCompany(1L)).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/v1/companies/details")
                        .param("companyPoid", "1")
                        .param("documentId", "000-002")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to fetch company: Service error"));
    }

    @Test
    void createCompany_Success() throws Exception {
        CreateCompanyRequest request = new CreateCompanyRequest();
        request.setCompanyCode("TEST");
        request.setCompanyName("Test Company");
        request.setEmail("test@company.com");
        request.setContactPerson("John Doe");
        when(companyService.createCompany(any(CreateCompanyRequest.class))).thenReturn("1");

        mockMvc.perform(post("/api/v1/companies/create")
                        .param("documentId", "000-002")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Company created successfully"))
                .andExpect(jsonPath("$.result.data.companyPoid").value("1"));

        verify(companyService).createCompany(any(CreateCompanyRequest.class));
    }

    @Test
    void createCompany_ValidationException() throws Exception {
        CreateCompanyRequest request = new CreateCompanyRequest();
        request.setCompanyCode("TEST");
        request.setCompanyName("Test Company");
        when(companyService.createCompany(any(CreateCompanyRequest.class)))
                .thenThrow(new ValidationException("Company name is required"));

        mockMvc.perform(post("/api/v1/companies/create")
                        .param("documentId", "000-002")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createCompany_ServiceException() throws Exception {
        CreateCompanyRequest request = new CreateCompanyRequest();
        request.setCompanyCode("TEST");
        request.setCompanyName("Test Company");
        when(companyService.createCompany(any(CreateCompanyRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/api/v1/companies/create")
                        .param("documentId", "000-002")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to create company: Database error"));
    }

    @Test
    void updateCompany_Success() throws Exception {
        UpdateCompanyRequest request = new UpdateCompanyRequest();
        request.setCompanyPoid(1L);
        request.setCompanyCode("TEST");
        request.setCompanyName("Test Company");
        request.setEmail("test@company.com");
        request.setContactPerson("John Doe");
        when(companyService.updateCompany(any(UpdateCompanyRequest.class))).thenReturn("1");

        mockMvc.perform(post("/api/v1/companies/update")
                        .param("documentId", "000-002")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Company updated successfully"))
                .andExpect(jsonPath("$.result.data.companyPoid").value("1"));

        verify(companyService).updateCompany(any(UpdateCompanyRequest.class));
    }

    @Test
    void updateCompany_ValidationException() throws Exception {
        UpdateCompanyRequest request = new UpdateCompanyRequest();
        request.setCompanyPoid(1L);
        request.setCompanyCode("TEST");
        request.setCompanyName("Test Company");
        when(companyService.updateCompany(any(UpdateCompanyRequest.class)))
                .thenThrow(new ValidationException("Validation error"));

        mockMvc.perform(post("/api/v1/companies/update")
                        .param("documentId", "000-002")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateCompany_ServiceException() throws Exception {
        UpdateCompanyRequest request = new UpdateCompanyRequest();
        request.setCompanyPoid(1L);
        request.setCompanyCode("TEST");
        request.setCompanyName("Test Company");
        when(companyService.updateCompany(any(UpdateCompanyRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/api/v1/companies/update")
                        .param("documentId", "000-002")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to update company: Database error"));
    }

    @Test
    void softDeleteCompany_Success() throws Exception {
        doNothing().when(companyService).softDeleteCompany(1L);

        mockMvc.perform(delete("/api/v1/companies/1")
                        .param("documentId", "000-002")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Company deleted successfully"));

        verify(companyService).softDeleteCompany(1L);
    }

    @Test
    void softDeleteCompany_ResourceNotFoundException() throws Exception {
        doThrow(new ResourceNotFoundException("Company", "companyPoid", 1L))
                .when(companyService).softDeleteCompany(1L);

        mockMvc.perform(delete("/api/v1/companies/1")
                        .param("documentId", "000-002")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        verify(companyService).softDeleteCompany(1L);
    }

    @Test
    void softDeleteCompany_ServiceException() throws Exception {
        doThrow(new RuntimeException("Delete failed"))
                .when(companyService).softDeleteCompany(1L);

        mockMvc.perform(delete("/api/v1/companies/1")
                        .param("documentId", "000-002")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to deactivate Company: Delete failed"));

        verify(companyService).softDeleteCompany(1L);
    }

    // Edge case tests
    @Test
    void getCompanyList_MissingDocumentId() throws Exception {
        mockMvc.perform(post("/api/v1/companies/list")
                        .param("actionRequested", "VIEW")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getCompanyList_MissingActionRequested() throws Exception {
        mockMvc.perform(post("/api/v1/companies/list")
                        .param("documentId", "000-002")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getCompanyList_InvalidJson() throws Exception {
        mockMvc.perform(post("/api/v1/companies/list")
                        .param("documentId", "000-002")
                        .param("actionRequested", "VIEW")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid-json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCompanyList_UnsupportedMediaType() throws Exception {
        mockMvc.perform(post("/api/v1/companies/list")
                        .param("documentId", "000-002")
                        .param("actionRequested", "VIEW")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("{}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getCompanyDetails_MissingCompanyPoid() throws Exception {
        mockMvc.perform(get("/api/v1/companies/details")
                        .param("documentId", "000-002")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getCompanyDetails_InvalidCompanyPoid() throws Exception {
        mockMvc.perform(get("/api/v1/companies/details")
                        .param("companyPoid", "invalid")
                        .param("documentId", "000-002")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCompanyDetails_NegativeCompanyPoid() throws Exception {
        when(companyService.getCompany(-1L)).thenReturn(companyDto);

        mockMvc.perform(get("/api/v1/companies/details")
                        .param("companyPoid", "-1")
                        .param("documentId", "000-002")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isOk());
    }

    @Test
    void getCompanyDetails_ZeroCompanyPoid() throws Exception {
        when(companyService.getCompany(0L)).thenReturn(companyDto);

        mockMvc.perform(get("/api/v1/companies/details")
                        .param("companyPoid", "0")
                        .param("documentId", "000-002")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isOk());
    }

    @Test
    void createCompany_MissingDocumentId() throws Exception {
        CreateCompanyRequest request = new CreateCompanyRequest();
        request.setCompanyCode("TEST");
        request.setCompanyName("Test Company");

        mockMvc.perform(post("/api/v1/companies/create")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void createCompany_MissingActionRequested() throws Exception {
        CreateCompanyRequest request = new CreateCompanyRequest();
        request.setCompanyCode("TEST");
        request.setCompanyName("Test Company");

        mockMvc.perform(post("/api/v1/companies/create")
                        .param("documentId", "000-002")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void createCompany_EmptyRequestBody() throws Exception {
        mockMvc.perform(post("/api/v1/companies/create")
                        .param("documentId", "000-002")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCompany_NullRequestBody() throws Exception {
        mockMvc.perform(post("/api/v1/companies/create")
                        .param("documentId", "000-002")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCompany_InvalidJson() throws Exception {
        mockMvc.perform(post("/api/v1/companies/create")
                        .param("documentId", "000-002")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid-json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCompany_MissingDocumentId() throws Exception {
        UpdateCompanyRequest request = new UpdateCompanyRequest();
        request.setCompanyPoid(1L);
        request.setCompanyCode("TEST");
        request.setCompanyName("Test Company");

        mockMvc.perform(post("/api/v1/companies/update")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateCompany_MissingActionRequested() throws Exception {
        UpdateCompanyRequest request = new UpdateCompanyRequest();
        request.setCompanyPoid(1L);
        request.setCompanyCode("TEST");
        request.setCompanyName("Test Company");

        mockMvc.perform(post("/api/v1/companies/update")
                        .param("documentId", "000-002")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateCompany_EmptyRequestBody() throws Exception {
        mockMvc.perform(post("/api/v1/companies/update")
                        .param("documentId", "000-002")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void softDeleteCompany_MissingDocumentId() throws Exception {
        mockMvc.perform(delete("/api/v1/companies/1")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void softDeleteCompany_MissingActionRequested() throws Exception {
        mockMvc.perform(delete("/api/v1/companies/1")
                        .param("documentId", "000-002"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void softDeleteCompany_InvalidCompanyPoid() throws Exception {
        mockMvc.perform(delete("/api/v1/companies/invalid")
                        .param("documentId", "000-002")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void softDeleteCompany_NegativeCompanyPoid() throws Exception {
        doNothing().when(companyService).softDeleteCompany(-1L);

        mockMvc.perform(delete("/api/v1/companies/-1")
                        .param("documentId", "000-002")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isOk());
    }

    @Test
    void softDeleteCompany_ZeroCompanyPoid() throws Exception {
        doNothing().when(companyService).softDeleteCompany(0L);

        mockMvc.perform(delete("/api/v1/companies/0")
                        .param("documentId", "000-002")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isOk());
    }
}