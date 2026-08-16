package com.asg.settings.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.exception.ValidationException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.settings.dto.ApprovalActionRequest;
import com.asg.settings.dto.request.GrantEditPermissionRequest;
import com.asg.settings.dto.DocumentDto;
import com.asg.common.lib.dto.DropdownStringDto;
import com.asg.settings.dto.request.UpdateDocumentRequest;
import com.asg.settings.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.asg.common.lib.dto.response.ApiResponse.*;

@RestController
@RequestMapping("/v1/document")
public class DocumentController {

    @Autowired
    private DocumentService documentService;
    @Autowired
    private LoggingService loggingService;

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @Operation(
            summary = "List Documents with Search and Sort",
            description = "Provide search filters. Valid `searchField` values: GLOBALSEARCH or (DOC_ID,DOC_SHORT_NAME,DOC_TYPE,MODULE_ID). Sorting default on docId, desc." +
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
                      2. Any combination of specific fields (DOC_ID,DOC_SHORT_NAME,DOC_TYPE,MODULE_ID).
                      3. operator field will either have "AND" or "OR", if not given will be considered as "OR"
                      4. isDeleted when 'N' or null, will search and return non deleted records, 'Y' will check and return deleted records
                    """,
            content = @Content(
                    schema = @Schema(implementation = FilterRequestDto.class),
                    examples = {
                            @ExampleObject(
                                    name = "Document Filters",
                                    value = """
                                            {
                                                "operator":"OR",
                                                "isDeleted":"N",
                                                "filters":[
                                                    {
                                                        "searchField":"DOC_ID",
                                                        "searchValue":"000-005"
                                                    },
                                                    {
                                                        "searchField":"DOC_SHORT_NAME",
                                                        "searchValue":"Users"
                                                    }
                                                ]
                                            }
                                            """
                            )
                    }
            )
    )
    @PostMapping("/list")
    public ResponseEntity<?> getDocumentList(@ParameterObject Pageable pageable,
                                             @RequestBody(required = false) FilterRequestDto filters) {
        try {
            Map<String, Object> users = documentService.listDocuments(UserContext.getDocumentId(), filters, pageable);

            return success("Documents fetched successfully", users);

        } catch (Exception e) {
            return internalServerError("Unable to fetch documents list: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Get Document Details",
            description = """
                        Fetch Document details by `docId`.
                        Use `includeSql=true` to include SQL fields (for internal/admin use).
                        Default is `false` (SQL fields hidden for security).
                    """
    )
    @GetMapping("/details")
    public ResponseEntity<?> getDocumentById(
            @RequestParam String docId,
            @RequestParam(required = false, defaultValue = "false") Boolean includeSql) {
        DocumentDto document = documentService.getDocumentById(docId, includeSql);
        loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(), document.getDocPoid().toString());
        return success("success", document);
    }

    @Operation(
            summary = "Get searchable fields ",
            description = """
                    to get searchable fields for given document Id.
                    ###  Parameters
                        - **documentId:** Document Id of the document for which you require the searchable fields.
                        - **actionRequested:** Action being performed (`VIEW`)
                    """
    )
    @GetMapping("/searchable-fields")
    public ResponseEntity<?> getSearchableFields(@RequestParam String documentId) {
        try {
            List<DropdownStringDto> response = documentService.getSearchableFieldsForDropdown(documentId);
            if (response == null) {
                return internalServerError("Fields not found");
            }
            return success("success", response);
        } catch (Exception e) {
            return internalServerError("Error fetching Fields: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Delete Document",
            description = """
                    Deletes a DocId based on the provided parameters.
                    
                    ### Path variable
                        - **docId:** Document's Primary Key (optional)
                    """
    )

    @DeleteMapping("/delete/{docId}")
    public ResponseEntity<?> deleteCompany(
            @PathVariable("docId") String docId,
            @RequestBody(required = false) DeleteReasonDto deleteReasonDto
    ) {
        try {
            if (docId == null || docId.isBlank()) {
                throw new ValidationException("Document Id is needed to delete");
            }

            documentService.deleteDocument(docId, deleteReasonDto);

            Map<String, Object> data = new HashMap<>();
            data.put("documentId", docId);

            return success("Document Master deleted successfully", data);
        } catch (Exception ex) {
            return internalServerError("Failed to delete Document Master: " + ex.getMessage());
        }
    }

    @Operation(
            summary = "Get Deleted Documents",
            description = "Retrieves a list of all documents that are marked as deleted (active = 'N' and deleted = 'Y')",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved deleted documents",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = DocumentDto.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request parameters",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error while fetching deleted documents",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @GetMapping("/deleted-documents")
    public ResponseEntity<?> getInactiveAndDeletedDocuments() {
        try {
            List<DocumentDto> documents = documentService.getInactiveAndDeletedDocuments();
            return success("Successfully retrieved inactive and deleted documents", documents);
        } catch (Exception e) {
            return internalServerError("Error fetching inactive and deleted documents: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Perform Approval Action on Document",
            description = "Submit, approve, reject, or check status of a document via stored procedure."
    )
    @PostMapping("/{docId}/approval")
    public ResponseEntity<?> performApprovalAction(
            @PathVariable String docId,
            @RequestBody ApprovalActionRequest request) {
        try {
            Map<String, Object> response = documentService.performApprovalAction(docId, request);
            return success("Approval action executed successfully", response);
        } catch (ValidationException e) {
            return badRequest("Validation Error: " + e.getMessage());
        } catch (Exception e) {
            return internalServerError("Error executing approval action: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Get Temporary Edit Permission Status",
            description = "Checks if the current user has an active temporary edit grant for the given document record (calls PROC_GLOBAL_DOC_EDIT_RIGHT_GET)."
    )
    @GetMapping("/{docId}/grant-edit-permission/status")
    public ResponseEntity<?> getGrantEditPermissionStatus(
            @PathVariable String docId,
            @RequestParam Long docKeyPoid) {
        try {
            boolean hasAccess = documentService.getGrantEditPermissionStatus(docId, docKeyPoid);
            Map<String, Object> data = new HashMap<>();
            data.put("hasTemporaryEditAccess", hasAccess);
            return success("Status fetched successfully", data);
        } catch (Exception e) {
            return internalServerError("Error checking grant edit permission status: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Give Temporary Edit Access",
            description = "Grants temporary edit access (8 hours) for the selected document record (calls PROC_GLOBAL_DOC_EDIT_RIGHT_SET). Requires permission 000-070 View."
    )
    @PostMapping("/{docId}/grant-edit-permission")
    public ResponseEntity<?> grantEditPermission(
            @PathVariable String docId,
            @RequestBody GrantEditPermissionRequest request) {
        try {
            if (request.getDocKeyPoid() == null) {
                return badRequest("Validation Error: docKeyPoid is required");
            }
            String status = documentService.grantEditPermission(docId, request.getDocKeyPoid(), request.getReason(), request.getApprovalStatus());
            if (status != null && status.contains("SUCCESS")) {
                return success(status);
            }
            return badRequest(status);
        } catch (Exception e) {
            return internalServerError("Error granting edit permission: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Update Document",
            description = """
                    Update an existing document identified by documentKeyPoid (DOC_POID).
                    Performs backend validations and edit-right checks.
                    
                    ### Path Variable
                    - **documentKeyPoid:** Document's Primary Key (DOC_POID) - must be numeric (e.g., 54)
                    ### Example Usage
                    PUT /v1/document/54
                    """
    )
    @PutMapping("/{documentKeyPoid}")
    public ResponseEntity<?> updateDocument(
            @PathVariable("documentKeyPoid") Long documentKeyPoid,
            @Valid @RequestBody UpdateDocumentRequest request) {
        Map<String, Object> result = documentService.updateDocument(documentKeyPoid, request);
        return success("Document updated successfully", result);
    }
}