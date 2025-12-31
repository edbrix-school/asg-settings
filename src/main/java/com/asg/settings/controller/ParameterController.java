package com.asg.settings.controller;

import com.asg.settings.dto.BulkUpdateResponseDTO;
import com.asg.settings.dto.GlobalParameterResponse;
import com.asg.settings.dto.UpdateParameterRequestDTO;
import com.asg.settings.service.ParameterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.asg.common.lib.dto.response.ApiResponse.error;
import static com.asg.common.lib.dto.response.ApiResponse.success;


@RestController
@RequestMapping("/v1/global-parameters")

public class ParameterController {

    @Autowired
    private ParameterService parameterService;

    @Operation(
            summary = "Get List Of User Parameters",
            description = """
                        Fetch List of **USER** type parameters..
                        And Filter applied on parameterName,parameterKeyIdType,parameterValue and parameterDetails.
                         ### API Behavior:
                           - `userParameters` → All USER type parameters
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully Fetched SYSTEM Type parameters"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/user")
    public ResponseEntity<?> getUserParameters(
            @RequestParam(required = false) String filter,
            @ParameterObject Pageable pageable
    ) {
        GlobalParameterResponse response = parameterService.getUserParameters(filter, pageable);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("globalParameters", response.getGlobalParameters());
        responseData.put("privileged", response.getPrivileged());
        responseData.put("page", pageable.getPageNumber());
        responseData.put("size", pageable.getPageSize());
        responseData.put("totalElements", response.getTotalElements());

        return success("User parameters fetched successfully", responseData);
    }

    @Operation(
            summary = "Get List Of System Parameters",
            description = """
                        Fetch List of **SYSTEM** type parameters..
                        And Filter applied on parameterName,parameterKeyIdType,parameterValue and parameterDetails.
                         ### API Behavior:
                             - `systemParameters` → All SYSTEM type parameters
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully Fetched SYSTEM Type parameters"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/system")
    public ResponseEntity<?> getSystemParameters(
            @RequestParam Long userPoid,
            @RequestParam(required = false) String filter,
            @ParameterObject Pageable pageable
    ) {
        GlobalParameterResponse response = parameterService.getSystemParameters(userPoid, filter, pageable);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("userPoid", userPoid);
        responseData.put("globalParameters", response.getGlobalParameters());
        responseData.put("privileged", response.getPrivileged());
        responseData.put("page", pageable.getPageNumber());
        responseData.put("size", pageable.getPageSize());
        responseData.put("totalElements", response.getTotalElements());

        return success("System parameters fetched successfully", responseData);
    }

    @Operation(
            summary = "Update Parameters",
            description = "Updating an existing parameter. This endpoint allows you to update an existing parameter's details. The `parameterPoid` is required to identify the parameter to be updated. The updated details are provided in the request body. The response indicates the success or failure of the update operation."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = """
                    ### Request Body
                        Update the parameters by providing the required details.
                        - **loginUserPoid:** ID of the user updating the parameters. Required.
                        - **parameters:** Parameters to update. This field is mandatory.
                    
                    ### Notes
                        - All fields are required for successful parameters update.
                    
                    """,
            content = @Content(
                    examples = {
                            @ExampleObject(
                                    name = "Update Parameters Example",
                                    value = """
                                            {
                                                "loginUserPoid": 3371,
                                                "parameters": [
                                                    {
                                                        "parameterPoid": 55717,
                                                        "parameterKeyId": "70",
                                                        "parameterValue": "\\\\\\\\10.100.100.159\\\\erp_reports\\\\Reports\\\\9_updated\\\\"
                                                    },
                                                    {
                                                        "parameterPoid": 55718,
                                                        "parameterKeyId": "70",
                                                        "parameterValue": "\\\\\\\\10.100.100.159\\\\erp_reports\\\\Reports\\\\10_updated\\\\"
                                                    }
                                                ]
                                            }
                                            """
                            )
                    }
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the parameters"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/bulk-update")
    public ResponseEntity<?> updateParameters(
            @Valid @RequestBody UpdateParameterRequestDTO updateParameterDTO) {

        BulkUpdateResponseDTO response = parameterService.updateParameters(updateParameterDTO);

        return switch (response.getOverallStatus()) {
            case "SUCCESS" -> success("All parameters updated successfully", response);
            case "PARTIAL_SUCCESS" -> success("Some parameters updated successfully", response);
            default -> error("Failed to update parameters", 500, response);
        };
    }

    @Operation(summary = "Get parameter value by name", description = "Simple endpoint for internal service calls")
    @GetMapping("/value/{parameterName}")
    public ResponseEntity<?> getParameterValue(@PathVariable String parameterName) {
        String value = parameterService.getParameterValue(parameterName);
        return success("Parameter value fetched", value);
    }

    @GetMapping("/value-decimal/{parameterType}/{parameterName}")
    public ResponseEntity<?> getParameterValueAsDecimal(@PathVariable String parameterType, @PathVariable String parameterName) {
        java.math.BigDecimal value = parameterService.getParameterValueByNameAsDecimal(parameterType, parameterName);
        return success("Parameter value fetched", value);
    }

    @GetMapping("/value/{parameterType}/{parameterName}")
    public ResponseEntity<?> getParameterValueByType(@PathVariable String parameterType, @PathVariable String parameterName) {
        Integer value = parameterService.getParameterValueByName(parameterType, parameterName);
        return success("Parameter value fetched", value);
    }
}
