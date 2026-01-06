package com.asg.settings.controller;

import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.entity.CurrencyEntity;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.settings.dto.CurrencyRateDto;
import com.asg.settings.dto.request.CurrencyCreateRequest;
import com.asg.settings.exceptions.GlobalExceptionHandler;
import com.asg.settings.repository.CurrencyCreateRepository;
import com.asg.settings.service.CurrencyService;
import com.asg.settings.service.CurrencyUploadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CurrencyControllerTest {

    @Mock
    private CurrencyService currencyService;

    @Mock
    private CurrencyUploadService uploadService;

    @Mock
    private CurrencyCreateRepository currencyCreateRepository;

    @InjectMocks
    private CurrencyController currencyController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(currencyController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCurrencyUpload_fromFile_returns200() throws Exception {
        when(uploadService.uploadCurrencyRates(any(), any(), any(), any())).thenReturn("Success");

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "test content".getBytes()
        );

        mockMvc.perform(multipart("/v1/currency/upload-excel")
                        .file(mockFile)
                        .param("groupPoid", "1014")
                        .param("companyPoid", "2001")
                        .param("userPoid", "3001")
                        .param("documentId", "000-006")
                        .param("actionRequested", "CREATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testUpdateRate_withValidRequest_returns200() throws Exception {
        when(uploadService.updateCurrencyRates(any())).thenReturn("Success");

        String jsonBody = """
        {
            "groupPOID": 1,
            "currencyCode": "USD",
            "rateChangeDate": "2025-07-15",
            "buyRate": 83.2,
            "sellRate": 84.0
        }
        """;

        mockMvc.perform(post("/v1/currency/update-rate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .param("documentId", "000-006")
                        .param("actionRequested", "EDIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testCurrencyUpload_errorResponse_returns500() throws Exception {
        lenient().when(uploadService.uploadCurrencyRates(any(), any(), any(), any())).thenReturn("ERROR: Invalid file format");

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "test content".getBytes()
        );

        mockMvc.perform(multipart("/v1/currency/upload-excel")
                        .file(mockFile)
                        .param("groupPoid", "1014")
                        .param("companyPoid", "2001")
                        .param("userPoid", "3001")
                        .param("documentId", "000-006")
                        .param("actionRequested", "CREATE"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testCurrencyUpload_nullResponse_returns500() throws Exception {
        lenient().when(uploadService.uploadCurrencyRates(any(), any(), any(), any())).thenReturn(null);

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "test content".getBytes()
        );

        mockMvc.perform(multipart("/v1/currency/upload-excel")
                        .file(mockFile)
                        .param("groupPoid", "1014")
                        .param("companyPoid", "2001")
                        .param("userPoid", "3001")
                        .param("documentId", "000-006")
                        .param("actionRequested", "CREATE"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testCurrencyUpload_exception_returns500() throws Exception {
        lenient().when(uploadService.uploadCurrencyRates(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Database connection failed"));

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "test content".getBytes()
        );

        mockMvc.perform(multipart("/v1/currency/upload-excel")
                        .file(mockFile)
                        .param("groupPoid", "1014")
                        .param("companyPoid", "2001")
                        .param("userPoid", "3001")
                        .param("documentId", "000-006")
                        .param("actionRequested", "CREATE"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testUpdateRate_errorResponse_returns500() throws Exception {
        lenient().when(uploadService.updateCurrencyRates(any())).thenReturn("ERROR: Invalid currency code");

        String jsonBody = """
        {
            "groupPOID": 1,
            "currencyCode": "INVALID",
            "rateChangeDate": "2025-07-15",
            "buyRate": 83.2,
            "sellRate": 84.0
        }
        """;

        mockMvc.perform(post("/v1/currency/update-rate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .param("documentId", "000-006")
                        .param("actionRequested", "EDIT"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testUpdateRate_nullResponse_returns500() throws Exception {
        lenient().when(uploadService.updateCurrencyRates(any())).thenReturn(null);

        String jsonBody = """
        {
            "groupPOID": 1,
            "currencyCode": "USD",
            "rateChangeDate": "2025-07-15",
            "buyRate": 83.2,
            "sellRate": 84.0
        }
        """;

        mockMvc.perform(post("/v1/currency/update-rate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .param("documentId", "000-006")
                        .param("actionRequested", "EDIT"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testUpdateRate_exception_returns500() throws Exception {
        lenient().when(uploadService.updateCurrencyRates(any()))
                .thenThrow(new RuntimeException("Service unavailable"));

        String jsonBody = """
        {
            "groupPOID": 1,
            "currencyCode": "USD",
            "rateChangeDate": "2025-07-15",
            "buyRate": 83.2,
            "sellRate": 84.0
        }
        """;

        mockMvc.perform(post("/v1/currency/update-rate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .param("documentId", "000-006")
                        .param("actionRequested", "EDIT"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testGetCurrencies_Success() throws Exception {
        Map<String, Object> currencies = new HashMap<>();
        currencies.put("content", new Object[]{});
        when(currencyService.listCurrencies(anyString(), any(), any())).thenReturn(currencies);

        FilterRequestDto filters = new FilterRequestDto("OR", "N", null);
        try (var mockedUserContext = mockStatic(com.asg.common.lib.security.util.UserContext.class)) {
            mockedUserContext.when(com.asg.common.lib.security.util.UserContext::getDocumentId).thenReturn("000-006");
            
            mockMvc.perform(post("/v1/currency/list")
                            .param("documentId", "000-006")
                            .param("actionRequested", "VIEW")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(filters)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Currency list fetched successfully"));
        }
    }

    @Test
    void testGetCurrencyDetails_Success() throws Exception {
        CurrencyRateDto dto = new CurrencyRateDto();
        dto.setCurrencyPoid(1L);
        when(currencyService.getAllCurrencyRates(1L)).thenReturn(dto);

        mockMvc.perform(post("/v1/currency/details")
                        .param("currencyPoid", "1")
                        .param("documentId", "000-006")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testCreateCurrency_Success() throws Exception {
        CurrencyEntity entity = new CurrencyEntity();
        entity.setCurrencyPoid(1L);
        entity.setCurrencyCode("USD");
        entity.setCurrencyName("US Dollar");
        when(currencyService.createOrUpdateCurrency(any(), any(), any())).thenReturn(entity);

        CurrencyCreateRequest request = new CurrencyCreateRequest();
        request.setCurrencyName("US Dollar");
        request.setCurrencyCode("USD");

        mockMvc.perform(post("/v1/currency/create")
                        .param("documentId", "000-006")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testSoftDeleteCurrency_Success() throws Exception {
        doNothing().when(currencyService).softDeleteCurrency(1L);

        mockMvc.perform(delete("/v1/currency/soft-delete")
                        .param("currencyPoid", "1")
                        .param("documentId", "000-006")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testSoftDeleteCurrency_ResourceNotFoundException() throws Exception {
        lenient().doThrow(new ResourceNotFoundException("Currency", "currencyPoid", "1"))
                .when(currencyService).softDeleteCurrency(1L);

        mockMvc.perform(delete("/v1/currency/soft-delete")
                        .param("currencyPoid", "1")
                        .param("documentId", "000-006")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testSoftDeleteCurrency_MissingCurrencyPoid() throws Exception {
        mockMvc.perform(delete("/v1/currency/soft-delete")
                        .param("documentId", "000-006")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testSoftDeleteCurrency_InvalidCurrencyPoid() throws Exception {
        mockMvc.perform(delete("/v1/currency/soft-delete")
                        .param("currencyPoid", "invalid")
                        .param("documentId", "000-006")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSoftDeleteCurrency_MissingDocumentId() throws Exception {
        doNothing().when(currencyService).softDeleteCurrency(1L);
        
        mockMvc.perform(delete("/v1/currency/soft-delete")
                        .param("currencyPoid", "1")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateCurrency_MissingCurrencyName() throws Exception {
        CurrencyCreateRequest request = new CurrencyCreateRequest();
        request.setCurrencyCode("USD");

        mockMvc.perform(post("/v1/currency/create")
                        .param("documentId", "000-006")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateCurrency_MissingCurrencyCode() throws Exception {
        CurrencyCreateRequest request = new CurrencyCreateRequest();
        request.setCurrencyName("US Dollar");

        mockMvc.perform(post("/v1/currency/create")
                        .param("documentId", "000-006")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateCurrency_ServiceException() throws Exception {
        lenient().when(currencyService.createOrUpdateCurrency(any(), any(), any()))
                .thenThrow(new RuntimeException("Database error"));

        CurrencyCreateRequest request = new CurrencyCreateRequest();
        request.setCurrencyName("US Dollar");
        request.setCurrencyCode("USD");

        mockMvc.perform(post("/v1/currency/create")
                        .param("documentId", "000-006")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetCurrencies_ServiceException() throws Exception {
        when(currencyService.listCurrencies(anyString(), any(), any()))
                .thenThrow(new RuntimeException("Database error"));

        FilterRequestDto filters = new FilterRequestDto("OR", "N", null);
        
        try (var mockedUserContext = mockStatic(com.asg.common.lib.security.util.UserContext.class)) {
            mockedUserContext.when(com.asg.common.lib.security.util.UserContext::getDocumentId).thenReturn("000-006");
            
            mockMvc.perform(post("/v1/currency/list")
                            .param("documentId", "000-006")
                            .param("actionRequested", "VIEW")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(filters)))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Test
    void testGetCurrencies_MissingDocumentId() throws Exception {
        Map<String, Object> currencies = new HashMap<>();
        currencies.put("content", new Object[]{});
        when(currencyService.listCurrencies(anyString(), any(), any())).thenReturn(currencies);
        
        FilterRequestDto filters = new FilterRequestDto("OR", "N", null);
        
        try (var mockedUserContext = mockStatic(com.asg.common.lib.security.util.UserContext.class)) {
            mockedUserContext.when(com.asg.common.lib.security.util.UserContext::getDocumentId).thenReturn("000-006");
            
            mockMvc.perform(post("/v1/currency/list")
                            .param("actionRequested", "VIEW")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(filters)))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void testGetCurrencyDetails_ServiceException() throws Exception {
        lenient().when(currencyService.getAllCurrencyRates(1L))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/v1/currency/details")
                        .param("currencyPoid", "1")
                        .param("documentId", "000-006")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetCurrencyDetails_MissingCurrencyPoid() throws Exception {
        mockMvc.perform(post("/v1/currency/details")
                        .param("documentId", "000-006")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetCurrencyDetails_InvalidCurrencyPoid() throws Exception {
        mockMvc.perform(post("/v1/currency/details")
                        .param("currencyPoid", "invalid")
                        .param("documentId", "000-006")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCurrencyUpload_MissingFile() throws Exception {
        mockMvc.perform(multipart("/v1/currency/upload-excel")
                        .param("groupPoid", "1014")
                        .param("companyPoid", "2001")
                        .param("userPoid", "3001")
                        .param("documentId", "000-006")
                        .param("actionRequested", "CREATE"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testCurrencyUpload_InvalidGroupPoid() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "test".getBytes());

        mockMvc.perform(multipart("/v1/currency/upload-excel")
                        .file(mockFile)
                        .param("groupPoid", "invalid")
                        .param("companyPoid", "2001")
                        .param("userPoid", "3001")
                        .param("documentId", "000-006")
                        .param("actionRequested", "CREATE"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testUpdateRate_MissingRequestBody() throws Exception {
        mockMvc.perform(post("/v1/currency/update-rate")
                        .param("documentId", "000-006")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateRate_InvalidJson() throws Exception {
        mockMvc.perform(post("/v1/currency/update-rate")
                        .param("documentId", "000-006")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid-json"))
                .andExpect(status().isBadRequest());
    }
}