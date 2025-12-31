package com.asg.settings.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.entity.CurrencyEntity;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.settings.dto.CurrencyRateDto;
import com.asg.settings.dto.request.CurrencyCreateRequest;
import com.asg.settings.dto.request.CurrencyUpdateRequest;

import com.asg.settings.service.CurrencyService;
import com.asg.settings.service.CurrencyUploadService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

import static com.asg.common.lib.dto.response.ApiResponse.*;

@RestController
@RequestMapping("/v1/currency")

@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;
    private final CurrencyUploadService uploadService;

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @Operation(
            summary = "List Currencies with Search and Sort",
            description = "Provide search filters. Valid `searchField` values: GLOBALSEARCH or (CURRENCY_CODE, CURRENCY_NAME, CURRENCY_SHORT_NAME, COIN_SHORT_NAME). Sorting default on currencyPoid, desc." +
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
                      2. Any combination of specific fields (CURRENCY_CODE, CURRENCY_NAME, CURRENCY_SHORT_NAME, COIN_SHORT_NAME).
                      3. operator field will either have "AND" or "OR", if not given will be considered as "OR"
                      4. isDeleted when 'N' or null, will search and return non deleted records, 'Y' will check and return deleted records
                    """,
            content = @Content(
                    schema = @Schema(implementation = FilterRequestDto.class),
                    examples = {
                            @ExampleObject(
                                    name = "Currency Filters",
                                    value = """
                                             {
                                             "operator": "OR",
                                             "isDeleted": "N",
                                             "filters": [
                                                { "searchField": "GLOBALSEARCH", "searchValue": "USD" },
                                                { "searchField": "CURRENCY_CODE", "searchValue": "USD" },
                                                { "searchField": "CURRENCY_NAME", "searchValue": "U.S.DOLLARS" },
                                                { "searchField": "CURRENCY_SHORT_NAME", "searchValue": "DOLLARS" },
                                                { "searchField": "COIN_SHORT_NAME", "searchValue": "DOLLARS" }                                          \s
                                             ]
                                             }
                                            """
                            )
                    }
            )
    )
    @PostMapping("/list")
    public ResponseEntity<?> getCurrencies(
            @ParameterObject Pageable pageable,
            @RequestBody(required = false) FilterRequestDto filters) {
        try {
            Map<String, Object> users = currencyService.listCurrencies(UserContext.getDocumentId(), filters, pageable);
            return success("Currency list fetched successfully", users);
        } catch (Exception e) {
            return internalServerError("Unable to fetch currency list: " + e.getMessage());
        }
    }

    @PostMapping("/details")
    public ResponseEntity<?> getCurrencyDetails(
            @RequestParam Long currencyPoid) {
        try {
            CurrencyRateDto data = currencyService.getAllCurrencyRates(currencyPoid);
            return success("Currency  fetched successfully", data);
        } catch (Exception ex) {
            return internalServerError("Failed to retrieve currency deails: " + ex.getMessage());
        }
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @Operation(
            summary = "Upload Excel File",
            description = """
                    Uploads an Excel file and processes its data.
                    
                    ### Request Parameters
                        - **file:** Excel file to be uploaded
                    """
    )
    @PostMapping("/upload-excel")
    public ResponseEntity<?> uploadExcel(@RequestParam("file") MultipartFile file) {
        try {
            String result = uploadService.uploadCurrencyRates(file, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
            if (result == null || result.toUpperCase().contains("ERROR")) {
                return internalServerError(result);
            }
            return success("Currency rates uploaded successfully", result);
        } catch (Exception e) {
            return internalServerError("Failed to process request: " + e.getMessage());
        }
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @Operation(
            summary = "Update Currency Rate",
            description = """
                    Updates the buy and sell rates for a given currency.
                    
                    ### Request Body
                        - **groupPOID:** Group's Primary Key
                        - **currencyCode:** Code of the currency (e.g., USD, EUR)
                        - **rateChangeDate:** Date of the rate change
                        - **buyRate:** New buy rate
                        - **sellRate:** New sell rate
                    """
    )
    @PostMapping("/update-rate")
    public ResponseEntity<?> updateRate(@RequestBody CurrencyUpdateRequest request) {
        try {
            String result = uploadService.updateCurrencyRates(request);
            if (result == null || result.toUpperCase().contains("ERROR")) {
                return internalServerError(result);
            }
            return success(result, "");
        } catch (Exception e) {
            return internalServerError("Failed to process request: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Create Currency",
            description = "Create a new currency based on the request payload."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = """      
                    ### Request Body
                        Provide currency details.
                        - **currencyPoid:** Must be `null` when creating.
                        - **currencyName:** (Required) Name of the currency (e.g., US Dollar).
                        - **currencyCode:** (Required) Unique currency code (e.g., USD).
                        - Other fields as applicable for currency setup
                    """,
            content = @Content(
                    schema = @Schema(implementation = CurrencyCreateRequest.class),
                    examples = {
                            @ExampleObject(
                                    name = "Currency Create Example",
                                    value = """
                                            {
                                              "currencyName": "US Dollar",
                                              "currencyName2": "Dollar",
                                              "currencyCode": "USD",
                                              "currencyShortName": "DOLLAR",
                                              "currencyDecimals": 2,
                                              "seqno": 1,
                                              "active": "Y"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Currency Create Example 2",
                                    value = """
                                            {
                                              "currencyName": "Indian Rupee",
                                              "currencyName2": "Rupee",
                                              "currencyCode": "INR",
                                              "currencyShortName": "RUPEE",
                                              "currencyDecimals": 2,
                                              "seqno": 2,
                                              "active": "Y"
                                            }
                                            """
                            )
                    }
            )
    )
    @PostMapping("/create")
    public ResponseEntity<?> createCurrency(
            @RequestBody @Valid CurrencyCreateRequest currencyDto
    ) {
        try {
            if (currencyDto == null || currencyDto.getCurrencyName() == null) {
                return badRequest("Missing required currency fields (currencyName)");
            }

            if (StringUtils.isBlank(currencyDto.getCurrencyCode())) {
                return badRequest("currencyCode is required");
            }

            if (currencyDto.getCurrencyPoid() != null) {
                return badRequest("currencyPoid must be null when creating");
            }

            CurrencyEntity savedEntity = currencyService.createOrUpdateCurrency(currencyDto, UserContext.getGroupPoid(), UserContext.getUserPoid());

            Map<String, Object> data = new HashMap<>();
            data.put("currencyPoid", savedEntity.getCurrencyPoid());
            data.put("currencyCode", savedEntity.getCurrencyCode());
            data.put("currencyName", savedEntity.getCurrencyName());
            data.put("currencyName2", savedEntity.getCurrencyName2());

            return success("Currency created successfully", data);
        } catch (Exception ex) {
            return internalServerError(ex.getMessage());
        }
    }

    @Operation(
            summary = "Update Currency",
            description = "Update an existing currency based on the request payload."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = """
                    ### Request Body
                        Provide currency details.
                        - **currencyPoid:** Existing currency will be updated for provided currencyPoid.
                        - **currencyName:** (Required) Name of the currency (e.g., US Dollar).
                        - **currencyCode:** (Required) Unique currency code (e.g., USD).
                        - Other fields as applicable for currency setup
                    """,
            content = @Content(
                    schema = @Schema(implementation = CurrencyCreateRequest.class),
                    examples = {
                            @ExampleObject(
                                    name = "Currency Update Example",
                                    value = """
                                            {
                                              "currencyPoid": 120,
                                              "currencyName": "US Dollar Updated",
                                              "currencyName2": "Dollar",
                                              "currencyCode": "USD",
                                              "currencyShortName": "DOLLAR",
                                              "currencyDecimals": 2,
                                              "seqno": 1,
                                              "active": "Y"
                                            }
                                            """
                            )
                    }
            )
    )
    @PostMapping("/update")
    public ResponseEntity<?> updateCurrency(
            @RequestBody @Valid CurrencyCreateRequest currencyDto
    ) {
        try {
            if (currencyDto == null || currencyDto.getCurrencyName() == null) {
                return badRequest("Missing required currency fields (currencyName)");
            }

            if (StringUtils.isBlank(currencyDto.getCurrencyCode())) {
                return badRequest("currencyCode is required");
            }

            if (currencyDto.getCurrencyPoid() == null) {
                return badRequest("currencyPoid is required when updating");
            }

            CurrencyEntity savedEntity = currencyService.createOrUpdateCurrency(currencyDto, UserContext.getGroupPoid(), UserContext.getUserPoid());

            Map<String, Object> data = new HashMap<>();
            data.put("currencyPoid", savedEntity.getCurrencyPoid());
            data.put("currencyCode", savedEntity.getCurrencyCode());
            data.put("currencyName", savedEntity.getCurrencyName());
            data.put("currencyName2", savedEntity.getCurrencyName2());

            return success("Currency updated successfully", data);
        } catch (Exception ex) {
            return internalServerError(ex.getMessage());
        }
    }

    @Operation(
            summary = "Soft Delete Currency",
            description = """
                    Soft deletes a currency based on the provided parameters.
                    
                    ### Request Parameters
                        - **currencyPoid:** Currency's Primary Key
                    """
    )
    @DeleteMapping("/soft-delete")
    public ResponseEntity<?> softDeleteCurrency(
            @RequestParam Long currencyPoid) {
        try {
            currencyService.softDeleteCurrency(currencyPoid);
            return success("Currency soft deleted successfully", Map.of("currencyPoid", currencyPoid));
        } catch (Exception e) {
            return internalServerError("Failed to soft delete currency: " + e.getMessage());
        }
    }

}