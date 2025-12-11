package com.asg.settings.controller;

import com.asg.settings.dto.request.DocAcquireLockRequestDto;
import com.asg.settings.dto.request.DocReleaseLockRequestDto;
import com.asg.settings.dto.request.DocUpdateLockRequestDto;
import com.asg.settings.service.DocumentLockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.asg.common.lib.dto.response.ApiResponse.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/documentLock")
public class DocumentLockController {

    private final DocumentLockService documentLockService;

    @Operation(
            summary = "Release document lock",
            description = "Releases a previously acquired document lock after the user has completed editing or viewing.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Request body containing document and user details to release the lock",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "loginGroupPoid": 1,
                                              "loginCompanyPoid": 1,
                                              "loginUserPoid": "504",
                                              "docId": "400-001",
                                              "docPoidValue": 54,
                                              "userId": "PRIYA"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lock released successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                            {
                                              "success": true,
                                              "message": "Lock released successfully",
                                              "statusCode": 200,
                                              "result": {
                                                "data": "Record Locking Released - Success..."
                                              }
                                            }
                                            """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request parameters (e.g., missing or incorrect fields)"
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Attempted to release a lock that does not exist or belongs to another user"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error while releasing the lock"
                    )
            }
    )
    @PostMapping("/release-lock")
    public ResponseEntity<?> releaseLock(
            @Parameter(description = "Release lock request payload", required = true)
            @Valid @RequestBody DocReleaseLockRequestDto request


    ) {

        String status = documentLockService.releaseLock(request);
        return success("Lock released successfully", status);
    }

    @Operation(
            summary = "Acquire document lock",
            description = """
                    Acquires a lock on a document to prevent concurrent edits by multiple users.
                    
                    **Note:**
                    - The `type` field in the request body determines the operation mode:
                        - **E** → Acquire Lock (Edit Mode)
                        - **R** → Release Lock (Release Mode)
                    
                    This API ensures that when a user is editing a document, others cannot modify it until the lock is released.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Request body containing document and user details",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "userId": "MOHAMMED",
                                              "sessionDetails": "1TE23",
                                              "docId": "400-001",
                                              "docName": "Collection Outlets Master",
                                              "docKeyPoid": 54,
                                              "type": "E"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lock acquired successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                            {
                                              "success": true,
                                              "message": "Lock acquired successfully",
                                              "statusCode": 200,
                                              "result": {
                                                "data": "Locked By :- MOHAMMED, Locked Date :- 2025-10-29T17:00:23"
                                              }
                                            }
                                            """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request parameters (e.g., missing required fields)"
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Document is already locked by another user"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error while acquiring the lock"
                    )
            }
    )

    @PostMapping("/acquire-lock")
    public ResponseEntity<?> acquireLock(
            @Parameter(description = "Acquire lock request payload", required = true)
            @Valid @RequestBody DocAcquireLockRequestDto request

    ) {

        String status = documentLockService.acquireLock(request);
        log.info("status : {}", status);

        if (status == null || status.isBlank()) {
            return internalServerError("Failed to acquire lock");
        }

        String normalized = status.trim();
        if (normalized.startsWith("Locked By")) {
            return conflict("Document is already locked by another user");
        }
        if (normalized.toUpperCase().startsWith("ERROR")) {
            return conflict(normalized);
        }

        return success("Lock acquired successfully", status);
    }

    @Operation(
            summary = "Session update (LOGIN / UPDATE / LOGOUT)",
            description = "Single endpoint to manage session lifecycle via DB procedure: pass status as LOGIN, UPDATE or LOGOUT. UPDATE serves as heartbeat to keep session/lock alive.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Payload with status and session context",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "userId": "MOHAMMED",
                                              "sessionId": "1TE23",
                                              "sessionIp": "10.0.0.1",
                                              "sessionBrowser": "Chrome 130",
                                              "lastPageVisited": "/gl/cheque-cash",
                                              "status": "UPDATE"  // also supports LOGIN or LOGOUT
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Session updated",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                            {
                                              "success": true,
                                              "message": "Session/lock updated",
                                              "statusCode": 200,
                                              "result": { "data": true }
                                            }
                                            """)
                            )
                    )
            }
    )
    @PostMapping("/update-lock")
    public ResponseEntity<?> updateLock(
            @Parameter(description = "Heartbeat request payload", required = true)
            @Valid @RequestBody DocUpdateLockRequestDto request
    ) {
        String status = documentLockService.updateLock(request);
        log.info("heartbeat status : {}", status);
        return success("Lock heartbeat updated", status);
    }

}
