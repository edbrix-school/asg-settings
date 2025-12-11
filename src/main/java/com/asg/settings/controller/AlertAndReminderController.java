package com.asg.settings.controller;


import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.settings.dto.AlertAndRemainderDto;
import com.asg.settings.service.AlertConfigService;
import com.nimbusds.oauth2.sdk.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.asg.common.lib.dto.response.ApiResponse.internalServerError;
import static com.asg.common.lib.dto.response.ApiResponse.success;


@RestController
@RequestMapping("/v1/alerts")

public class AlertAndReminderController {

    private final AlertConfigService alertConfigService;

    @Autowired
    public AlertAndReminderController(AlertConfigService alertConfigService) {
        this.alertConfigService = alertConfigService;
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @Operation(
            summary = "List Alert and Reminders with Search and Sort",
            description = "Provide search filters. Valid `searchField` values: GLOBALSEARCH or (ALERT_NAME, FREQUENCY_TYPE). Sorting default on userRolePoid, desc." +
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
                      2. Any combination of specific fields (ALERT_NAME, FREQUENCY_TYPE).
                      3. operator field will either have "AND" or "OR", if not given will be considered as "OR"
                      4. isDeleted when 'N' or null, will search and return non deleted records, 'Y' will check and return deleted records
                    """,
            content = @Content(
                    schema = @Schema(implementation = FilterRequestDto.class),
                    examples = {
                            @ExampleObject(
                                    name = "Alert and Reminders Filters",
                                    value = """
                                            {
                                                "operator":"AND",
                                                "isDeleted":"N",
                                                "filters":[
                                                    {
                                                        "searchField":"ALERT_NAME",
                                                        "searchValue":"IT Related Alerts"
                                                    },
                                                    {
                                                        "searchField":"FREQUENCY_TYPE",
                                                        "searchValue":"DAY"
                                                    }
                                                ]
                                            }
                                            """
                            )
                    }
            )
    )
    @PostMapping("/list")
    public ResponseEntity<?> getAlertAndRemindersList(@ParameterObject Pageable pageable,
                                                      @RequestBody(required = false) FilterRequestDto filters) {
        Map<String, Object> alertConfigs = alertConfigService.getAllAlertConfigs(UserContext.getDocumentId(), filters, pageable);
        return success("Alerts list fetched successfully", alertConfigs);
    }

    @Operation(
            summary = "Create alert configuration",
            description = "Creates a new alert or reminder configuration",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Alert created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )

    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    schema = @Schema(implementation = AlertAndRemainderDto.class),
                    examples = {
                            @ExampleObject(
                                    name = "Create Alert Example",
                                    value = """
                                            {
                                                "alertName": "Contract Expiry Alert",
                                                "sqlQuery": "SELECT * FROM contracts WHERE expiry_date <= SYSDATE + :notifyDays",
                                                "expiryDateField": "EXPIRY_DATE",
                                                "notifyDays": 30,
                                                "notifyUserRolesPoid": ["123", "456"],
                                                "active": "Y",
                                                "seqNo": 1,
                                                "alertCheckType": "DATECHECK",
                                                "escalateDays": 7,
                                                "escalationUserRolesPoid": ["789"],
                                                "deleted": "N",
                                                "alertEscalateFrequency": 1,
                                                "alertNotifyFrequency": 1,
                                                "frequencyType": "DAY"
                                            }
                                            """
                            )
                    }
            )
    )
    @PostMapping
    public ResponseEntity<?> createAlert(
            @Valid @RequestBody AlertAndRemainderDto request) {
        AlertAndRemainderDto response = alertConfigService.createAlert(request);
        return success("Alert created successfully", response);
    }

    @Operation(
            summary = "Get alert configuration by ID",
            description = "Retrieves a specific alert configuration by its unique identifier",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Alert configuration retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Alert configuration not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )

    @GetMapping("/{configId}")
    public ResponseEntity<?> getAlertConfigById(
            @Parameter(description = "Alert configuration ID", required = true, example = "1")
            @PathVariable(name = "configId") Long configId) {

        {
            try {
                if (configId == null) {
                    return internalServerError("config Poid not found");
                }
                AlertAndRemainderDto alertAndRemainderDto = alertConfigService.getByAlertConfigId(configId);

                return success("success", alertAndRemainderDto);
            } catch (Exception e) {
                return internalServerError("Error fetching Alert Details: " + e.getMessage());
            }
        }
    }

    @Operation(
            summary = "Update alert configuration",
            description = "Updates an existing alert configuration",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Alert configuration updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "404", description = "Alert configuration not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @PutMapping("/{configPoid}")
    public ResponseEntity<?> updateAlert(
            @Parameter(description = "Alert configuration ID to update", required = true, example = "1")
            @PathVariable Long configPoid,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            schema = @Schema(implementation = AlertAndRemainderDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Update Alert Example",
                                            value = """
                                                    {
                                                        "alertName": "Updated Contract Alert",
                                                        "sqlQuery": "SELECT * FROM contracts WHERE expiry_date <= SYSDATE + :notifyDays",
                                                        "expiryDateField": "EXPIRY_DATE",
                                                        "notifyDays": 45,
                                                        "notifyUserRolesPoid": ["123", "456"],
                                                        "active": "N",
                                                        "seqNo": 2,
                                                        "alertCheckType": "DATECHECK",
                                                        "escalateDays": 10,
                                                        "escalationUserRolesPoid": ["789"],
                                                        "deleted": "N",
                                                        "alertEscalateFrequency": 2,
                                                        "alertNotifyFrequency": 1,
                                                        "frequencyType": "DAY"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            @Valid @RequestBody AlertAndRemainderDto updateRequest) {
        AlertAndRemainderDto updatedConfig = alertConfigService.updateAlertConfig(configPoid, updateRequest);
        return success("Alert configuration updated successfully", updatedConfig);
    }

    @Operation(
            summary = "Get inactive and deleted alert configurations",
            description = "Retrieves alert configurations marked as inactive or deleted",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Inactive and deleted alerts retrieved successfully"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )

    @GetMapping("/deleted-configs")
    public ResponseEntity<?> getInactiveAndDeletedAlerts() {
        List<AlertAndRemainderDto> inactiveDeletedAlerts = alertConfigService.getInactiveAndDeletedAlerts();
        return success("Successfully retrieved inactive and deleted alerts", inactiveDeletedAlerts);
    }


    @Operation(
            summary = "Soft delete an alert configuration",
            description = "Marks an alert configuration as deleted by setting the 'deleted' flag to 'Y' and 'active' flag to 'N' instead of removing it from the database.",
            tags = {"Alert Configuration Management"},
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Alert configuration successfully marked as deleted",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SuccessResponse.class),
                                    examples = @ExampleObject(
                                            value = "{\"message\": \"Alert configuration deleted successfully\", \"success\": true, \"statusCode\": 200}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Alert configuration not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            value = "{\"message\": \"Alert configuration not found with configPoid: 999\", \"success\": false, \"statusCode\": 404}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )

    @DeleteMapping("/{configPoid}")
    public ResponseEntity<?> softdeleteAlertConfig(
            @Parameter(description = "Alert configuration ID to delete", required = true, example = "1")
            @PathVariable Long configPoid) {
        alertConfigService.softDeleteByconfigPoid(configPoid);
        return success("Alert configuration deleted successfully");
    }
}