package com.asg.settings.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.exception.ValidationException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.dto.CompanyDto;
import com.asg.common.lib.entity.Company;
import com.asg.settings.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.asg.common.lib.dto.response.ApiResponse.*;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/companies")

public class CompanyController {

    private final CompanyService companyService;

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @Operation(
            summary = "List Companies with Search and Sort",
            description = "Provide search filters. Valid `searchField` values: GLOBALSEARCH or (COMPANY_CODE, COMPANY_NAME, EMAIL, TELEPHONE, CONTACT_PERSON). Sorting default on companyPoid, desc." +
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
                      2. Any combination of specific fields (COMPANY_CODE, COMPANY_NAME, EMAIL, TELEPHONE, CONTACT_PERSON).
                      3. operator field will either have "AND" or "OR", if not given will be considered as "OR"
                      4. isDeleted when 'N' or null, will search and return non deleted records, 'Y' will check and return deleted records
                    
""",
            content = @Content(
                    schema = @Schema(implementation = FilterRequestDto.class),
                    examples = {
                            @ExampleObject(
                                    name = "Company Filters",
                                    value = """
                                            {
                                            "operator": "AND",
                                            "isDeleted": "N",
                                            "filters": [
                                               { "searchField": "GLOBALSEARCH", "searchValue": "SHS" },
                                               { "searchField": "COMPANY_CODE", "searchValue": "SHS" },
                                               { "searchField": "COMPANY_NAME", "searchValue": "TEST-Seahorse Shipping Agency W.L.L." },
                                               { "searchField": "EMAIL", "searchValue": "cfo@SHIPPINGBAHRAIN.COM" },
                                               { "searchField": "TELEPHONE", "searchValue": "17515050" },
                                               { "searchField": "CONTACT_PERSON", "searchValue": "Manu C Joy" }
                                            ]
                                            }
                                            """
                            )
                    }
            )
    )
    @PostMapping("/list")
    public ResponseEntity<?> getCompanyList(
            @ParameterObject Pageable pageable,
            @RequestBody(required = false) FilterRequestDto filters
    ) {
        try {
            Map<String, Object> companies = companyService.listCompanies(UserContext.getDocumentId(), filters, pageable);
            return success("Companies list fetched successfully", companies);
        } catch (Exception ex) {
            return internalServerError("Failed to list company: " + ex.getMessage());
        }

    }

    @GetMapping("/details")
    public ResponseEntity<?> getCompanyDetails(
            @RequestParam(required = true) Long companyPoid
    ) {
        try {
            // Process company with divisions having individual action types
            CompanyDto company = companyService.getCompany(companyPoid);
            return success("Company details fetched successfully", company);
        } catch (Exception ex) {
            return internalServerError("Failed to fetch company: " + ex.getMessage());
        }
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @Operation(
            summary = "Create Company",
            description = "Create a new company based on the request payload."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = """
                    ### Request Body
                        Provide company details.
                        - **companyPoid:** If `null`, a new company will be created.
                        - Other fields as applicable for company setup.
                    
                    """,
            content = @Content(
                    schema = @Schema(implementation = Company.class),
                    examples = {
                            @ExampleObject(
                                    name = "Company Create Example",
                                    value = """
                                            {
                                              "groupPoid": 1,
                                              "companyPoid": null,
                                              "companyCode": "DEF9",
                                              "companyName": "Crown Shipping Agency & Logistics WLL 10",
                                              "companyName2": "CSA-LOGISTICS2",
                                              "contactPerson": "Manu C Joy Updated2",
                                              "telephone": "17515555",
                                              "fax": "17911999",
                                              "email": "operations@shippingbahrain.com",
                                              "countryId": "18",
                                              "stateId": "337",
                                              "address": "P.O. Box 12000, Manama, Kingdom of Bahrain",
                                              "financialPeriodStart": "2025-01-01T00:00:00",
                                              "financialPeriodEnd": "2025-12-31T00:00:00",
                                              "reportPeriodStart": "2025-01-01T00:00:00",
                                              "reportPeriodEnd": "2025-12-31T00:00:00",
                                              "active": "Y",
                                              "seqNo": 8,
                                              "createdBy": "system",
                                              "createdDate": null,
                                              "lastModifiedBy": "admin_user",
                                              "lastModifiedDate": "2025-08-12T11:45:00",
                                              "deleted": null,
                                              "transPeriodStart": "2025-04-01T00:00:00",
                                              "transPeriodEnd": "2025-12-31T00:00:00",
                                              "provisionalClosedDate": "2018-01-31T00:00:00",
                                              "bankDetail": "National Bank of Bahrain, IBAN BH12NBOB00000001234567",
                                              "bankPoid": 350,
                                              "tinNumber": "9",
                                              "vatRegistrationDate": "2020-01-01T00:00:00",
                                              "vatLastFiledDate": "2025-04-15T00:00:00",
                                              "accountPerson": "Anish Kumar",
                                              "stockPeriodStart": "2025-01-01T00:00:00",
                                              "stockPeriodEnd": "2025-12-31T00:00:00",
                                              "vatFilingPeriod": "3",
                                              "accountEmail": "accounts@shippingbahrain.com",
                                              "vatLastFiledBy": "Arun Joseph",
                                              "vatLastFiledCreatedDate": "2025-04-28T16:48:04",
                                              "financialDateUpdatedBy": "MANOJ",
                                              "financialDateUpdatedDate": "2025-02-04T12:06:36",
                                              "transDateUpdatedBy": "MANOJ",
                                              "transDateUpdatedDate": "2025-06-21T13:21:00",
                                              "reportDateUpdatedBy": "MANOJ",
                                              "reportDateUpdatedDate": "2025-02-04T12:06:42",
                                              "inventoryDateUpdatedBy": null,
                                              "inventoryDateUpdatedDate": null,
                                              "dateFormat": "DD-MM-YYYY",
                                              "timezoneId": 101,
                                              "currencyPoid": 120,
                                              "submissionPeriod": 202501,
                                              "divisions": [
                                                  {
                                                    "divPoid": 3,
                                                    "remarks": "ASG-SHIPPING Updated3",
                                                    "lastModifiedBy": "admin_user",
                                                    "lastModifiedDate": "2025-08-12T10:30:00",
                                                    "companyDivAddress": "Flat No. 45, Building 2020B, Road 220, SEEF, Block 428, Bahrain",
                                                    "divisionName": "ASG Logistics",
                                                    "companyDivAddressPos": "26.2275,50.5860",
                                                    "companyDivLogo": "",
                                                    "actionType": "noChange"
                                                  }
                                              ]
                                            }
                                            """
                            )
                    }
            )
    )
    @PostMapping("/create")
    public ResponseEntity<?> createCompany(
            @RequestBody @Valid Company company) {
        try {
            if (company.getCompanyPoid() != null) {
                return badRequest("companyPoid must be null when creating");
            }

            String companyPoid = companyService.saveOrUpdateCompany(company);

            Map<String, Object> data = Map.of("companyPoid", companyPoid);
            return success("Company created successfully", data);

        } catch (ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            return internalServerError("Failed to create company: " + ex.getMessage());
        }
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @Operation(
            summary = "Update Company",
            description = "Update an existing one based on the request payload."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = """
                    ### Request Parameters
                        - **userPoid:** User's Primary Key (optional, defaults to 0)
                    
                    ### Request Body
                        Provide company details.
                        - **companyPoid:** Existing company will be updated for provided companypoid.
                        - Other fields as applicable for company setup.
                    
                    """,
            content = @Content(
                    schema = @Schema(implementation = Company.class),
                    examples = {
                            @ExampleObject(
                                    name = "Company Update Example",
                                    value = """
                                            {
                                              "groupPoid": 1,
                                              "companyPoid": 207,
                                              "companyCode": "DEF9",
                                              "companyName": "Crown Shipping Agency & Logistics WLL 10",
                                              "companyName2": "CSA-LOGISTICS2",
                                              "contactPerson": "Manu C Joy Updated2",
                                              "telephone": "17515555",
                                              "fax": "17911999",
                                              "email": "operations@shippingbahrain.com",
                                              "countryId": "18",
                                              "stateId": "337",
                                              "address": "P.O. Box 12000, Manama, Kingdom of Bahrain",
                                              "financialPeriodStart": "2025-01-01T00:00:00",
                                              "financialPeriodEnd": "2025-12-31T00:00:00",
                                              "reportPeriodStart": "2025-01-01T00:00:00",
                                              "reportPeriodEnd": "2025-12-31T00:00:00",
                                              "active": "Y",
                                              "seqNo": 8,
                                              "createdBy": "system",
                                              "createdDate": null,
                                              "lastModifiedBy": "admin_user",
                                              "lastModifiedDate": "2025-08-12T11:45:00",
                                              "deleted": null,
                                              "transPeriodStart": "2025-04-01T00:00:00",
                                              "transPeriodEnd": "2025-12-31T00:00:00",
                                              "provisionalClosedDate": "2018-01-31T00:00:00",
                                              "bankDetail": "National Bank of Bahrain, IBAN BH12NBOB00000001234567",
                                              "bankPoid": 350,
                                              "tinNumber": "9",
                                              "vatRegistrationDate": "2020-01-01T00:00:00",
                                              "vatLastFiledDate": "2025-04-15T00:00:00",
                                              "accountPerson": "Anish Kumar",
                                              "stockPeriodStart": "2025-01-01T00:00:00",
                                              "stockPeriodEnd": "2025-12-31T00:00:00",
                                              "vatFilingPeriod": "3",
                                              "accountEmail": "accounts@shippingbahrain.com",
                                              "vatLastFiledBy": "Arun Joseph",
                                              "vatLastFiledCreatedDate": "2025-04-28T16:48:04",
                                              "financialDateUpdatedBy": "MANOJ",
                                              "financialDateUpdatedDate": "2025-02-04T12:06:36",
                                              "transDateUpdatedBy": "MANOJ",
                                              "transDateUpdatedDate": "2025-06-21T13:21:00",
                                              "reportDateUpdatedBy": "MANOJ",
                                              "reportDateUpdatedDate": "2025-02-04T12:06:42",
                                              "inventoryDateUpdatedBy": null,
                                              "inventoryDateUpdatedDate": null,
                                              "dateFormat": "DD-MM-YYYY",
                                              "timezoneId": 101,
                                              "currencyPoid": 120,
                                              "submissionPeriod": 202501,
                                              "divisions": [
                                                  {
                                                    "divPoid": 3,
                                                    "remarks": "ASG-SHIPPING Updated3",
                                                    "lastModifiedBy": "admin_user",
                                                    "lastModifiedDate": "2025-08-12T10:30:00",
                                                    "companyDivAddress": "Flat No. 45, Building 2020B, Road 220, SEEF, Block 428, Bahrain",
                                                    "divisionName": "ASG Logistics",
                                                    "companyDivAddressPos": "26.2275,50.5860",
                                                    "companyDivLogo": "",
                                                    "actionType": "noChange"
                                                  }
                                              ]
                                            }
                                            """
                            )
                    }
            )
    )
    @PostMapping("/update")
    public ResponseEntity<?> updateCompany(
            @RequestBody @Valid Company company,
            @RequestParam(required = false, defaultValue = "0") Long userPoid) {
        try {
            if (company.getCompanyPoid() == null) {
                return badRequest("companyPoid is required when updating");
            }

            String companyPoid = companyService.saveOrUpdateCompany(company);

            Map<String, Object> data = Map.of("companyPoid", companyPoid);
            return success("Company updated successfully", data);

        } catch (ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            return internalServerError("Failed to update company: " + ex.getMessage());
        }
    }

    @Operation(
            summary = "Delete Company",
            description = """
                    Deletes a company based on the provided parameters.
                    
                    ### Request Parameters
                        - **companyPoid:** Company's Primary Key (optional)
                    
                    """
    )
    @DeleteMapping("/{companyPoid}")
    public ResponseEntity<?> softDeleteCompany(
            @PathVariable @NotNull @Min(1) Long companyPoid,
            @RequestBody(required = false) DeleteReasonDto deleteReasonDto
    ) {
        try {
            companyService.softDeleteCompany(companyPoid, deleteReasonDto);
            return success("Company deleted successfully", null);
        } catch (ResourceNotFoundException e) {
            log.error("Error deactivating company with userPoid {}: {}", companyPoid, e.getMessage());
            return notFound(e.getMessage());
        } catch (Exception e) {
            log.error("Error deactivating Company with userPoid {}: {}", companyPoid, e.getMessage());
            return internalServerError("Failed to deactivate Company: " + e.getMessage());
        }
    }
}