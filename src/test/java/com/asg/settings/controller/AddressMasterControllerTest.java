package com.asg.settings.controller;

import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.response.AddressMasterResponse;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.settings.controller.AddressMasterController;
import com.asg.settings.exceptions.GlobalExceptionHandler;
import com.asg.settings.service.AddressMasterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AddressMasterControllerTest {

    @Mock
    private AddressMasterService service;

    @InjectMocks
    private AddressMasterController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void listAddressMasters_Success() throws Exception {
        List<FilterDto> filters = Arrays.asList(new FilterDto("addressName", "Test Company"));
        FilterRequestDto request = new FilterRequestDto("AND", "N", filters);
        Map<String, Object> mockResponse = Map.of("content", Arrays.asList());

        lenient().when(service.listAddressMasters(eq("000-016"), any(FilterRequestDto.class), any(Pageable.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/v1/address-master/list")
                        .param("documentId", "000-016")
                        .param("actionRequested", "VIEW")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Address Masters list fetched successfully"));
    }

    @Test
    void getMaster_Success() throws Exception {
        AddressMasterResponse response = new AddressMasterResponse();
        response.setAddressMasterPoid(1L);
        response.setAddressName("Test Company");

        when(service.getMasterWithDetails(1L)).thenReturn(response);

        mockMvc.perform(get("/v1/address-master/1")
                        .param("documentId", "000-016")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createAll_Success() throws Exception {
        when(service.createAll(1L)).thenReturn("SUCCESS");

        mockMvc.perform(post("/v1/address-master/1/create-all")
                        .param("documentId", "000-016")
                        .param("actionRequested", "CREATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void copyAll_Success() throws Exception {
        when(service.copyAll(1L)).thenReturn("SUCCESS");

        mockMvc.perform(post("/v1/address-master/1/copy-all")
                        .param("documentId", "000-016")
                        .param("actionRequested", "CREATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void softDeleteAddressMaster_Success() throws Exception {
        mockMvc.perform(delete("/v1/address-master/1")
                        .param("documentId", "000-016")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void softDeleteAddressMaster_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Address Master", "addressMasterPoid", 1L))
                .when(service).softDeleteAddressMaster(1L);

        mockMvc.perform(delete("/v1/address-master/1")
                        .param("documentId", "000-016")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateAddress_ValidationError() throws Exception {
        AddressMasterResponse request = new AddressMasterResponse();

        mockMvc.perform(post("/v1/address-master/update")
                        .param("documentId", "000-016")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void listAddressMasters_InvalidArgument() throws Exception {
        FilterRequestDto request = new FilterRequestDto("AND", "N", Arrays.asList());

        when(service.listAddressMasters(eq("000-016"), any(FilterRequestDto.class), any(Pageable.class)))
                .thenThrow(new IllegalArgumentException("Invalid search field"));

        mockMvc.perform(post("/v1/address-master/list")
                        .param("documentId", "000-016")
                        .param("actionRequested", "VIEW")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void listAddressMasters_EmptyResult() throws Exception {
        FilterRequestDto request = new FilterRequestDto("AND", "N", Arrays.asList());
        Map<String, Object> emptyResponse = Map.of("content", Arrays.asList());

        lenient().when(service.listAddressMasters(eq("000-016"), any(FilterRequestDto.class), any(Pageable.class)))
                .thenReturn(emptyResponse);

        mockMvc.perform(post("/v1/address-master/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void listAddressMasters_NullFilters() throws Exception {
        Map<String, Object> mockResponse = Map.of("content", Arrays.asList());

        lenient().when(service.listAddressMasters(eq("000-016"), any(), any(Pageable.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/v1/address-master/list")
                        .param("documentId", "000-016")
                        .param("actionRequested", "VIEW")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void listAddressMasters_InternalServerError() throws Exception {
        FilterRequestDto request = new FilterRequestDto("AND", "N", Arrays.asList());

        when(service.listAddressMasters(eq("000-016"), any(FilterRequestDto.class), any(Pageable.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(post("/v1/address-master/list")
                        .param("documentId", "000-016")
                        .param("actionRequested", "VIEW")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getMaster_InternalServerError() throws Exception {
        when(service.getMasterWithDetails(1L))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/v1/address-master/1")
                        .param("documentId", "000-016")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createAll_InternalServerError() throws Exception {
        when(service.createAll(1L))
                .thenThrow(new RuntimeException("Processing error"));

        mockMvc.perform(post("/v1/address-master/1/create-all")
                        .param("documentId", "000-016")
                        .param("actionRequested", "CREATE"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void copyAll_InternalServerError() throws Exception {
        when(service.copyAll(1L))
                .thenThrow(new RuntimeException("Processing error"));

        mockMvc.perform(post("/v1/address-master/1/copy-all")
                        .param("documentId", "000-016")
                        .param("actionRequested", "CREATE"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void softDeleteAddressMaster_InternalServerError() throws Exception {
        doThrow(new RuntimeException("Database error"))
                .when(service).softDeleteAddressMaster(1L);

        mockMvc.perform(delete("/v1/address-master/1")
                        .param("documentId", "000-016")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void listAddressMasters_InvalidJson() throws Exception {
        mockMvc.perform(post("/v1/address-master/list")
                        .param("documentId", "000-016")
                        .param("actionRequested", "VIEW")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listAddressMasters_SpecialCharactersInFilter() throws Exception {
        List<FilterDto> filters = Arrays.asList(new FilterDto("addressName", "Test & Co. <script>"));
        FilterRequestDto request = new FilterRequestDto("AND", "N", filters);
        Map<String, Object> mockResponse = Map.of("content", Arrays.asList());

        lenient().when(service.listAddressMasters(eq("000-016"), any(FilterRequestDto.class), any(Pageable.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/v1/address-master/list")
                        .param("documentId", "000-016")
                        .param("actionRequested", "VIEW")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void listAddressMasters_LargePageSize() throws Exception {
        FilterRequestDto request = new FilterRequestDto("AND", "N", Arrays.asList());
        Map<String, Object> mockResponse = Map.of("content", Arrays.asList());

        lenient().when(service.listAddressMasters(eq("000-016"), any(FilterRequestDto.class), any(Pageable.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/v1/address-master/list")
                        .param("documentId", "000-016")
                        .param("actionRequested", "VIEW")
                        .param("size", "1000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getMaster_ZeroPoid() throws Exception {
        when(service.getMasterWithDetails(0L))
                .thenThrow(new IllegalArgumentException("Invalid POID"));

        mockMvc.perform(get("/v1/address-master/0")
                        .param("documentId", "000-016")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getMaster_NegativePoid() throws Exception {
        when(service.getMasterWithDetails(-1L))
                .thenThrow(new IllegalArgumentException("Invalid POID"));

        mockMvc.perform(get("/v1/address-master/-1"))
                .andExpect(status().isInternalServerError());
    }
    @Test
    void createAll_ZeroPoid() throws Exception {
        when(service.createAll(0L))
                .thenThrow(new IllegalArgumentException("Invalid POID"));

        mockMvc.perform(post("/v1/address-master/0/create-all")
                        .param("documentId", "000-016")
                        .param("actionRequested", "CREATE"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void createAll_NegativePoid() throws Exception {
        when(service.createAll(-1L))
                .thenThrow(new IllegalArgumentException("Invalid POID"));

        mockMvc.perform(post("/v1/address-master/-1/create-all")
                        .param("documentId", "000-016")
                        .param("actionRequested", "CREATE"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void copyAll_ZeroPoid() throws Exception {
        when(service.copyAll(0L))
                .thenThrow(new IllegalArgumentException("Invalid POID"));

        mockMvc.perform(post("/v1/address-master/0/copy-all")
                        .param("documentId", "000-016")
                        .param("actionRequested", "CREATE"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void copyAll_NegativePoid() throws Exception {
        when(service.copyAll(-1L))
                .thenThrow(new IllegalArgumentException("Invalid POID"));

        mockMvc.perform(post("/v1/address-master/-1/copy-all")
                        .param("documentId", "000-016")
                        .param("actionRequested", "CREATE"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void createAddress_InvalidJsonFormat() throws Exception {
        mockMvc.perform(post("/v1/address-master/create")
                        .param("documentId", "000-016")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAddress_MissingContentType() throws Exception {
        AddressMasterResponse request = new AddressMasterResponse();

        mockMvc.perform(post("/v1/address-master/create")
                        .param("documentId", "000-016")
                        .param("actionRequested", "CREATE")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void softDeleteAddressMaster_ZeroPoid() throws Exception {
        doThrow(new IllegalArgumentException("Invalid POID"))
                .when(service).softDeleteAddressMaster(0L);

        mockMvc.perform(delete("/v1/address-master/0")
                        .param("documentId", "000-016")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void softDeleteAddressMaster_NegativePoid() throws Exception {
        doThrow(new IllegalArgumentException("Invalid POID"))
                .when(service).softDeleteAddressMaster(-1L);

        mockMvc.perform(delete("/v1/address-master/-1")
                        .param("documentId", "000-016")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isInternalServerError());
    }
}
