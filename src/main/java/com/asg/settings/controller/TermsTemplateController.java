package com.asg.settings.controller;


import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.settings.dto.TermsTemplateDtlDto;
import com.asg.settings.dto.TermsTemplateDto;
import com.asg.settings.dto.response.TemplateResponseDto;
import com.asg.settings.service.TermsTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.asg.common.lib.dto.response.ApiResponse.internalServerError;
import static com.asg.common.lib.dto.response.ApiResponse.success;

@Slf4j
@RestController
@RequestMapping("/v1/terms")
@RequiredArgsConstructor
@Tag(name = "Terms & Conditions", description = "APIs for Terms Template and Conditions Management")
public class TermsTemplateController {

    private final TermsTemplateService termsTemplateService;
    private final LoggingService loggingService;

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @Operation(summary = "List Terms Templates with Search and Sort", description = "Provide search filters. Valid `searchField` values: GLOBALSEARCH or (TEMPLATE_ID, DOC_ID, TEMPLATE_NAME, TERMS_CATEGORY). Sorting default on termsPoid, desc." + "Will be searched in all available fields given in list_of_records_sql or main_table field in doc_master table." + "Sorting will be applied as specified in list_of_records_sql in doc_master table." + "Display fields for showing columns can be customized through list_of_display_columns_and_types field in doc_master.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = """
              - ### Filters:
                Use either:
                1. A single `GLOBALSEARCH` filter, OR
                2. Any combination of specific fields (TEMPLATE_ID, DOC_ID, TEMPLATE_NAME, TERMS_CATEGORY).
                3. operator field will either have "AND" or "OR", if not given will be considered as "OR"
                4. isDeleted when 'N' or null, will search and return non deleted records, 'Y' will check and return deleted records.  
            """, content = @Content(schema = @Schema(implementation = FilterRequestDto.class), examples = {@ExampleObject(name = "Terms Template Filters", value = """
            {
                "operator":"OR",
                "isDeleted":"N",
                "filters":[
                    {
                        "searchField":"TEMPLATE_NAME",
                        "searchValue":"Customer Credit Agrement"
                    },
                    {
                        "searchField":"TERMS_CATEGORY",
                        "searchValue":"SALES_QUOTATION_FF"
                    }
                ]
            }
            """)}))
    @PostMapping("/list")
    public ResponseEntity<?> getTermsTemplateList(@ParameterObject Pageable pageable, @RequestBody(required = false) FilterRequestDto filters) {
        try {
            Map<String, Object> companies = termsTemplateService.listTerms(UserContext.getDocumentId(), filters, pageable);
            return success("Terms fetched successfully", companies);
        } catch (Exception ex) {
            return internalServerError("Failed to list terms: " + ex.getMessage());
        }
    }


    @Operation(summary = "Update Terms and Clauses", description = "Updating an existing Terms and Clauses. This endpoint allows you to update Terms and Clauses." + " The `termsPoid` is required to identify the parameter to be updated. The updated details are provided in the request body." + " For clauses, use actionType: 'isCreated' for new clauses, 'isUpdated' for modifications, 'isDeleted' for soft deletion." + " The response indicates the success or failure of the update operation." + "\n\n### Required Headers: loginUserPoid=3371")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Terms template details with clauses to be updated. Use actionType in clauses: 'isCreated', 'isUpdated', 'isDeleted', 'noChange'", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TermsTemplateDto.class), examples = @ExampleObject(name = "Update Terms Template with Clause Actions Example", value = """
            {
                "termsPoid": 406,
                "groupPoid": 1,
                "docId": "400-102",
                "templateName": "Test Terms And Condition template 001",
                "active": "Y",
                "seqNo": 1,
                "deleted": "N",
                "remarks": "Remarks of Test Terms And Condition template 001",
                "termsCategory": "2",
                "clauses": [
                    {
                        "detRowId": 1762516428950,
                        "clauseNo": "001",
                        "clauseDetails": "Test clause 01 updt",
                        "active": "Y",
                        "actionType": "isUpdated"
                    },
                    {
                        "detRowId": 1762516428951,
                        "clauseNo": "002",
                        "clauseDetails": "Test clause 02",
                        "active": "Y",
                        "actionType": "isDeleted"
                    }
                ]
            }
            """)))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successfully updated the terms template", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TemplateResponseDto.class))), @ApiResponse(responseCode = "400", description = "Bad Request"), @ApiResponse(responseCode = "500", description = "Internal server error")})
    @PutMapping("/{termsPoid}")
    public ResponseEntity<?> updateTemplateMetadata(@Parameter(description = "termsPoid identifier", example = "208", required = true) @PathVariable Long termsPoid, @Parameter(description = "loginUserPoid identifier", example = "3371", required = true) @RequestHeader("loginUserPoid") String loginUserPoid, @Valid @RequestBody TermsTemplateDto request) {
        try {
            TemplateResponseDto updatedTemplate = termsTemplateService.updateTemplateMetadata(termsPoid, request, loginUserPoid);
            loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(), termsPoid.toString());
            return success("Template updated successfully", updatedTemplate);
        } catch (Exception ex) {
            return internalServerError("Failed to update template: " + ex.getMessage());
        }
    }


    @Operation(summary = "Create New Terms Template with Clauses", description = "Creates a new terms template along with its associated clauses. " + "The template will be created with the provided details and associated with the specified group. " + "**Note:** templateId is auto-generated by database trigger and should not be provided in the request.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Terms template details with clauses to be created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TermsTemplateDto.class), examples = @ExampleObject(name = "Create Terms Template Example", value = """
            {
                "docId": "400-007",
                "templateName": "New Sales Terms Template",
                "remarks": "This is a new sales terms template",
                "active": "Y",
                "seqNo": 1,
                "termsCategory": "TTRM",
                "clauses": [
                    {
                        "clauseNo": "001",
                        "clauseDetails": "This is the first TTRM sales",
                        "active": "Y"
                    },
                    {
                        "clauseNo": "002",
                        "clauseDetails": "This is the second TTRM sales",
                        "active": "Y"
                    }
                ]
            }
            """))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successfully created the terms template", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TemplateResponseDto.class), examples = @ExampleObject(value = """
            {
                    "termsPoid": 209,
                    "templateId": "AUTO_GENERATED_ID_123",
                    "docId": "400-007"
            }
            """))), @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input or missing required fields", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {
                "status": "error",
                "message": "Validation failed",
                "errors": [
                    "templateName: templateName is required",
                    "docId: docId is required",
                    "termsCategory: termsCategory is required"
                ]
            }
            """))), @ApiResponse(responseCode = "500", description = "Internal server error occurred while processing the request")})
    @PostMapping
    public ResponseEntity<?> createTermsAndConditionsTemplate(@Parameter(description = "groupPoid identifier", example = "1", required = true) @RequestHeader("groupPoid") Long groupPoid, @Parameter(description = "loginUserPoid identifier", example = "123", required = true) @RequestHeader("loginUserPoid") String loginUserPoid, @Valid @RequestBody TermsTemplateDto request) {

        TemplateResponseDto templateResponseDto = termsTemplateService.addTemplateAndClause(request, groupPoid, loginUserPoid);

        return success("Template created successfully", templateResponseDto);
    }

    @Operation(summary = "Get Terms Templates And Clauses by termsPoid", description = "Retrieves terms template and associated clauses based on the provided termsPoid. " + "The response includes all template details along with its clauses.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Terms Templates and Clauses retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TermsTemplateDto.class))), @ApiResponse(responseCode = "404", description = "Terms Templates and Clauses not found"), @ApiResponse(responseCode = "500", description = "Internal server error")})
    @GetMapping("/{termsPoid}")
    public ResponseEntity<?> getTermsTemplateAndClauses(@Parameter(description = "Unique identifier of the terms template", required = true, example = "30") @PathVariable Long termsPoid) {
        TermsTemplateDto termsTemplateDto = termsTemplateService.getTermsTemplateAndClauses(termsPoid);
        return success("Terms & clauses fetched successFully ", termsTemplateDto);
    }

    @Operation(summary = "Delete the Terms & Conditions Template By termsPoid", description = "Soft Delete the Terms & Conditions By termsPoid if its deleted then delete the associated Clauses")
    @DeleteMapping("/{termsPoid}")
    public ResponseEntity<?> softDeleteTemplate(@Parameter(description = "Unique identifier of the terms template", required = true, example = "30") @PathVariable Long termsPoid,
                                                @RequestBody(required = false) DeleteReasonDto deleteReasonDto) {
        termsTemplateService.softDeleteByTermsPoid(termsPoid, deleteReasonDto);
        return success("Terms & clauses deleted successfully ");
    }


    @DeleteMapping("/{termsPoid}/clause/{clauseNo}")
    public ResponseEntity<?> softDeleteTermsAndClauses(@Parameter(description = "Unique identifier of the Terms template", example = "30", required = true) @PathVariable Long termsPoid,

                                                       @Parameter(description = "Clause number to be soft deleted", example = "001", required = true) @PathVariable String clauseNo) {
        List<TermsTemplateDtlDto> termsTemplateDto = termsTemplateService.softDeleteClause(termsPoid, clauseNo);
        return success("Clause deleted successfully ", termsTemplateDto);
    }

    @Operation(summary = "Add new Clause to the Given Terms & Conditions Template", description = "Add new Clause to the Given Terms & Conditions Template")
    @PostMapping("/{termsPoid}/clause")
    public ResponseEntity<?> addClauseToTemplate(@Parameter(description = "Unique identifier of the Terms template", example = "30", required = true) @PathVariable Long termsPoid, @Valid @RequestBody TermsTemplateDtlDto dto) {
        dto.setTermsPoid(termsPoid);
        TermsTemplateDtlDto termsTemplateDtlDto = termsTemplateService.addClause(termsPoid, dto);
        return success("Clause added to template successfully ", termsTemplateDtlDto);
    }
}

