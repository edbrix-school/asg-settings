package com.asg.settings.controller;

import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.settings.dto.AlertAndRemainderDto;
import com.asg.settings.service.AlertConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AlertAndReminderControllerTest {

    @Mock
    private AlertConfigService alertConfigService;

    @InjectMocks
    private AlertAndReminderController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @org.springframework.web.bind.annotation.ControllerAdvice
    static class GlobalExceptionHandler {
        @org.springframework.web.bind.annotation.ExceptionHandler(ResourceNotFoundException.class)
        public org.springframework.http.ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException e) {
            return org.springframework.http.ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @Test
    void testGetAlertConfigList_success() throws Exception {
        Map<String, Object> response = Map.of(
                "content", Collections.singletonList(Map.of("configPoid", 1L, "alertName", "Test Alert")),
                "totalElements", 1L,
                "totalPages", 1
        );

        when(alertConfigService.getAllAlertConfigs(anyString(), any(FilterRequestDto.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/alerts/list")
                        .param("documentId", "000-010")
                        .param("actionRequested", "VIEW")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "                                                \"operator\":\"AND\",\n" +
                                "                                                \"isDeleted\":\"N\",\n" +
                                "                                                \"filters\":[\n" +
                                "                                                    {\n" +
                                "                                                        \"searchField\":\"ALERT_NAME\",\n" +
                                "                                                        \"searchValue\":\"IT Related Alerts\"\n" +
                                "                                                    },\n" +
                                "                                                    {\n" +
                                "                                                        \"searchField\":\"FREQUENCY_TYPE\",\n" +
                                "                                                        \"searchValue\":\"DAY\"\n" +
                                "                                                    }\n" +
                                "                                                ]\n" +
                                "                                            }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Alerts list fetched successfully"));
    }

    @Test
    void testGetAlertConfigList_withNullFilters() throws Exception {
        Map<String, Object> response = Map.of("content", Collections.emptyList(), "totalElements", 0L);

        when(alertConfigService.getAllAlertConfigs(anyString(), isNull(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/alerts/list")
                        .param("documentId", "000-010")
                        .param("actionRequested", "VIEW")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateAlert_success() throws Exception {
        String requestJson = """
                {
                    "alertName": "Test Alert",
                    "sqlQuery": "SELECT * FROM test_table",
                    "notifyUserRolesPoid": ["123", "456"],
                    "alertCheckType": "DATECHECK",
                    "frequencyType": "DAY"
                }
                """;

        AlertAndRemainderDto response = new AlertAndRemainderDto();
        response.setConfigPoid(1L);
        response.setAlertName("Test Alert");

        when(alertConfigService.createAlert(any(AlertAndRemainderDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/alerts")
                        .param("documentId", "000-010")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Alert Created Successfully"));
    }

    @Test
    void testCreateAlert_validationError() throws Exception {
        String requestJson = """
                {
                    "sqlQuery": "SELECT * FROM test_table"
                }
                """;

        mockMvc.perform(post("/api/v1/alerts")
                        .param("documentId", "000-010")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAlertConfigById_success() throws Exception {
        AlertAndRemainderDto alert = new AlertAndRemainderDto();
        alert.setConfigPoid(1L);
        alert.setAlertName("Test Alert");

        when(alertConfigService.getByAlertConfigId(1L)).thenReturn(alert);

        mockMvc.perform(get("/api/v1/alerts/1")
                        .param("documentId", "000-010")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("success"));
    }

    @Test
    void testGetAlertConfigById_notFound() throws Exception {
        when(alertConfigService.getByAlertConfigId(999L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/alerts/999")
                        .param("documentId", "000-010")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetAlertConfigById_exception() throws Exception {
        when(alertConfigService.getByAlertConfigId(1L)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/alerts/1")
                        .param("documentId", "000-010")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error fetching Alert Details: Database error"));
    }

    @Test
    void testUpdateAlert_success() throws Exception {
        String requestJson = """
                {
                    "alertName": "Updated Alert",
                    "sqlQuery": "SELECT * FROM updated_table",
                    "notifyUserRolesPoid": ["123"],
                    "alertCheckType": "DATECHECK",
                    "frequencyType": "DAY"
                }
                """;

        AlertAndRemainderDto response = new AlertAndRemainderDto();
        response.setConfigPoid(1L);
        response.setAlertName("Updated Alert");

        when(alertConfigService.updateAlertConfig(eq(1L), any(AlertAndRemainderDto.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/alerts/1")
                        .param("documentId", "000-010")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Alert configuration updated successfully"));
    }

    @Test
    void testUpdateAlert_notFound() throws Exception {
        String requestJson = """
                {
                    "alertName": "Updated Alert",
                    "sqlQuery": "SELECT * FROM updated_table",
                    "notifyUserRolesPoid": ["123"],
                    "alertCheckType": "DATECHECK",
                    "frequencyType": "DAY"
                }
                """;

        when(alertConfigService.updateAlertConfig(eq(999L), any(AlertAndRemainderDto.class)))
                .thenThrow(new ResourceNotFoundException("AlertConfig", "configPoid", 999L));

        mockMvc.perform(put("/api/v1/alerts/999")
                        .param("documentId", "000-010")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateAlert_validationError() throws Exception {
        String requestJson = """
                {
                    "sqlQuery": "SELECT * FROM updated_table"
                }
                """;

        mockMvc.perform(put("/api/v1/alerts/1")
                        .param("documentId", "000-010")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetInactiveAndDeletedAlerts_success() throws Exception {
        AlertAndRemainderDto alert = new AlertAndRemainderDto();
        alert.setConfigPoid(1L);
        alert.setAlertName("Inactive Alert");
        alert.setActive("N");

        List<AlertAndRemainderDto> alerts = Arrays.asList(alert);
        when(alertConfigService.getInactiveAndDeletedAlerts()).thenReturn(alerts);

        mockMvc.perform(get("/api/v1/alerts/deleted-configs")
                        .param("documentId", "000-010")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Successfully retrieved inactive and deleted alerts"));
    }

    @Test
    void testGetInactiveAndDeletedAlerts_empty() throws Exception {
        when(alertConfigService.getInactiveAndDeletedAlerts()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/alerts/deleted-configs")
                        .param("documentId", "000-010")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testSoftDeleteAlertConfig_success() throws Exception {
        Long configPoid = 1L;

        mockMvc.perform(delete("/api/v1/alerts/" + configPoid)
                        .param("documentId", "000-010")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Alert configuration deleted successfully"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    void testSoftDeleteAlertConfig_notFound() throws Exception {
        Long configPoid = 999L;

        doThrow(new ResourceNotFoundException("Alert Config", "configPoid", configPoid))
                .when(alertConfigService).softDeleteByconfigPoid(configPoid);

        mockMvc.perform(delete("/api/v1/alerts/" + configPoid)
                        .param("documentId", "000-010")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSoftDeleteAlertConfig_missingDocumentId() throws Exception {
        Long configPoid = 1L;

        mockMvc.perform(delete("/api/v1/alerts/" + configPoid)
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSoftDeleteAlertConfig_missingActionRequested() throws Exception {
        Long configPoid = 1L;

        mockMvc.perform(delete("/api/v1/alerts/" + configPoid)
                        .param("documentId", "000-010"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAlertConfigList_missingRequiredParams() throws Exception {
        mockMvc.perform(post("/api/v1/alerts/list")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateAlert_missingRequiredParams() throws Exception {
        String requestJson = """
                {
                    "alertName": "Test Alert",
                    "sqlQuery": "SELECT * FROM test_table"
                }
                """;

        mockMvc.perform(post("/api/v1/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateAlert_missingRequiredParams() throws Exception {
        String requestJson = """
                {
                    "alertName": "Updated Alert"
                }
                """;

        mockMvc.perform(put("/api/v1/alerts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAlertConfigById_missingRequiredParams() throws Exception {
        mockMvc.perform(get("/api/v1/alerts/1"))
                .andExpect(status().isBadRequest());
    }
}
