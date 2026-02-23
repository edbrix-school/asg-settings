package com.asg.settings.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.settings.dto.request.GlobalKPIMastersRequestDto;
import com.asg.settings.dto.response.GlobalKPIMastersResponseDto;
import com.asg.settings.dto.response.KpiLineMasterResponseDto;
import com.asg.settings.service.GlobalKPIMasterService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Global KPI Master")
public class GlobalKPIController {

        private final GlobalKPIMasterService service;
        private final LoggingService loggingService;

        // ========================= GET BY ID =========================

        @AllowedAction(UserRolesRightsEnum.VIEW)
        @Operation(summary = "Get Global KPI Master by ID",
                description = "Retrieve a specific Global KPI Master record by POID")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Global KPI Master retrieved successfully"),
                @ApiResponse(responseCode = "404", description = "Global KPI Master not found"),
                @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        @GetMapping("/{globalKpiMastersPoid}")
        public ResponseEntity<?> getById(@PathVariable Long globalKpiMastersPoid) {

                GlobalKPIMastersResponseDto result = service.getById(globalKpiMastersPoid);

                loggingService.createLogSummaryEntry(
                        LogDetailsEnum.VIEWED,
                        UserContext.getDocumentId(),
                        globalKpiMastersPoid.toString());

                return success("Global KPI Master retrieved successfully", result);
        }

        // ========================= LIST =========================

        @AllowedAction(UserRolesRightsEnum.VIEW)
        @Operation(summary = "List Global KPI Masters",
                description = "Retrieve a paginated list of Global KPI Master records")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Global KPI Master list retrieved successfully"),
                @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
                @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        @PostMapping("/list")
        public ResponseEntity<?> list(
                @RequestBody(required = false) FilterRequestDto filters,
                @PageableDefault(size = 20) Pageable pageable) {

                Map<String, Object> result = service.list(filters, pageable);
                return success("Global KPI Master list retrieved successfully", result);
        }

        // ========================= CREATE =========================

        @AllowedAction(UserRolesRightsEnum.CREATE)
        @Operation(summary = "Create Global KPI Master",
                description = "Create a new Global KPI Master record")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Global KPI Master created successfully"),
                @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
                @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        @PostMapping
        public ResponseEntity<?> create(@Valid @RequestBody GlobalKPIMastersRequestDto requestDto) {

                GlobalKPIMastersResponseDto result = service.create(requestDto);
                return success("Global KPI Master created successfully", result);
        }

        // ========================= UPDATE =========================

        @AllowedAction(UserRolesRightsEnum.EDIT)
        @Operation(summary = "Update Global KPI Master",
                description = "Update an existing Global KPI Master record")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Global KPI Master updated successfully"),
                @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
                @ApiResponse(responseCode = "404", description = "Global KPI Master not found"),
                @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        @PutMapping("/{globalKpiMastersPoid}")
        public ResponseEntity<?> update(
                @Valid @RequestBody GlobalKPIMastersRequestDto requestDto,
                @PathVariable Long globalKpiMastersPoid) {

                GlobalKPIMastersResponseDto result =
                        service.update(requestDto, globalKpiMastersPoid);

                return success("Global KPI Master updated successfully", result);
        }

        // ========================= DELETE =========================

        @AllowedAction(UserRolesRightsEnum.DELETE)
        @Operation(summary = "Delete Global KPI Master",
                description = "Delete a Global KPI Master record")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Global KPI Master deleted successfully"),
                @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
                @ApiResponse(responseCode = "404", description = "Global KPI Master not found"),
                @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        @DeleteMapping("/{globalKpiMastersPoid}")
        public ResponseEntity<?> delete(
                @PathVariable Long globalKpiMastersPoid,
                @Valid @RequestBody DeleteReasonDto deleteReasonDto) {

                service.delete(globalKpiMastersPoid, deleteReasonDto);
                return success("Global KPI Master deleted successfully", null);
        }

        // ========================= FETCH RELATED DATA =========================

        @AllowedAction(UserRolesRightsEnum.VIEW)
        @Operation(summary = "Fetch KPI Lines",
                description = "Retrieve all active KPI Line records")
        @GetMapping("/fetch/lines")
        public ResponseEntity<?> listKpiLines() {

                List<KpiLineMasterResponseDto> result = service.getAllLines();
                return success("KPI Lines retrieved successfully", result);
        }

        @AllowedAction(UserRolesRightsEnum.VIEW)
        @Operation(summary = "Fetch KPI Companies",
                description = "Retrieve all active KPI Company records")
        @GetMapping("/fetch/companies")
        public ResponseEntity<?> getAllCompanies() {

                return success("KPI Companies retrieved successfully",
                        service.getAllCompanies());
        }

        @AllowedAction(UserRolesRightsEnum.VIEW)
        @Operation(summary = "Fetch KPI Employees",
                description = "Retrieve all active KPI Employee records")
        @GetMapping("/fetch/employees")
        public ResponseEntity<?> getAllEmployees() {

                return success("KPI Employees retrieved successfully",
                        service.getAllEmployees());
        }
}

