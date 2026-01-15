package com.asg.settings.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.exception.ValidationException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.dto.response.AddressMasterResponse;
import com.asg.settings.service.AddressMasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@RequestMapping("/v1/address-master")
@RequiredArgsConstructor

@Validated
@Slf4j
@Tag(
        name = "address-master-controller",
        description = "Manage Address Master records with details for MAIN, FINANCE, SALES, OPERATIONS, and other tabs (DocId: 000-016)"
)
public class AddressMasterController {

    private final AddressMasterService service;

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @Operation(
            summary = "List Address Masters with Search (DocId: 000-016)",
            description = "Fetch Address Masters using filters and pagination. Valid `searchField` values: GLOBALSEARCH, ADDRESS_NAME, ADDRESS_NAME2, REMARKS, OLD_ACCTNO, ACTIVE."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = false,
            description = "Provide filters for search. Authorization Parameters (handled by interceptor): documentId (000-016), actionRequested (VIEW)",
            content = @Content(
                    schema = @Schema(implementation = FilterRequestDto.class),
                    examples = @ExampleObject(
                            name = "Address Master Filters",
                            value = "{\"operator\":\"AND\",\"isDeleted\":\"N\",\"filters\":[{\"searchField\":\"GLOBALSEARCH\",\"searchValue\":\"LogiTest\"}]}"
                    )
            )
    )
    @PostMapping("/list")
    public ResponseEntity<?> listAddressMasters(
            @ParameterObject Pageable pageable,
            @RequestBody(required = false) FilterRequestDto request
    ) {
        try {
            Map<String, Object> addressMasters = service.listAddressMasters(UserContext.getDocumentId(), request, pageable);
            return success("Address Masters list fetched successfully", addressMasters);
        } catch (Exception e) {
            return internalServerError("Unable to fetch Address Masters: " + e.getMessage());
        }
    }


    @Operation(
            summary = "Get Address Master Details (DocId: 000-016)",
            description = "Fetch a single Address Master and all its tab details."
    )
    @GetMapping("/{poid}")
    public ResponseEntity<?> getMaster(
            @PathVariable @Parameter(description = "Address Master POID", required = true) Long poid) {
        try {
            AddressMasterResponse response = service.getMasterWithDetails(poid);
            return success("Address Master fetched successfully", response);
        } catch (Exception e) {
            return internalServerError("Error fetching Address Master: " + e.getMessage());
        }
    }

    @GetMapping("/simple/{poid}")
    public ResponseEntity<?> getSimple(@PathVariable Long poid) {
        try {
            AddressMasterResponse response = service.getMasterWithDetails(poid);
            return success("Address Master fetched successfully", response);
        } catch (Exception e) {
            return internalServerError("Error fetching Address Master: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Create All Address Types for Master (DocId: 000-016)",
            description = "Executes the 'Create All' process which creates all missing address types for this master."
    )
    @PostMapping("/{poid}/create-all")
    public ResponseEntity<?> createAll(
            @PathVariable @Parameter(description = "Address Master POID", required = true) Long poid) {
        try {
            String status = service.createAll(poid);
            return success("Create All executed successfully", Map.of("status", status));
        } catch (Exception e) {
            return internalServerError("Error executing Create All: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Copy All Address Type Details to Other Tabs (DocId: 000-016)",
            description = "Executes the 'Copy All' process which copies the first address type details to all other tabs."
    )
    @PostMapping("/{poid}/copy-all")
    public ResponseEntity<?> copyAll(
            @PathVariable @Parameter(description = "Address Master POID", required = true) Long poid) {
        try {
            String status = service.copyAll(poid);
            return success("Copy All executed successfully", Map.of("status", status));
        } catch (Exception e) {
            return internalServerError("Error executing Copy All: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Create Address Master (DocId: 000-016)",
            description = "Create a new Address Master record. Business Rules: Address Name, Seq No, Country, and MAIN contact (Mobile & Email) are mandatory."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Provide full details for Address Master including MAIN and other contact tabs.",
            content = @Content(
                    schema = @Schema(implementation = AddressMasterResponse.class),
                    examples = @ExampleObject(
                            name = "Address Master Create Example",
                            value = """
                                    {
                                      "addressMasterPoid": null,
                                      "addressName": "ABC Shipping LLC",
                                      "addressName2": "ABC Logistics",
                                      "countryId": 1,
                                      "preferredCommunication": ["EMAIL","MOBILE"],
                                      "partyType": ["CUSTOMER"],
                                      "whatsappNo": "+97312345678",
                                      "linkedIn": "https://linkedin.com/abc",
                                      "instagram": "@abc_shipping",
                                      "facebook": "abc.fb",
                                      "remarks": "Preferred customer",
                                      "crNumber": "CR-98765",
                                      "isForwarder": true,
                                      "active": "Y",
                                      "seqno": 10,
                                      "addressTypeMap": {
                                        "MAIN": [
                                          {
                                            "contactPerson": "John Doe",
                                            "designation": "Manager",
                                            "mobile": "+97398765432",
                                            "email": ["john.doe@abc.com","alt@abc.com"],
                                            "offTel1": "123456",
                                            "offTel2": "789456",
                                            "fax": "98765",
                                            "offNo": "Office-101",
                                            "bldg": "Building-A",
                                            "road": "Road-12",
                                            "area": "Block 1",
                                            "website": "https://abc.com",
                                            "poBox": "PO 123",
                                            "city": "Manama",
                                            "state": ["Capital Governorate"],
                                            "landMark": "Near City Mall",
                                            "whatsappNo": "+97311122233",
                                            "linkedIn": "https://linkedin.com/john.doe",
                                            "instagram": "@johndoe_ops",
                                            "facebook": "john.doe.fb"
                                          }
                                        ],
                                        "FINANCE": [
                                          {
                                            "contactPerson": "Sarah Johnson",
                                            "designation": "Finance Manager",
                                            "mobile": "+97312345679",
                                            "email": ["sarah.finance@abc.com"],
                                            "offTel1": "654321",
                                            "department": "Finance",
                                            "remarks": "Handles all financial matters"
                                          }
                                        ],
                                        "SALES": [
                                          {
                                            "contactPerson": "Mike Wilson",
                                            "designation": "Sales Director",
                                            "mobile": "+97312345680",
                                            "email": ["mike.sales@abc.com", "sales@abc.com"],
                                            "offTel1": "987654",
                                            "territory": "Middle East",
                                            "commission": "5%",
                                            "remarks": "Regional sales head"
                                          }
                                        ],
                                        "CLAIM_UAC": [
                                          {
                                            "contactName": "Jane Smith",
                                            "contactType": "Claims Manager",
                                            "companyName": "Insurance Corp",
                                            "email": ["jane@insurance.com"],
                                            "contactNo": "+97387654321",
                                            "countryName": "Bahrain",
                                            "details": "Handles all claim processing",
                                            "active": "Y",
                                            "seqno": 1
                                          }
                                        ]
                                      }
                                    }
                                    """
                    )
            )
    )
    @PostMapping("/create")
    public ResponseEntity<?> createAddress(
            @Valid @RequestBody AddressMasterResponse request) {
        try {
            if (request.getAddressMasterPoid() != null) {
                return badRequest("addressMasterPoid must be null when creating");
            }

            Long poid = service.saveAddressMaster(request);
            Map<String, Object> data = Map.of("addressMasterPoid", poid);
            return success("Address Master created successfully", data);

        } catch (ValidationException ex) {
            return badRequest(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return unprocessableEntity(ex.getMessage());
        } catch (Exception ex) {
            return internalServerError("Error creating Address Master: " + ex.getMessage());
        }
    }

    @Operation(
            summary = "Update Address Master (DocId: 000-016)",
            description = "Update an existing Address Master record. Business Rules: Address Master POID, Address Name, Seq No, Country, and MAIN contact (Mobile & Email) are mandatory."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Provide full details for Address Master including MAIN and other contact tabs.",
            content = @Content(
                    schema = @Schema(implementation = AddressMasterResponse.class),
                    examples = @ExampleObject(
                            name = "Address Master Update Example",
                            value = """
                                    {
                                      "addressMasterPoid": 123,
                                      "addressName": "ABC Shipping LLC Updated",
                                      "addressName2": "ABC Logistics Updated",
                                      "countryId": 1,
                                      "preferredCommunication": ["EMAIL","MOBILE"],
                                      "partyType": ["CUSTOMER"],
                                      "whatsappNo": "+97312345678",
                                      "linkedIn": "https://linkedin.com/abc",
                                      "instagram": "@abc_shipping",
                                      "facebook": "abc.fb",
                                      "remarks": "Preferred customer updated",
                                      "crNumber": "CR-98765",
                                      "isForwarder": true,
                                      "active": "Y",
                                      "seqno": 15,
                                      "addressTypeMap": {
                                        "MAIN": [
                                          {
                                            "contactPerson": "John Doe Updated",
                                            "designation": "Senior Manager",
                                            "mobile": "+97398765432",
                                            "email": ["john.updated@abc.com","alt.updated@abc.com"],
                                            "offTel1": "123456",
                                            "offTel2": "789456",
                                            "fax": "98765",
                                            "offNo": "Office-101",
                                            "bldg": "Building-A",
                                            "road": "Road-12",
                                            "area": "Block 1",
                                            "website": "https://abc.com",
                                            "poBox": "PO 123",
                                            "city": "Manama",
                                            "state": ["Capital Governorate"],
                                            "landMark": "Near City Mall",
                                            "whatsappNo": "+97311122233",
                                            "linkedIn": "https://linkedin.com/john.doe",
                                            "instagram": "@johndoe_ops",
                                            "facebook": "john.doe.fb"
                                          }
                                        ],
                                        "FINANCE": [
                                          {
                                            "contactPerson": "Sarah Johnson Updated",
                                            "designation": "Senior Finance Manager",
                                            "mobile": "+97312345679",
                                            "email": ["sarah.finance.updated@abc.com"],
                                            "offTel1": "654321",
                                            "department": "Finance",
                                            "remarks": "Updated finance contact"
                                          }
                                        ],
                                        "SALES": [
                                          {
                                            "contactPerson": "Mike Wilson Updated",
                                            "designation": "Regional Sales Director",
                                            "mobile": "+97312345680",
                                            "email": ["mike.sales.updated@abc.com", "sales.updated@abc.com"],
                                            "offTel1": "987654",
                                            "territory": "GCC Region",
                                            "commission": "7%",
                                            "remarks": "Updated regional sales head"
                                          }
                                        ],
                                        "CLAIM_UAC": [
                                          {
                                            "contactName": "Jane Smith Updated",
                                            "contactType": "Senior Claims Manager",
                                            "companyName": "Insurance Corp Updated",
                                            "email": ["jane.updated@insurance.com"],
                                            "contactNo": "+97387654321",
                                            "countryName": "Bahrain",
                                            "details": "Updated claims processing contact",
                                            "active": "Y",
                                            "seqno": 1
                                          }
                                        ]
                                      }
                                    }
                                    """
                    )
            )
    )
    @PostMapping("/update")
    public ResponseEntity<?> updateAddress(
            @Valid @RequestBody AddressMasterResponse request) {
        try {
            if (request.getAddressMasterPoid() == null) {
                return badRequest("addressMasterPoid is required when updating");
            }

            Long poid = service.saveAddressMaster(request);
            Map<String, Object> data = Map.of("addressMasterPoid", poid);
            return success("Address Master updated successfully", data);

        } catch (ValidationException ex) {
            return badRequest(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return unprocessableEntity(ex.getMessage());
        } catch (Exception ex) {
            return internalServerError("Error updating Address Master: " + ex.getMessage());
        }
    }



    @PostMapping("/upsert")
    public ResponseEntity<?> upsertAddress(@Valid @RequestBody AddressMasterResponse request) {
        try {
            Long poid = service.saveAddressMaster(request);
            AddressMasterResponse response = service.getMasterWithDetails(poid);
            return success("Address Master saved successfully", response);
        } catch (ValidationException ex) {
            return badRequest(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return unprocessableEntity(ex.getMessage());
        } catch (Exception ex) {
            return internalServerError("Error saving Address Master: " + ex.getMessage());
        }
    }

    @Operation(
            summary = "Soft Delete Address Master",
            description = "Deactivate an Address Master by setting ACTIVE='N' and DELETED='Y' in GLOBAL_ADDRESS_MASTER table."
    )
    @DeleteMapping("/{addressMasterPoid}")
    public ResponseEntity<?> softDeleteAddressMaster(
            @PathVariable @NotNull @Min(1) Long addressMasterPoid,
            @RequestBody(required = false) DeleteReasonDto deleteReasonDto) {
        try {
            service.softDeleteAddressMaster(addressMasterPoid, deleteReasonDto);
            return success("Address Master marked as deleted and deactivated successfully", null);
        } catch (ResourceNotFoundException e) {
            log.error("Error deactivating address master with userPoid {}: {}", addressMasterPoid, e.getMessage());
            return notFound(e.getMessage());
        } catch (Exception e) {
            log.error("Error deactivating address master with userPoid {}: {}", addressMasterPoid, e.getMessage());
            return internalServerError("Failed to deactivate Address Master: " + e.getMessage());
        }
    }
}
