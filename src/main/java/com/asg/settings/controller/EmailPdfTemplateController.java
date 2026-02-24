package com.asg.settings.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.settings.dto.EmailPdfTemplateDto;
import com.asg.settings.entity.EmailPdfTemplateMasterEntity;
import com.asg.settings.service.EmailPdfTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.asg.common.lib.dto.response.ApiResponse.*;

@RestController
@RequestMapping("/v1/email-pdf-template")
@RequiredArgsConstructor
public class EmailPdfTemplateController {

    private final EmailPdfTemplateService service;
    private final LoggingService loggingService;

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @Operation(
            summary = "List Email PDF Templates with Search and Sort",
            description = "Provide search filters. Valid searchField values: GLOBALSEARCH or (TEMPLATE_NAME, TYPE, DOC_ID)"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = """
                    - ### Filters:
                      Use either:
                      1. A single GLOBALSEARCH filter, OR
                      2. Any combination of specific fields (TEMPLATE_NAME, TYPE, DOC_ID)
                      3. operator field will either have "AND" or "OR", if not given will be considered as "OR"
                      4. isDeleted when 'N' or null, will search and return non deleted records, 'Y' will check and return deleted records
                    """,
            content = @Content(
                    schema = @Schema(implementation = FilterRequestDto.class),
                    examples = {
                            @ExampleObject(
                                    name = "Template Filters",
                                    value = """
                                             {
                                             "operator": "OR",
                                             "isDeleted": "N",
                                             "filters": [
                                                { "searchField": "GLOBALSEARCH", "searchValue": "Invoice" },
                                                { "searchField": "TEMPLATE_NAME", "searchValue": "Invoice Template" },
                                                { "searchField": "TYPE", "searchValue": "PDF & Email" }
                                             ]
                                             }
                                            """
                            )
                    }
            )
    )
    @PostMapping("/list")
    public ResponseEntity<?> listTemplates(
            @ParameterObject Pageable pageable,
            @RequestBody(required = false) FilterRequestDto filters) {
        try {
            Map<String, Object> templates = service.listTemplates(UserContext.getDocumentId(), filters, pageable);
            return success("Template list fetched successfully", templates);
        } catch (Exception e) {
            return internalServerError("Unable to fetch template list: " + e.getMessage());
        }
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @Operation(summary = "Get Template Details by ID")
    @GetMapping("/details/{templatePoid}")
    public ResponseEntity<?> getTemplateDetails(@PathVariable Long templatePoid) {
        try {
            EmailPdfTemplateDto data = service.getTemplateById(templatePoid);
            loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(), templatePoid.toString());
            return success("Template fetched successfully", data);
        } catch (Exception ex) {
            return internalServerError("Failed to retrieve template details: " + ex.getMessage());
        }
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @Operation(
            summary = "Create Email PDF Template",
            description = "Create a new email/PDF template configuration"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = """
                    ### Request Body
                        - **templatePoid:** Must be null when creating
                        - **templateName:** (Required) Name of the template
                        - **type:** (Required) PDF & Email, Email Only, or PDF Only
                        - **templateDocId:** Document ID reference
                        - **emailSubject:** Email subject line
                        - **emailContent:** (Required if type includes Email) HTML content for email
                        - **pdfContent:** (Required if type includes PDF) HTML content for PDF
                        - **fieldsToUse:** Comma-separated field names
                        - **sqlQuery:** (Required) SQL query with field placeholders
                    """,
            content = @Content(
                    schema = @Schema(implementation = EmailPdfTemplateDto.class),
                    examples = {
                            @ExampleObject(
                                    name = "Create Template Example",
                                    value = """
                                            {
                                              "templateDocId": "000-024",
                                              "templateName": "Invoice Email Template",
                                              "type": "PDF & Email",
                                              "emailSubject": "Invoice #INVOICE_NO#",
                                              "emailContent": "<html><body>Dear #CUSTOMER_NAME#, Please find attached invoice #INVOICE_NO#</body></html>",
                                              "pdfContent": "<html><body><h1>Invoice #INVOICE_NO#</h1></body></html>",
                                              "fieldsToUse": "CUSTOMER_NAME, INVOICE_NO, AMOUNT",
                                              "sqlQuery": "SELECT customer_name, invoice_no, amount FROM invoices WHERE invoice_id = #INVOICE_ID#",
                                              "seqNo": 1,
                                              "active": "Y"
                                            }
                                            """
                            )
                    }
            )
    )
    @PostMapping("/create")
    public ResponseEntity<?> createTemplate(@RequestBody @Valid EmailPdfTemplateDto dto) {
        try {
            if (dto.getTemplatePoid() != null) {
                return badRequest("templatePoid must be null when creating");
            }

            EmailPdfTemplateMasterEntity saved = service.createOrUpdateTemplate(dto);

            Map<String, Object> data = new HashMap<>();
            data.put("templatePoid", saved.getTemplatePoid());
            data.put("templateName", saved.getTemplateName());

            return success("Template created successfully", data);
        } catch (Exception ex) {
            return internalServerError(ex.getMessage());
        }
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @Operation(
            summary = "Update Email PDF Template",
            description = "Update an existing email/PDF template configuration"
    )
    @PutMapping("/update/{templatePoid}")
    public ResponseEntity<?> updateTemplate(
            @PathVariable Long templatePoid,
            @RequestBody @Valid EmailPdfTemplateDto dto) {
        try {
            dto.setTemplatePoid(templatePoid);
            EmailPdfTemplateMasterEntity saved = service.createOrUpdateTemplate(dto);

            Map<String, Object> data = new HashMap<>();
            data.put("templatePoid", saved.getTemplatePoid());
            data.put("templateName", saved.getTemplateName());

            return success("Template updated successfully", data);
        } catch (Exception ex) {
            return internalServerError(ex.getMessage());
        }
    }

    @AllowedAction(UserRolesRightsEnum.DELETE)
    @Operation(summary = "Delete Template")
    @DeleteMapping("/delete")
    public ResponseEntity<?> softDeleteTemplate(
            @RequestParam Long templatePoid,
            @RequestBody com.asg.common.lib.dto.DeleteReasonDto deleteReasonDto) {
        try {
            service.softDeleteTemplate(templatePoid, deleteReasonDto);
            return success("Template deleted successfully", Map.of("templatePoid", templatePoid));
        } catch (Exception e) {
            return internalServerError("Failed to delete template: " + e.getMessage());
        }
    }
}
