package com.asg.settings.controller;

import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.dto.CountryDto;
import com.asg.settings.service.CountryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.asg.common.lib.dto.response.ApiResponse.internalServerError;
import static com.asg.common.lib.dto.response.ApiResponse.success;

@RestController
@RequestMapping("/v1/country-master")
public class CountryController {

    private final CountryService countryService;

    @Autowired
    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }


    @Operation(
            summary = "Get country by ID",
            description = "Retrieves country details based on the provided country POID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved the country details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CountryDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Country not found",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @GetMapping("/{countryPoid}")
    public ResponseEntity<?> getCountryByCountryPoid(
            @Parameter(description = "CountryPoid reference identifier", required = true)
            @PathVariable Long countryPoid) {
        CountryDto countryDto = countryService.getCountryById(countryPoid);
        return success("Task fetched successfully", countryDto);

    }


    @Operation(
            summary = "Create a new country",
            description = "Creates a new country with the provided details",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Successfully created the country",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CountryDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input, object invalid",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Country with the same code already exists",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @PostMapping
    public ResponseEntity<?> createCountry(

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Country object that needs to be created",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CountryDto.class)
                    )
            )
            @Parameter(description = "Country details to be created", required = true)
            @Valid @RequestBody CountryDto countryDto) {

        CountryDto createdCountry = countryService.createCountry(countryDto);
        return success("Country created successfully", createdCountry);
    }

    @Operation(
            summary = "Update country details",
            description = "Updates the details of an existing country identified by its ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Country updated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CountryDto.class),
                                    examples = @ExampleObject(
                                            value = "{\"status\": 200, \"message\": \"Country updated successfully\", \"data\": {...}}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input or validation error",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = "{\"status\": 400, \"message\": \"Validation error: [field] is required\"}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Country not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = "{\"status\": 404, \"message\": \"Country not found with countryPoid: 123\"}"
                                    )
                            )
                    )
            }
    )
    @PutMapping("/{countryPoid}")
    public ResponseEntity<?> updateCountry(
            @Parameter(description = "ID of the country to update", required = true)
            @PathVariable Long countryPoid,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Country object with updated details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CountryDto.class),
                            examples = @ExampleObject(
                                    name = "CountryUpdateExample",
                                    value = """
                        {
                            "groupPoid": 1,
                            "countryName": "Updated Country Name",
                            "countryCode": "UCN",
                            "countryName2": "Updated Country Name 2",
                            "active": "Y",
                            "countryTicketRate": 10.5
                        }
                        """
                            )
                    )
            )
            @Valid @RequestBody CountryDto countryDto) {
        countryDto.setCountryPoid(countryPoid);

        CountryDto updatedCountry = countryService.updateCountry(countryPoid, countryDto);
        return success("Country updated successfully", updatedCountry);
    }


    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = """
                    - ### Filters:
                      Use either:
                      1. A single `GLOBALSEARCH` filter, OR
                      2. Any combination of specific fields (COUNTRY_NAME, COUNTRY_CODE, CREATED_BY, etc.).
                      3. operator field will either have "AND" or "OR", if not given will be considered as "OR",
                         not required for GLOBALSEARCH, for non GLOBALSEARCH need to give only 1 time
                      4. isDeleted when 'Y' or null, will search and return non deleted records, 'Y' will check and return deleted records
                      5. sort will be default on primary key ascending if specified in db field otherwise you can override it giving the field name and the direction.
                    
                    - #### Global Search:
                      Apply one search term across multiple fields.
                      No operator need to send in this case.
                      • { "searchField": "GLOBALSEARCH", "searchValue": "United" }
                    
                    - #### Single Field, Single Value:
                      Search one field with one value.
                      • { "searchField": "COUNTRY_NAME", "searchValue": "United Arab Emirates" },
                    
                    - #### Single Field, Multiple Values:
                      Provide multiple values for the same field, separated by `|`.
                      • { "searchField": "COUNTRY_CODE", "searchValue": "AE|US|IN" }
                    
                    - #### Multiple Different Fields, Single or Multiple Value:
                      Provide one or multiple search values across multiple fields.
                      • { "searchField": "COUNTRY_NAME", "searchValue": "United Arab Emirates" },
                      • { "searchField": "CREATED_BY", "searchValue": "ADMIN|USER" }
                      • "operator" :  "AND"/"OR"
                    
                    - ### Sorting:
                      Defaults to whatever specified in list_of_records_sql field mostly primary key ascending.
                      To override, pass `sort=<field>,ASC|DESC` in query params.
                      Examples:
                      • sort=COUNTRY_NAME,ASC
                      • sort=COUNTRY_POID,DESC
                    """,
            content = @Content(
                    schema = @Schema(implementation = FilterRequestDto.class),
                    examples = {
                            @ExampleObject(
                                    name = "Country Filters",
                                    value = """
                                            {
                                            "operator": "AND",
                                            "isDeleted": "N",
                                            "filters": [
                                               { "searchField": "GLOBALSEARCH", "searchValue": "United" },
                                               { "searchField": "COUNTRY_NAME", "searchValue": "United Arab Emirates|United States" },
                                               { "searchField": "COUNTRY_CODE", "searchValue": "AE"},
                                               { "searchField": "CREATED_BY", "searchValue": "ADMIN"}
                                            ]
                                            }
                                            """
                            )
                    }
            )
    )
    @PostMapping("/list")
    public ResponseEntity<?> getCountryList(@ParameterObject Pageable pageable,
                                            @RequestBody(required = false) FilterRequestDto filters) {
        try {
            Map<String, Object> countries = countryService.listCountries(UserContext.getDocumentId(), filters, pageable);
            return success("Countries fetched successfully", countries);
        } catch (Exception e) {
            return internalServerError("Error fetching Country List: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Soft delete a country",
            description = "Marks a country as deleted without permanently removing its data",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully soft deleted the country",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CountryDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Country not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @DeleteMapping("/{countryPoid}")
    public ResponseEntity<?> softDeleteCountry(
            @Parameter(description = "CountryPoid reference identifier", required = true)
            @PathVariable Long countryPoid) {
        countryService.softDeleteCountry(countryPoid);
        return success("Country has been soft deleted successfully");
    }
}
