package com.asg.settings.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.dto.request.LogRequestDto;
import com.asg.common.lib.entity.CompanyDivisionEntity;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.utility.ASGHelperUtils;
import com.asg.common.lib.utility.DateUtil;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.common.lib.dto.AddressDetailsDTO;
import com.asg.common.lib.dto.response.AddressMasterResponse;
import com.asg.common.lib.dto.AddressTypeMapDTO;
import com.asg.settings.entity.AddressDetails;
import com.asg.settings.entity.AddressMaster;
import com.asg.settings.repository.AddressDetailsRepository;
import com.asg.settings.repository.AddressMasterRepository;
import com.asg.settings.repository.AddressProcedureRepository;
import com.asg.settings.repository.CountryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressMasterService {

    private final CountryRepository countryRepo;
    private final AddressMasterRepository masterRepo;
    private final AddressDetailsRepository detailsRepo;
    private final AddressProcedureRepository procRepo;
    private final DocumentSearchService documentService;
    private final LoggingService loggingService;
    private final DocumentDeleteService documentDeleteService;

    /**
     * Get single Address Master with all department details (tabs).
     */
    public AddressMasterResponse getMasterWithDetails(Long poid) {
        AddressMaster master = masterRepo.findByAddressMasterPoid(poid);
        if (master == null) {
            throw new NoSuchElementException("Address Master not found");
        }

        List<AddressDetails> details = detailsRepo.findByAddressMasterPoidOrderByAddressType(poid);

        // Group by type into DTO map
        Map<String, List<AddressDetailsDTO>> grouped = details.stream()
                .map(this::mapToDTO)
                .collect(Collectors.groupingBy(AddressDetailsDTO::getAddressType));

        AddressTypeMapDTO typeMap = new AddressTypeMapDTO();
        typeMap.setMain(grouped.getOrDefault("MAIN", List.of()));
        typeMap.setFinance(grouped.getOrDefault("FINANCE", List.of()));
        typeMap.setSales(grouped.getOrDefault("SALES", List.of()));
        typeMap.setOperation(grouped.getOrDefault("OPERATION", List.of()));
        typeMap.setInvoiceAddress(grouped.getOrDefault("INVOICE_ADDRESS", List.of()));
        typeMap.setDeliveryOrder(grouped.getOrDefault("DELIVERY_ORDER", List.of()));
        typeMap.setShipChandling(grouped.getOrDefault("SHIP_CHANDLING", List.of()));
        typeMap.setClaimUac(grouped.getOrDefault("CLAIM_UAC", List.of()));
        typeMap.setCan(grouped.getOrDefault("CAN", List.of()));

        AddressMasterResponse resp = buildMasterResponse(master, details);
        resp.setAddressTypeMap(typeMap);
        return resp;
    }

    public Map<String, Object> listAddressMasters(String docId, FilterRequestDto request, Pageable pageable) {
        String operator = documentService.resolveOperator(request);
        String isDeleted = documentService.resolveIsDeleted(request);
        List<FilterDto> filters = documentService.resolveFilters(request);

        RawSearchResult raw = documentService.search(docId, filters, operator, pageable, isDeleted,
                "ADDRESS_NAME",   // label
                "ADDRESS_MASTER_POID");    // value


        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    /**
     * Save or update Address Master along with all department details.
     */
    @Transactional
    public Long saveAddressMaster(AddressMasterResponse req) {
        validateMandatoryFields(req);
        validateDuplicateName(req);

        String currentUser = ASGHelperUtils.getCurrentUser();
        String docId = UserContext.getDocumentId();

        AddressMaster oldMaster = null;
        // If UPDATE → fetch old copy for logging
        if (req.getAddressMasterPoid() != null) {
            AddressMaster existing = masterRepo.findById(req.getAddressMasterPoid()).orElse(null);
            if (existing != null) {
                oldMaster = new AddressMaster();
                BeanUtils.copyProperties(existing, oldMaster);
            }
        }

        AddressMaster master = buildOrUpdateMaster(req, currentUser);
        
        // For new records, check if sequence-generated ID already exists
        if (req.getAddressMasterPoid() == null && master.getAddressMasterPoid() != null) {
            while (masterRepo.existsByAddressMasterPoid(master.getAddressMasterPoid())) {
                master.setAddressMasterPoid(null); // Force new sequence value
            }
        }
        AddressMaster saved = masterRepo.save(master);

        saveAllDetails(req.getAddressTypeMap(), saved, currentUser);

        String key = saved.getAddressMasterPoid().toString();
        // CASE 1: CREATE
        if (oldMaster == null) {
            loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, docId, key);
        } else {
            // CASE 2: UPDATE
            loggingService.logChanges(oldMaster, saved, AddressMaster.class, docId, key, LogDetailsEnum.MODIFIED, "ADDRESS_MASTER_POID");
        }
        return saved.getAddressMasterPoid();
    }

    @Transactional
    public void softDeleteAddressMaster(Long addressMasterPoid, DeleteReasonDto deleteReasonDto) {
        AddressMaster master = masterRepo.findById(addressMasterPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Address Master", "addressMasterPoid", addressMasterPoid));
        
        documentDeleteService.deleteDocument(
                addressMasterPoid,
                "GLOBAL_ADDRESS_MASTER",
                "ADDRESS_MASTER_POID",
                deleteReasonDto,
                null
        );
    }

    public String createAll(Long addressMasterPoid) {
        Long groupPoid = 1L;
        Long userPoid = UserContext.getUserPoid() != null ? UserContext.getUserPoid() : null;
        Long companyPoid = 200L;
        return procRepo.createAllTypes(groupPoid, userPoid, companyPoid, addressMasterPoid);
    }

    public String copyAll(Long targetPoid) {
        Long groupPoid = 1L;
        Long userPoid = UserContext.getUserPoid() != null ? UserContext.getUserPoid() : null;
        Long companyPoid = 200L;
        return procRepo.copyAllTypes(groupPoid, userPoid, companyPoid, targetPoid);
    }

    private void validateMandatoryFields(AddressMasterResponse req) {
        if (req.getAddressName() == null || req.getAddressName().isBlank())
            throw new IllegalArgumentException("Address Name is mandatory");

        if (req.getCountryId() == null)
            throw new IllegalArgumentException("Country is mandatory");

        if (req.getAddressTypeMap() == null || req.getAddressTypeMap().getMain() == null || req.getAddressTypeMap().getMain().isEmpty())
            throw new IllegalArgumentException("MAIN contact details are mandatory (Mobile & Email required)");

        AddressDetailsDTO main = req.getAddressTypeMap().getMain().get(0);
        if (main.getMobile() == null || main.getMobile().isBlank())
            throw new IllegalArgumentException("Mobile is mandatory");
        if (main.getEmail() == null || main.getEmail().isEmpty())
            throw new IllegalArgumentException("Email is mandatory");
    }

    //helpers
    private void validateDuplicateName(AddressMasterResponse req) {
        if (req.getAddressMasterPoid() == null) {
            if (masterRepo.existsByAddressNameIgnoreCase(req.getAddressName()))
                throw new IllegalArgumentException("Address Name already exists");
        } else {
            if (masterRepo.existsByAddressNameIgnoreCaseAndAddressMasterPoidNot(req.getAddressName(), req.getAddressMasterPoid()))
                throw new IllegalArgumentException("Address Name already exists");
        }
    }

    private AddressMaster buildOrUpdateMaster(AddressMasterResponse req, String currentUser) {
        AddressMaster master;
        if (req.getAddressMasterPoid() != null) {
            master = masterRepo.findById(req.getAddressMasterPoid())
                    .orElseThrow(() -> new NoSuchElementException("Address Master not found"));
        } else {
            master = new AddressMaster();
            master.setGroupPoid(1L);
            master.setDeleted("N"); // Ensure deleted flag is set
        }

        master.setAddressName(req.getAddressName());
        master.setAddressName2(req.getAddressName2());
        master.setCountryPoid(req.getCountryId());
        master.setPreferredCommunication(req.getPreferredCommunication() != null ? String.join(",", req.getPreferredCommunication()) : null);
        master.setPartyType(req.getPartyType() != null ? String.join(",", req.getPartyType()) : null);
        master.setWhatsappNo(req.getWhatsappNo());
        master.setLinkedIn(req.getLinkedIn());
        master.setInstagram(req.getInstagram());
        master.setFacebook(req.getFacebook());
        master.setRemarks(req.getRemarks());
        master.setCrNumber(req.getCrNumber());
        master.setIsForwarder(Boolean.TRUE.equals(req.getIsForwarder()) ? "Y" : "N");
        master.setActive(req.getActive());
        if (req.getSeqno() != null) {
            master.setSeqno(req.getSeqno());
        }

        return master;
    }

    private void saveAllDetails(AddressTypeMapDTO typeMap, AddressMaster master, String currentUser) {
        if (typeMap == null) return;

        String docId = UserContext.getDocumentId();
        String docKeyPoid = master.getAddressMasterPoid().toString();

        // Fetch existing details
        List<AddressDetails> existingDetails =
                detailsRepo.findByAddressMasterPoidOrderByAddressType(master.getAddressMasterPoid());

        Map<String, AddressDetails> existingMap = existingDetails.stream()
                .collect(Collectors.toMap(AddressDetails::getAddressPoid, d -> d));

        Map<String, AddressDetails> existingByType = existingDetails.stream()
                .collect(Collectors.toMap(
                        AddressDetails::getAddressType,
                        d -> d,
                        (a, b) -> a
                ));

        List<AddressDetails> toSave = new ArrayList<>();
        List<LogRequestDto<AddressDetails>> logRequests = new ArrayList<>();

        // --- MOST IMPORTANT FIX ---
        // Find the MAX used suffix for this addressMasterPoid
        int counter = getNextCounter(existingDetails, String.valueOf(master.getAddressMasterPoid()));
        // ---------------------------------------------

        // Combine all tabs
        Map<String, List<AddressDetailsDTO>> typedLists = new LinkedHashMap<>();
        if (typeMap.getMain() != null) typedLists.put("MAIN", typeMap.getMain());
        if (typeMap.getFinance() != null) typedLists.put("FINANCE", typeMap.getFinance());
        if (typeMap.getSales() != null) typedLists.put("SALES", typeMap.getSales());
        if (typeMap.getOperation() != null) typedLists.put("OPERATION", typeMap.getOperation());
        if (typeMap.getInvoiceAddress() != null) typedLists.put("INVOICE_ADDRESS", typeMap.getInvoiceAddress());
        if (typeMap.getDeliveryOrder() != null) typedLists.put("DELIVERY_ORDER", typeMap.getDeliveryOrder());
        if (typeMap.getShipChandling() != null) typedLists.put("SHIP_CHANDLING", typeMap.getShipChandling());
        if (typeMap.getClaimUac() != null) typedLists.put("CLAIM_UAC", typeMap.getClaimUac());
        if (typeMap.getCan() != null) typedLists.put("CAN", typeMap.getCan());

        for (Map.Entry<String, List<AddressDetailsDTO>> entry : typedLists.entrySet()) {
            String type = entry.getKey();
            if ("MAIN".equalsIgnoreCase(type)) {
                AddressDetailsDTO dto = entry.getValue().get(0);

                boolean hasMobile = StringUtils.isNotBlank(dto.getMobile());
                boolean hasEmail = dto.getEmail() != null && !dto.getEmail().isEmpty();
                if (!hasMobile || !hasEmail) {
                    throw new IllegalArgumentException("MAIN contact must have Mobile and Email");
                }

                AddressDetails existingMain = existingByType.get("MAIN");

                if (existingMain != null) {
                    AddressDetails oldCopy = new AddressDetails();
                    BeanUtils.copyProperties(existingMain, oldCopy);

                    updateDetail(existingMain, dto, type, currentUser);
                    toSave.add(existingMain);

                    String logDetail = "KeyId: ADDRESS_MASTER_POID:" + docKeyPoid +
                            " ADDRESS_POID:" + existingMain.getAddressPoid();

                    logRequests.add(new LogRequestDto<>(
                            oldCopy, existingMain, AddressDetails.class, docId, docKeyPoid, logDetail));

                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), oldCopy.getAddressPoid(), logDetail);

                } else {
                    AddressDetails detail = buildDetail(dto, master, "MAIN", counter++, currentUser);
                    toSave.add(detail);
                }
                continue;
            }
            for (AddressDetailsDTO dto : entry.getValue()) {
                String actionType = StringUtils.isBlank(dto.getActionType()) ? null : dto.getActionType();

                // Handle backward compatibility: if actionType is null, determine from addressPoid
                if (actionType == null) {
                    actionType = (dto.getAddressPoid() != null && existingMap.containsKey(dto.getAddressPoid()))
                            ? "isUpdated"
                            : "isCreated";
                }

                switch (actionType.toLowerCase()) {
                    case "nochange" -> {

                        if ("FINANCE".equalsIgnoreCase(type)
                                && StringUtils.isNotBlank(dto.getVerified())) {

                            AddressDetails existing = existingMap.get(dto.getAddressPoid());
                            if (existing != null) {

                                // Toggle ON
                                if ("Y".equalsIgnoreCase(dto.getVerified())
                                        && !"Y".equalsIgnoreCase(existing.getVerified())) {

                                    existing.setVerified("Y");
                                    existing.setVerifiedBy(
                                            UserContext.getUserName() != null
                                                    ? UserContext.getUserName()
                                                    : UserContext.getUserId()
                                    );
                                    existing.setVerifiedDate(DateUtil.getCurrentDateInUserTimeZone());
                                    toSave.add(existing);
                                }

                                // Toggle OFF
                                if ("N".equalsIgnoreCase(dto.getVerified())) {
                                    existing.setVerified("N");
                                    existing.setVerifiedBy(null);
                                    existing.setVerifiedDate(null);
                                    toSave.add(existing);
                                }
                            }
                        }
                        continue;
                    }


                    case "iscreated" -> {
                        AddressDetails detail = buildDetail(dto, master, type, counter++, currentUser);
                        toSave.add(detail);
                    }
                    case "isupdated" -> {
                        if (dto.getAddressPoid() != null && existingMap.containsKey(dto.getAddressPoid())) {
                            AddressDetails oldDetail = existingMap.get(dto.getAddressPoid());
                            AddressDetails oldCopy = new AddressDetails();
                            BeanUtils.copyProperties(oldDetail, oldCopy);

                            // Capture old verified value BEFORE update
                            String oldVerified = oldDetail.getVerified();

                            updateDetail(oldDetail, dto, type, currentUser);
                            // VERIFIED handling (FINANCE only)
                            if ("FINANCE".equalsIgnoreCase(type)
                                    && "Y".equalsIgnoreCase(dto.getVerified())
                                    && (oldDetail.getVerifiedBy() == null || oldDetail.getVerifiedDate() == null)) {

                                oldDetail.setVerified("Y");
                                oldDetail.setVerifiedBy(UserContext.getUserId());
                                oldDetail.setVerifiedDate(DateUtil.getCurrentDateInUserTimeZone());
                            }

                            toSave.add(oldDetail);

                            String logDetail = String.format("KeyId: ADDRESS_MASTER_POID:%s ADDRESS_POID:%s", oldDetail.getAddressMasterPoid() , oldDetail.getAddressPoid());
                            loggingService.createLogSummaryEntry(UserContext.getDocumentId(), oldCopy.getAddressPoid(), logDetail);
                            logRequests.add(new LogRequestDto<>(oldCopy, oldDetail, AddressDetails.class, docId, docKeyPoid, logDetail));
                        } else {
                            // Address not found, treat as create
                            AddressDetails detail = buildDetail(dto, master, type, counter++, currentUser);
                            toSave.add(detail);
                        }
                    }
                    case "isdeleted" -> {
                        // Delete the specific record from database
                        if (dto.getAddressPoid() != null && existingMap.containsKey(dto.getAddressPoid())) {
                            AddressDetails recordToDelete = existingMap.get(dto.getAddressPoid());
                            detailsRepo.delete(recordToDelete);
                        }
                    }
                    default -> {
                        // Default behavior for backward compatibility
                        AddressDetails detail;
                        if (dto.getAddressPoid() != null && existingMap.containsKey(dto.getAddressPoid())) {
                            detail = existingMap.get(dto.getAddressPoid());
                            updateDetail(detail, dto, type, currentUser);
                        } else {
                            detail = buildDetail(dto, master, type, counter++, currentUser);
                        }
                        toSave.add(detail);
                    }
                }
            }
        }

        //  Save updated and new details
        if (!toSave.isEmpty()) {
            detailsRepo.saveAll(toSave);
        }

        // Create logs for all updates
        if (!logRequests.isEmpty()) {
            loggingService.createLogBatch(logRequests);
        }
    }

    private void updateDetail(AddressDetails entity, AddressDetailsDTO dto, String type, String currentUser) {
        entity.setAddressType(type);
        entity.setContactPerson(dto.getContactPerson());
        entity.setDesignation(dto.getDesignation());
        entity.setOffTel1(dto.getOffTel1());
        entity.setOffTel2(dto.getOffTel2());
        entity.setMobile(dto.getMobile());
        entity.setFax(dto.getFax());

        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            entity.setEmail(dto.getEmail().get(0));
            entity.setEmail2(dto.getEmail().size() > 1 ? dto.getEmail().get(1) : null);
        } else {
            entity.setEmail(null);
            entity.setEmail2(null);
        }

        entity.setWebsite(dto.getWebsite());
        entity.setPoBox(dto.getPoBox());
        entity.setOffNo(dto.getOffNo());
        entity.setBldg(dto.getBldg());
        entity.setRoad(dto.getRoad());

        String area = dto.getArea();
        String city = dto.getCity();
        if ((city == null || city.isBlank()) && area != null && area.contains(",")) {
            String[] parts = area.split(",", 2);
            area = parts[0].trim();
            city = parts.length > 1 ? parts[1].trim() : null;
        }
        entity.setAreaCity(area);
        entity.setCity(city);

        entity.setState(dto.getState() != null && !dto.getState().isEmpty()
                ? String.join(",", dto.getState())
                : null
        );
        entity.setLandMark(dto.getLandMark());
        if (StringUtils.isNotBlank(dto.getVerified())) {
            entity.setVerified(dto.getVerified());
        }
        entity.setWhatsappNo(dto.getWhatsappNo());
        entity.setLinkedin(dto.getLinkedIn());
        entity.setInstagram(dto.getInstagram());
        entity.setFacebook(dto.getFacebook());

    }

    private AddressDetails buildDetail(AddressDetailsDTO dto, AddressMaster master, String type, int counter, String currentUser) {
        AddressDetails detail = new AddressDetails();

        String suffix = String.format("%03d", counter); // 001, 010, 011

        detail.setAddressPoid(
                dto.getAddressPoid() == null
                        ? master.getAddressMasterPoid() + "." + suffix
                        : String.valueOf(dto.getAddressPoid())
        );

        detail.setAddressMasterPoid(master.getAddressMasterPoid());
        detail.setAddressType(type);
        detail.setContactPerson(dto.getContactPerson());
        detail.setDesignation(dto.getDesignation());
        detail.setOffTel1(dto.getOffTel1());
        detail.setOffTel2(dto.getOffTel2());
        detail.setMobile(dto.getMobile());
        detail.setFax(dto.getFax());

        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            detail.setEmail(dto.getEmail().get(0));
            if (dto.getEmail().size() > 1) {
                detail.setEmail2(dto.getEmail().get(1));
            }
        }

        detail.setWebsite(dto.getWebsite());
        detail.setPoBox(dto.getPoBox());
        detail.setOffNo(dto.getOffNo());
        detail.setBldg(dto.getBldg());
        detail.setRoad(dto.getRoad());

        String area = dto.getArea();
        String city = dto.getCity();
        if ((city == null || city.isBlank()) && area != null && area.contains(",")) {
            String[] parts = area.split(",", 2);
            area = parts[0].trim();
            city = parts.length > 1 ? parts[1].trim() : null;
        }
        detail.setAreaCity(area);
        detail.setCity(city);

        detail.setState(dto.getState() != null && !dto.getState().isEmpty()
                ? String.join(",", dto.getState())
                : null
        );

        detail.setLandMark(dto.getLandMark());
        // VERIFIED fields must be backend-controlled
        detail.setVerified("N");
        detail.setVerifiedBy(null);
        detail.setVerifiedDate(null);
        detail.setWhatsappNo(dto.getWhatsappNo());
        detail.setLinkedin(dto.getLinkedIn());
        detail.setInstagram(dto.getInstagram());
        detail.setFacebook(dto.getFacebook());

        return detail;
    }

    // --- Stored procedure calls (unchanged) ---

    /**
     * Convert AddressMaster -> DTO
     */
    private AddressMasterResponse buildMasterResponse(AddressMaster m, List<AddressDetails> details) {
        AddressMasterResponse resp = new AddressMasterResponse();
        resp.setAddressMasterPoid(m.getAddressMasterPoid());
        resp.setAddressName(m.getAddressName());
        resp.setAddressName2(m.getAddressName2());
        resp.setPreferredCommunication(m.getPreferredCommunication() != null
                ? Arrays.asList(m.getPreferredCommunication().split(","))
                : List.of());
        resp.setPartyType(m.getPartyType() != null
                ? Arrays.asList(m.getPartyType().split(","))
                : List.of());
        resp.setWhatsappNo(m.getWhatsappNo());
        resp.setLinkedIn(m.getLinkedIn());
        resp.setInstagram(m.getInstagram());
        resp.setFacebook(m.getFacebook());
        resp.setRemarks(m.getRemarks());
        resp.setCrNumber(m.getCrNumber());
        resp.setIsForwarder("Y".equalsIgnoreCase(m.getIsForwarder()));
        resp.setActive(m.getActive());
        resp.setSeqno(m.getSeqno());
        
        // Set audit fields
        resp.setCreatedBy(m.getCreatedBy());
        resp.setCreatedDate(m.getCreatedDate() != null ? m.getCreatedDate() : null);
        resp.setLastModifiedBy(m.getLastModifiedBy());
        resp.setLastModifiedDate(m.getLastModifiedDate() != null ? m.getLastModifiedDate() : null);

        if (m.getCountryPoid() != null && m.getCountryPoid() != 0) {
            countryRepo.findById(m.getCountryPoid()).ifPresent(c -> {
                resp.setCountryId(c.getCountryPoid());
                resp.setCountryName(c.getCountryName());
            });
        }


        if (details != null) {
            details.stream()
                    .filter(d -> "MAIN".equalsIgnoreCase(d.getAddressType()))
                    .findFirst()
                    .ifPresentOrElse(main -> {
                        resp.setMobile(main.getMobile() != null ? main.getMobile() : "");
                        List<String> emails = new ArrayList<>();
                        if (main.getEmail() != null) emails.add(main.getEmail());
                        if (main.getEmail2() != null) emails.add(main.getEmail2());
                        resp.setEmail(emails);
                    }, () -> {
                        resp.setMobile("");
                        resp.setEmail(List.of());
                    });
        } else {
            resp.setMobile("");
            resp.setEmail(List.of());
        }

        return resp;
    }

    /**
     * Map AddressDetails -> DTO with area+city split handling
     */
    private AddressDetailsDTO mapToDTO(AddressDetails d) {
        List<String> emails = new ArrayList<>();
        if (d.getEmail() != null && !d.getEmail().isBlank()) emails.add(d.getEmail());
        if (d.getEmail2() != null && !d.getEmail2().isBlank()) emails.add(d.getEmail2());

        String area = d.getAreaCity();
        String city = d.getCity();
        if ((city == null || city.isBlank()) && area != null && area.contains(",")) {
            String[] parts = area.split(",", 2);
            area = parts[0].trim();
            city = parts.length > 1 ? parts[1].trim() : null;
        }

        return AddressDetailsDTO.builder()
                .addressPoid(d.getAddressPoid() != null ? String.valueOf(d.getAddressPoid()) : null)
                .addressType(d.getAddressType())
                .contactPerson(d.getContactPerson())
                .designation(d.getDesignation())
                .offTel1(d.getOffTel1())
                .offTel2(d.getOffTel2())
                .mobile(d.getMobile())
                .fax(d.getFax())
                .email(emails)
                .website(d.getWebsite())
                .poBox(d.getPoBox())
                .offNo(d.getOffNo())
                .bldg(d.getBldg())
                .road(d.getRoad())
                .area(area)
                .city(city)
                .state(d.getState() != null
                        ? Arrays.stream(d.getState().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList()
                        : List.of())
                .landMark(d.getLandMark())
                .verified(d.getVerified())
                .verifiedBy(d.getVerifiedBy())
                .verifiedDate(d.getVerifiedDate())
                .createdBy(d.getCreatedBy())
                .createdDate(d.getCreatedDate())
                .lastModifiedBy(d.getLastModifiedBy())
                .lastModifiedDate(d.getLastModifiedDate())
                .whatsappNo(d.getWhatsappNo())
                .linkedIn(d.getLinkedin())
                .instagram(d.getInstagram())
                .facebook(d.getFacebook())
                .build();
    }

    private int getNextCounter(List<AddressDetails> existingDetails, String masterPoid) {
        int max = 0;

        for (AddressDetails d : existingDetails) {
            String poid = d.getAddressPoid();
            if (poid != null && poid.startsWith(masterPoid + ".")) {
                String suffix = poid.substring(poid.indexOf('.') + 1).trim();
                if (suffix.matches("\\d+")) {
                    int num = Integer.parseInt(suffix);
                    max = Math.max(max, num);
                }
            }
        }

        return max + 1;
    }
}
