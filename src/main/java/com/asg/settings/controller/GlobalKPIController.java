package com.asg.settings.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.settings.dto.response.GlobalKPIMastersResponseDto;
import com.asg.settings.dto.response.KpiLineMasterResponseDto;
import com.asg.settings.service.GlobalKPIMasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.asg.common.lib.dto.response.ApiResponse.*;

@Slf4j
@RestController
@RequestMapping("/v1/global-kpi-master")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Global-KPI-Master-Controller")
public class GlobalKPIController {

        private final GlobalKPIMasterService service;
        private final LoggingService loggingService;

        @AllowedAction(UserRolesRightsEnum.VIEW)
        @Operation(summary = "Get Global KPI Master Data by ID", description = "Retrieve specific Global KPI Master data record by Global KPI Master POID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved Global KPI Master Data", content = @Content(schema = @Schema(implementation = GlobalKPIMastersResponseDto.class))),
                        @ApiResponse(responseCode = "404", description = "Global KPI Master data not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        @GetMapping("/{globalKpiMastersPoid}")
        public ResponseEntity<?> getById(
                        @Parameter(description = "Global KPI Master POID", required = true) @PathVariable Long globalKpiMastersPoid) {
                GlobalKPIMastersResponseDto result = service.getById(globalKpiMastersPoid);
                loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(),
                                globalKpiMastersPoid.toString());
                return success("Global KPI Master data retrieved successfully", result);
        }

        @AllowedAction(UserRolesRightsEnum.VIEW)
        @Operation(summary = "List Global KPI Master Data", description = "Retrieve a paginated list of Global KPI Master data")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved Global KPI Master data list", content = @Content(schema = @Schema(implementation = Map.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        @PostMapping("/list")
        public ResponseEntity<?> list(

                        @RequestBody(required = false) FilterRequestDto filters,
                        @PageableDefault(size = 20) Pageable pageable) {
                Map<String, Object> result = service.list(filters, pageable);
                return success("Global KPI Master data retrieved successfully", result);
        }

        @AllowedAction(UserRolesRightsEnum.DELETE)
        @Operation(summary = "Delete Global KPI Master")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Global KPI Master deleted successfully"),
                        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
                        @ApiResponse(responseCode = "404", description = "Global KPI Master not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        @DeleteMapping("/{globalKpiMastersPoid}")
        public ResponseEntity<?> delete(
                        @Parameter(description = "Global KPI Master POID", required = true) @PathVariable Long globalKpiMastersPoid,
                        @Valid @RequestBody DeleteReasonDto deleteReasonDto) {
                service.delete(globalKpiMastersPoid, deleteReasonDto);
                return success("Global KPI Master deleted successfully", null);

        }

        @AllowedAction(UserRolesRightsEnum.VIEW)
        @Operation(summary = "List KPI Line Master", description = "Retrieve all active KPI Line Master records")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved KPI Line Master list", content = @Content(schema = @Schema(implementation = KpiLineMasterResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        @GetMapping("/fetch/lines")
        public ResponseEntity<?> listKpiLines() {
                log.info("Fetching all KPI Lines");
                List<KpiLineMasterResponseDto> result = service.getAllLines();
                return success("KPI Line Master retrieved successfully", result);
        }

        @AllowedAction(UserRolesRightsEnum.VIEW)
        @Operation(summary = "List KPI Company Master", description = "Retrieve all active KPI Company Master records")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved KPI Company Master list"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        @GetMapping("/fetch/companies")
        public ResponseEntity<?> getAllCompanies() {
                return success("KPI Company Master retrieved successfully", service.getAllCompanies());
        }

        @AllowedAction(UserRolesRightsEnum.VIEW)
        @Operation(summary = "List KPI Employee Master", description = "Retrieve all active KPI Employee Master records")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved KPI Employee Master list"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        @GetMapping("/fetch/employees")
        public ResponseEntity<?> getAllEmployees() {
                return success("KPI Employee Master retrieved successfully", service.getAllEmployees());
        }

}
