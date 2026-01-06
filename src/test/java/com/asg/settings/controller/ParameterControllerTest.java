package com.asg.settings.controller;

import com.asg.settings.dto.BulkUpdateResponseDTO;
import com.asg.settings.dto.UpdateParameterDTO;
import com.asg.settings.dto.UpdateParameterRequestDTO;
import com.asg.settings.exceptions.GlobalExceptionHandler;
import com.asg.settings.service.ParameterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ParameterControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ParameterService parameterService;

    @InjectMocks
    private ParameterController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void updateParameters_Success() throws Exception {
        UpdateParameterDTO paramUpdate = new UpdateParameterDTO();
        paramUpdate.setParameterPoid(55717L);
        paramUpdate.setParameterKeyId("70");
        paramUpdate.setParameterValue("updated_value");

        UpdateParameterRequestDTO request = new UpdateParameterRequestDTO();
        request.setLoginUserPoid(3371L);
        request.setParameters(List.of(paramUpdate));

        BulkUpdateResponseDTO response = new BulkUpdateResponseDTO();
        response.setOverallStatus("SUCCESS");
        response.setResults(Collections.emptyList());

        when(parameterService.updateParameters(any(UpdateParameterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/v1/global-parameters/bulk-update")
                        .param("documentId", "000-009")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("All parameters updated successfully"))
                .andExpect(jsonPath("$.result.data.overallStatus").value("SUCCESS"));
    }

    @Test
    void updateParameters_PartialSuccess() throws Exception {
        UpdateParameterDTO paramUpdate = new UpdateParameterDTO();
        paramUpdate.setParameterPoid(55717L);
        paramUpdate.setParameterKeyId("70");
        paramUpdate.setParameterValue("updated_value");

        UpdateParameterRequestDTO request = new UpdateParameterRequestDTO();
        request.setLoginUserPoid(3371L);
        request.setParameters(List.of(paramUpdate));

        BulkUpdateResponseDTO response = new BulkUpdateResponseDTO();
        response.setOverallStatus("PARTIAL_SUCCESS");
        response.setResults(Collections.emptyList());

        when(parameterService.updateParameters(any(UpdateParameterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/v1/global-parameters/bulk-update")
                        .param("documentId", "000-009")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Some parameters updated successfully"))
                .andExpect(jsonPath("$.result.data.overallStatus").value("PARTIAL_SUCCESS"));
    }

    @Test
    void updateParameters_Failed() throws Exception {
        UpdateParameterDTO paramUpdate = new UpdateParameterDTO();
        paramUpdate.setParameterPoid(55717L);
        paramUpdate.setParameterKeyId("70");
        paramUpdate.setParameterValue("updated_value");

        UpdateParameterRequestDTO request = new UpdateParameterRequestDTO();
        request.setLoginUserPoid(3371L);
        request.setParameters(List.of(paramUpdate));

        BulkUpdateResponseDTO response = new BulkUpdateResponseDTO();
        response.setOverallStatus("FAILED");
        response.setResults(Collections.emptyList());

        when(parameterService.updateParameters(any(UpdateParameterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/v1/global-parameters/bulk-update")
                        .param("documentId", "000-009")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to update parameters"))
                .andExpect(jsonPath("$.errors.overallStatus").value("FAILED"));
    }

    @Test
    void updateParameters_UnknownStatus() throws Exception {
        UpdateParameterDTO paramUpdate = new UpdateParameterDTO();
        paramUpdate.setParameterPoid(55717L);
        paramUpdate.setParameterKeyId("70");
        paramUpdate.setParameterValue("updated_value");

        UpdateParameterRequestDTO request = new UpdateParameterRequestDTO();
        request.setLoginUserPoid(3371L);
        request.setParameters(List.of(paramUpdate));

        BulkUpdateResponseDTO response = new BulkUpdateResponseDTO();
        response.setOverallStatus("UNKNOWN_STATUS");
        response.setResults(Collections.emptyList());

        when(parameterService.updateParameters(any(UpdateParameterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/v1/global-parameters/bulk-update")
                        .param("documentId", "000-009")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to update parameters"))
                .andExpect(jsonPath("$.errors.overallStatus").value("UNKNOWN_STATUS"));
    }

    @Test
    void updateParameters_MissingDocumentId_ReturnsBadRequest() throws Exception {
        UpdateParameterRequestDTO request = new UpdateParameterRequestDTO();
        request.setLoginUserPoid(3371L);
        request.setParameters(Collections.emptyList());

        mockMvc.perform(put("/v1/global-parameters/bulk-update")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateParameters_MissingActionRequested_ReturnsBadRequest() throws Exception {
        UpdateParameterRequestDTO request = new UpdateParameterRequestDTO();
        request.setLoginUserPoid(3371L);
        request.setParameters(Collections.emptyList());

        mockMvc.perform(put("/v1/global-parameters/bulk-update")
                        .param("documentId", "000-009")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateParameters_InvalidJson_ReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/v1/global-parameters/bulk-update")
                        .param("documentId", "000-009")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateParameters_EmptyRequestBody_ReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/v1/global-parameters/bulk-update")
                        .param("documentId", "000-009")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateParameters_ValidationFailure_ReturnsBadRequest() throws Exception {
        UpdateParameterRequestDTO request = new UpdateParameterRequestDTO();
        // Missing required loginUserPoid and parameters

        mockMvc.perform(put("/v1/global-parameters/bulk-update")
                        .param("documentId", "000-009")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateParameters_ServiceException_ReturnsInternalServerError() throws Exception {
        UpdateParameterDTO paramUpdate = new UpdateParameterDTO();
        paramUpdate.setParameterPoid(55717L);
        paramUpdate.setParameterKeyId("70");
        paramUpdate.setParameterValue("updated_value");

        UpdateParameterRequestDTO request = new UpdateParameterRequestDTO();
        request.setLoginUserPoid(3371L);
        request.setParameters(List.of(paramUpdate));

        when(parameterService.updateParameters(any(UpdateParameterRequestDTO.class)))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(put("/v1/global-parameters/bulk-update")
                        .param("documentId", "000-009")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateParameters_MultipleParameters_Success() throws Exception {
        UpdateParameterDTO param1 = new UpdateParameterDTO();
        param1.setParameterPoid(55717L);
        param1.setParameterKeyId("70");
        param1.setParameterValue("value1");

        UpdateParameterDTO param2 = new UpdateParameterDTO();
        param2.setParameterPoid(55718L);
        param2.setParameterKeyId("71");
        param2.setParameterValue("value2");

        UpdateParameterRequestDTO request = new UpdateParameterRequestDTO();
        request.setLoginUserPoid(3371L);
        request.setParameters(List.of(param1, param2));

        BulkUpdateResponseDTO response = new BulkUpdateResponseDTO();
        response.setOverallStatus("SUCCESS");
        response.setResults(Collections.emptyList());

        when(parameterService.updateParameters(any(UpdateParameterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/v1/global-parameters/bulk-update")
                        .param("documentId", "000-009")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("All parameters updated successfully"))
                .andExpect(jsonPath("$.result.data.overallStatus").value("SUCCESS"));
    }

    @Test
    void updateParameters_NullParametersList_ReturnsBadRequest() throws Exception {
        UpdateParameterRequestDTO request = new UpdateParameterRequestDTO();
        request.setLoginUserPoid(3371L);
        request.setParameters(null);

        mockMvc.perform(put("/v1/global-parameters/bulk-update")
                        .param("documentId", "000-009")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateParameters_EmptyParametersList_ReturnsBadRequest() throws Exception {
        UpdateParameterRequestDTO request = new UpdateParameterRequestDTO();
        request.setLoginUserPoid(3371L);
        request.setParameters(Collections.emptyList());

        mockMvc.perform(put("/v1/global-parameters/bulk-update")
                        .param("documentId", "000-009")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateParameters_NullLoginUserPoid_ReturnsBadRequest() throws Exception {
        UpdateParameterDTO paramUpdate = new UpdateParameterDTO();
        paramUpdate.setParameterPoid(55717L);
        paramUpdate.setParameterKeyId("70");
        paramUpdate.setParameterValue("value");

        UpdateParameterRequestDTO request = new UpdateParameterRequestDTO();
        request.setLoginUserPoid(null);
        request.setParameters(List.of(paramUpdate));

        mockMvc.perform(put("/v1/global-parameters/bulk-update")
                        .param("documentId", "000-009")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateParameters_ZeroLoginUserPoid_Success() throws Exception {
        UpdateParameterDTO paramUpdate = new UpdateParameterDTO();
        paramUpdate.setParameterPoid(55717L);
        paramUpdate.setParameterKeyId("70");
        paramUpdate.setParameterValue("value");

        UpdateParameterRequestDTO request = new UpdateParameterRequestDTO();
        request.setLoginUserPoid(0L);
        request.setParameters(List.of(paramUpdate));

        BulkUpdateResponseDTO response = new BulkUpdateResponseDTO();
        response.setOverallStatus("SUCCESS");

        when(parameterService.updateParameters(any(UpdateParameterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/v1/global-parameters/bulk-update")
                        .param("documentId", "000-009")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updateParameters_NullParameterPoid_Success() throws Exception {
        UpdateParameterDTO paramUpdate = new UpdateParameterDTO();
        paramUpdate.setParameterPoid(null);
        paramUpdate.setParameterKeyId("70");
        paramUpdate.setParameterValue("value");

        UpdateParameterRequestDTO request = new UpdateParameterRequestDTO();
        request.setLoginUserPoid(3371L);
        request.setParameters(List.of(paramUpdate));

        BulkUpdateResponseDTO response = new BulkUpdateResponseDTO();
        response.setOverallStatus("SUCCESS");

        when(parameterService.updateParameters(any(UpdateParameterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/v1/global-parameters/bulk-update")
                        .param("documentId", "000-009")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updateParameters_EmptyParameterValue_Success() throws Exception {
        UpdateParameterDTO paramUpdate = new UpdateParameterDTO();
        paramUpdate.setParameterPoid(55717L);
        paramUpdate.setParameterKeyId("70");
        paramUpdate.setParameterValue("");

        UpdateParameterRequestDTO request = new UpdateParameterRequestDTO();
        request.setLoginUserPoid(3371L);
        request.setParameters(List.of(paramUpdate));

        BulkUpdateResponseDTO response = new BulkUpdateResponseDTO();
        response.setOverallStatus("SUCCESS");

        when(parameterService.updateParameters(any(UpdateParameterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/v1/global-parameters/bulk-update")
                        .param("documentId", "000-009")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updateParameters_SpecialCharactersInValues_Success() throws Exception {
        UpdateParameterDTO paramUpdate = new UpdateParameterDTO();
        paramUpdate.setParameterPoid(55717L);
        paramUpdate.setParameterKeyId("special!@#$%^&*()");
        paramUpdate.setParameterValue("value with spaces & special chars: !@#$%^&*()");

        UpdateParameterRequestDTO request = new UpdateParameterRequestDTO();
        request.setLoginUserPoid(3371L);
        request.setParameters(List.of(paramUpdate));

        BulkUpdateResponseDTO response = new BulkUpdateResponseDTO();
        response.setOverallStatus("SUCCESS");

        when(parameterService.updateParameters(any(UpdateParameterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/v1/global-parameters/bulk-update")
                        .param("documentId", "000-009")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updateParameters_VeryLongParameterValue_Success() throws Exception {
        String longValue = "a".repeat(1000);

        UpdateParameterDTO paramUpdate = new UpdateParameterDTO();
        paramUpdate.setParameterPoid(55717L);
        paramUpdate.setParameterKeyId("70");
        paramUpdate.setParameterValue(longValue);

        UpdateParameterRequestDTO request = new UpdateParameterRequestDTO();
        request.setLoginUserPoid(3371L);
        request.setParameters(List.of(paramUpdate));

        BulkUpdateResponseDTO response = new BulkUpdateResponseDTO();
        response.setOverallStatus("SUCCESS");

        when(parameterService.updateParameters(any(UpdateParameterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/v1/global-parameters/bulk-update")
                        .param("documentId", "000-009")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updateParameters_WrongHttpMethod_Get_ReturnsInternalServerError() throws Exception {
        mockMvc.perform(get("/v1/global-parameters/bulk-update")
                        .param("documentId", "000-009")
                        .param("actionRequested", "EDIT"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateParameters_WrongHttpMethod_Post_ReturnsInternalServerError() throws Exception {
        UpdateParameterRequestDTO request = new UpdateParameterRequestDTO();
        request.setLoginUserPoid(3371L);
        request.setParameters(Collections.emptyList());

        mockMvc.perform(post("/v1/global-parameters/bulk-update")
                        .param("documentId", "000-009")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateParameters_WrongContentType_ReturnsInternalServerError() throws Exception {
        UpdateParameterRequestDTO request = new UpdateParameterRequestDTO();
        request.setLoginUserPoid(3371L);
        request.setParameters(Collections.emptyList());

        mockMvc.perform(put("/v1/global-parameters/bulk-update")
                        .param("documentId", "000-009")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
}