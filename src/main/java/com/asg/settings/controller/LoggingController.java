package com.asg.settings.controller;

import com.asg.common.lib.dto.response.ApiResponse;
import com.asg.settings.service.LoggingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/log")

@RequiredArgsConstructor
@Slf4j
public class LoggingController {

    private final LoggingService loggingService;

    // ----------------- SUMMARY LOG -----------------
    @Operation(
            summary = "Get Log Summary",
            description = "Fetch the summary of logs for a given document."
    )
    @GetMapping("/{docId}/{docKeyPoid}/summary")
    public Object getSummaryLog(
            @Parameter(description = "Document ID", required = true)
            @PathVariable String docId,

            @Parameter(description = "Document Key Poid", required = true)
            @PathVariable Long docKeyPoid
    ) {
        try {
            List<?> summary = loggingService.getLogSummary(docId, docKeyPoid);
            return ApiResponse.success("Log summary fetched successfully", summary);
        } catch (Exception e) {
            log.error("Error fetching log summary for docId {}, key {}", docId, docKeyPoid, e);
            return ApiResponse.internalServerError("Failed to fetch log summary: " + e.getMessage());
        }
    }

    // ----------------- DETAIL LOG -----------------
    @Operation(
            summary = "Get Log Details",
            description = "Fetch detailed log entries for a given document."
    )
    @GetMapping("/{docId}/{docKeyPoid}/details")
    public Object getDetailLog(
            @Parameter(description = "Document ID", required = true)
            @PathVariable String docId,

            @Parameter(description = "Document Key Poid", required = true)
            @PathVariable Long docKeyPoid
    ) {
        try {
            List<?> details = loggingService.getDetailedLogs(docId, docKeyPoid);
            return ApiResponse.success("Log details fetched successfully", details);
        } catch (Exception e) {
            log.error("Error fetching detail log for docId {}, key {}", docId, docKeyPoid, e);
            return ApiResponse.internalServerError("Failed to fetch log details: " + e.getMessage());
        }
    }
}
