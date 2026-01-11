package com.asg.settings.controller;

import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.security.util.UserContext;
import com.asg.settings.dto.request.DivisionCreateRequest;
import com.asg.settings.dto.request.DivisionUpdateRequest;
import com.asg.settings.dto.response.DivisionResponse;
import com.asg.settings.service.DivisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static com.asg.common.lib.dto.response.ApiResponse.*;


@RestController
@RequestMapping("/v1/division")
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "division-controller",
        description = "Manage Division Master records with full CRUD, activate, deactivate, and soft delete. DocId: 000-017"
)
public class DivisionController {

    @Autowired
    private DivisionService divisionService;

    @Operation(
            summary = "Create Division (DocId: 000-017)",
            description = """
                    Create a new division in the system.
                    
                    ### Validation
                    - Division code must be unique (case-insensitive).
                    - Required fields: `divisionCode`, `divisionName`, `active`, `createdBy`
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Provide division details to create a new record.",
            content = @Content(
                    schema = @Schema(implementation = DivisionCreateRequest.class),
                    examples = @ExampleObject(
                            name = "Division Create Example",
                            value = """
                                    {
                                      "divisionCode": "FIN01",
                                      "divisionName": "Finance",
                                      "active": "Y",
                                      "seqNo": 1,
                                      "createdBy": "Admin",
                                      "remarks": "Handles finance operations"
                                    }
                                    """
                    )
            )
    )
    @PostMapping("/create")
    public ResponseEntity<?> createDivision(@Valid @RequestBody DivisionCreateRequest request) {
        if (divisionService.existsByDivisionCodeAndDeleted(request.getDivisionCode(), "0")) {
            return conflict("Division code already exists");
        }
        DivisionResponse created = divisionService.createDivision(request);
        return success("Division created successfully", created);
    }

    @Operation(
            summary = "List Divisions with Search and Sort (DocId: 000-017)",
            description = "Provide search filters. Valid `searchField` values: GLOBALSEARCH or (DIVISION_CODE, DIVISION_NAME, ACTIVE, CREATED_BY). Sorting default on divisionId, desc." +
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
                      2. Any combination of specific fields (DIVISION_CODE, DIVISION_NAME, ACTIVE, CREATED_BY).
                      3. operator field will either have "AND" or "OR", if not given will be considered as "OR",
                         not required for GLOBALSEARCH, for non GLOBALSEARCH need to give only 1 time
                      4. isDeleted when 'N' or null, will search and return non deleted records, 'Y' will check and return deleted records
                      5. sort will be default on primary key ascending if specified in db field otherwise you can override it giving the field name and the direction.
                    
                    - #### Global Search:
                      Apply one search term across multiple fields.
                      No operator need to send in this case.
                      • { "searchField": "GLOBALSEARCH", "searchValue": "Finance" }
                    
                    - #### Single Field, Single Value:
                      Search one field with one value.
                      • { "searchField": "DIVISION_CODE", "searchValue": "FIN01" },
                    
                    - #### Single Field, Multiple Values:
                      Provide multiple values for the same field, separated by `|`.
                      • { "searchField": "DIVISION_CODE", "searchValue": "FIN01|HR01" }
                    
                    - #### Multiple Different Fields, Single or Multiple Value:
                      Provide one or multiple search values across multiple fields.
                      • { "searchField": "DIVISION_CODE", "searchValue": "FIN01" },
                      • { "searchField": "DIVISION_NAME", "searchValue": "Finance|HR" }
                      • "operator" :  "AND"/"OR"
                    
                    - ### Sorting:
                      Defaults to whatever specified in list_of_records_sql field mostly primary key ascending.
                      To override, pass `sort=<field>,ASC|DESC` in query params.
                      Examples:
                      • sort=DIVISION_NAME,ASC
                      • sort=DIVISION_CODE,DESC
                    """,
            content = @Content(
                    schema = @Schema(implementation = FilterRequestDto.class),
                    examples = {
                            @ExampleObject(
                                    name = "Division Filters",
                                    value = """
                                            {
                                            "operator": "AND",
                                            "isDeleted": "N",
                                            "filters": [
                                               { "searchField": "GLOBALSEARCH", "searchValue": "Finance" },
                                               { "searchField": "DIVISION_CODE", "searchValue": "FIN01|HR01" },
                                               { "searchField": "DIVISION_NAME", "searchValue": "Finance"},
                                               { "searchField": "ACTIVE", "searchValue": "Y"},
                                               { "searchField": "CREATED_BY", "searchValue": "Admin"}
                                            ]
                                            }
                                            """
                            )
                    }
            )
    )
    @PostMapping("/list")
    public ResponseEntity<?> getDivisions(@ParameterObject Pageable pageable,
                                          @RequestBody(required = false) FilterRequestDto filters) {
        try {
            Map<String, Object> data = divisionService.listDivisions(UserContext.getDocumentId(), filters, pageable);
            return success("Divisions fetched successfully", data);
        } catch (Exception ex) {
            return internalServerError("Unable to fetch division list: " + ex.getMessage());
        }
    }

    @Operation(
            summary = "Get Division By Id (DocId: 000-017)",
            description = """
                    Fetch a division by its primary key.
                    """
    )
    @GetMapping("/{id}")
    public ResponseEntity<?> getDivisionById(@PathVariable @Parameter(description = "Division ID", required = true) Long id) {
        Optional<DivisionResponse> divisionOpt = divisionService.getDivisionById(id);
        if (divisionOpt.isPresent()) {
            return success("Division found", divisionOpt.get());
        } else {
            return notFound("Division not found");
        }
    }

    @Operation(
            summary = "Update Division (DocId: 000-017)",
            description = """
                    Update an existing division.
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Provide division details to update an existing record.",
            content = @Content(
                    schema = @Schema(implementation = DivisionUpdateRequest.class),
                    examples = @ExampleObject(
                            name = "Division Update Example",
                            value = """
                                    {
                                      "divisionName": "Finance & Accounts",
                                      "remarks": "Updated remark",
                                      "seqNo": 2,
                                      "active": "N",
                                      "updatedBy": "Admin"
                                    }
                                    """
                    )
            )
    )
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDivision(@PathVariable
                                            @Parameter(description = "Division ID", required = true) Long id,
                                            @Valid @RequestBody DivisionUpdateRequest request) {
        try {
            return success("Division updated successfully",
                    divisionService.updateDivision(id, request));
        } catch (org.springframework.web.server.ResponseStatusException ex) {
            if (ex.getStatusCode().value() == 404) {
                return notFound("Division not found");
            }
            throw ex;
        }
    }

    @Operation(
            summary = "Soft Delete Division (DocId: 000-017)",
            description = """
                    Soft delete a division (mark as deleted without physical deletion).
                    """
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDeleteDivision(@PathVariable
                                                @Parameter(description = "Division ID", required = true) Long id,
                                                @RequestParam @Parameter(description = "Updated by") String updatedBy) {
        divisionService.softDeleteDivision(id, updatedBy);
        return success("Division deleted successfully", Map.of("deleted", true));
    }

    @Operation(
            summary = "Activate Division (DocId: 000-017)",
            description = """
                    Activate a division.
                    """
    )
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateDivision(@PathVariable
                                              @Parameter(description = "Division ID", required = true) Long id,
                                              @RequestParam @Parameter(description = "Updated by") String updatedBy) {
        divisionService.activateDivision(id, updatedBy);
        return success("Division activated successfully", Collections.emptyMap());
    }

    @Operation(
            summary = "Deactivate Division (DocId: 000-017)",
            description = """
                    Deactivate a division.
                    """
    )
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateDivision(@PathVariable
                                                @Parameter(description = "Division ID", required = true) Long id,
                                                @RequestParam @Parameter(description = "Updated by") String updatedBy) {
        divisionService.deactivateDivision(id, updatedBy);
        return success("Division deactivated successfully", Collections.emptyMap());
    }
}

