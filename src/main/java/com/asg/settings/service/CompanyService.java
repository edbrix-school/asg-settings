package com.asg.settings.service;

import com.asg.common.lib.dto.*;
import com.asg.common.lib.entity.Company;
import com.asg.common.lib.entity.CompanyDivisionEntity;
import com.asg.common.lib.entity.TimeZoneEntity;
import com.asg.common.lib.entity.key.CompanyDivisionEntityKey;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.exception.ValidationException;
import com.asg.common.lib.repository.TimeZoneDataRepository;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.service.LovDataService;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.settings.dto.UserCompanyDto;
import com.asg.settings.entity.DivisionMasterEntity;
import com.asg.settings.entity.State;
import com.asg.settings.entity.UsersCompanyEntity;
import com.asg.settings.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {

    public static final String IS_DELETED = "isDeleted";
    public static final String IS_UPDATED = "isUpdated";
    public static final String IS_CREATED = "isCreated";
    public static final String NO_CHANGE = "noChange";

    private final CompanyRepository companyRepository;

    private final CountryRepository countryRepository;

    private final StateRepository stateRepository;

    private final UsersCompanyRepository usersCompanyRepository;

    private final CompanyDivisionRepository companyDivisionRepository;

    private final DivisionRepository divisionRepository;

    private final TimeZoneDataRepository timeZoneRepository;

    private final DocumentSearchService documentService;

    private final LoggingService loggingService;

    private final LovDataService lovDataService;

    private final CurrencyService currencyService;

    private final DocumentDeleteService documentDeleteService;

    // Method to get User Companies mapped to User
    public List<UserCompanyDto> getUsersCompanies(Long userPoid) {
        try {
            List<UsersCompanyEntity> companyList = usersCompanyRepository.findCompanyAccess(userPoid);
            List<UserCompanyDto> result = new ArrayList<>();

            for (UsersCompanyEntity usersCompanyEntity : companyList) {
                LocalDate expiry = usersCompanyEntity.getExpiryDate();
                Long companyPoid = usersCompanyEntity.getId().getCompanyPoid();
                Company company = companyRepository.findByCompanyPoid(companyPoid);
                TimeZoneEntity timeZoneEntity = timeZoneRepository.findByTimezoneId(company.getTimezoneId());
                TimeZoneDto timeZoneDto = timeZoneEntity != null ? new TimeZoneDto(timeZoneEntity.getTimezoneId(), timeZoneEntity.getTimezoneCode(), timeZoneEntity.getTimezoneName()) : null;
                String countryCode = company.getCountryId() != null ? getCountryCodeForCompany(companyPoid) : null;
                State state = getStateForCompany(company.getCountryId(), company.getStateId());
                String stateName = state != null ? state.getStateName() : null;
                String dateFormat = company.getDateFormat() != null ? company.getDateFormat() : null;
                result.add(new UserCompanyDto(company.getCompanyPoid(), company.getCompanyName(), countryCode, stateName, expiry, company.getDeleted(), timeZoneDto, dateFormat, company.getActive(), ""));
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // Method to get Companies mapped to User
    public List<Company> getCompanies(Long userPoid) {
        try {
            List<UsersCompanyEntity> companyList = usersCompanyRepository.findCompanyAccess(userPoid);
            List<Company> companies = new ArrayList<>();

            for (UsersCompanyEntity usersCompanyEntity : companyList) {
                Long companyPoid = usersCompanyEntity.getId().getCompanyPoid();
                Company company = companyRepository.findByCompanyPoid(companyPoid);

                company.setLabel(company.getCompanyName()); // for dropdown UI
                company.setValue(company.getCompanyPoid()); // for dropdown UI

                if (company.getDateFormat() != null) {
                    company.setDateFormat(company.getDateFormat());
                }

                if (company.getCountryId() != null) {
                    String countryCode = getCountryCodeForCompany(company.getCompanyPoid());
                    company.setCountryCode(countryCode);

                    if (company.getStateId() != null) {
                        State state = getStateForCompany(company.getCountryId(), company.getStateId());
                        company.setStateName(state != null ? state.getStateName() : null);
                    }
                }

                if (company.getTimezoneId() != null) {
                    TimeZoneEntity timeZoneEntity = timeZoneRepository.findByTimezoneId(company.getTimezoneId());
                    if (timeZoneEntity != null) {
                        company.setTimeZone(new TimeZoneDto(timeZoneEntity.getTimezoneId(), timeZoneEntity.getTimezoneCode(), timeZoneEntity.getTimezoneName()));
                    }
                }

                companies.add(company);
            }

            return companies;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    public CompanyDto getCompany(Long companyId) {
        try {
            Company company = companyRepository.findByCompanyPoid(companyId);
            if (company == null) {
                throw new ResourceNotFoundException("Company", "companyId", companyId.toString());
            }
            company.setLabel(company.getCompanyName()); //label(name)
            company.setValue(company.getCompanyPoid()); //value(primarykey)
            if (company.getTimezoneId() != null) {
                TimeZoneEntity timeZoneEntity = timeZoneRepository.findByTimezoneId(company.getTimezoneId());
                TimeZoneDto timeZoneDto = timeZoneEntity != null ? new TimeZoneDto(timeZoneEntity.getTimezoneId(), timeZoneEntity.getTimezoneCode(), timeZoneEntity.getTimezoneName()) : null;
                company.setTimeZone(timeZoneDto);
            }
            String countryCode = getCountryCodeForCompany(company.getCompanyPoid());
            company.setCountryCode(countryCode);

            List<CompanyDivisionEntity> divisions = companyDivisionRepository.findById_CompanyPoid(company.getCompanyPoid());
            if (divisions != null && !divisions.isEmpty()) {
                List<Long> divPoids = divisions.stream().map(CompanyDivisionEntity::getDivPoid).filter(Objects::nonNull).toList();
                if (!divPoids.isEmpty()) {
                    Map<Long, String> divisionNames = divisionRepository.findAllById(divPoids).stream()
                        .collect(Collectors.toMap(DivisionMasterEntity::getDivisionId, DivisionMasterEntity::getDivisionName));
                    divisions.forEach(div -> {
                        if (div.getDivPoid() != null) {
                            div.setDivisionName(divisionNames.get(div.getDivPoid()));
                        }
                    });
                }
            }
            company.setDivisions(divisions);

            if (company.getCountryId() != null && company.getStateId() != null) {
                State state = getStateForCompany(company.getCountryId(), company.getStateId());
                company.setStateName(state != null ? state.getStateName() : null);
            }

            return toDto(company);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public com.asg.common.lib.dto.CompanySimpleDto getCompanySimple(Long companyPoid) {
        Company company = companyRepository.findByCompanyPoid(companyPoid);
        if (company == null) {
            throw new ResourceNotFoundException("Company", "companyPoid", companyPoid);
        }
        return com.asg.common.lib.dto.CompanySimpleDto.builder()
                .companyPoid(company.getCompanyPoid())
                .companyCode(company.getCompanyCode())
                .companyName(company.getCompanyName())
                .vatFilingPeriod(company.getVatFilingPeriod())
                .build();
    }

    //Replaced with this to just get country code instead
    public String getCountryCodeForCompany(Long companyPoid) {
        String countryPoid = companyRepository.findCountryIdByCompanyPoid(companyPoid);
        if (countryPoid != null) {

            return countryRepository.findCountryCodeByCountryPoid(Long.valueOf(countryPoid));
        }
        return null;
    }


    public State getStateForCompany(String countryPoid, String statePoid) {
        if (countryPoid != null && statePoid != null) {
            Long countryId = Long.valueOf(countryPoid);
            Long stateId = Long.valueOf(statePoid);
            if (stateRepository.existsByCountryPoidAndStatePoid(countryId, stateId)) {
                return stateRepository.findByCountryPoidAndStatePoid(countryId, stateId);
            }
        }
        return null;
    }

    public Map<String, Object> listCompanies(String docId, FilterRequestDto request, Pageable pageable) {
        String operator = documentService.resolveOperator(request);
        String isDeleted = documentService.resolveIsDeleted(request);
        List<FilterDto> filters = documentService.resolveFilters(request);

        RawSearchResult raw = documentService.search(docId, filters, operator, pageable, isDeleted,
                "COMPANY_NAME",   // label
                "COMPANY_POID");    // value

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());

    }

    @Transactional
    // Modified method signature to accept actionType
    public String saveOrUpdateCompany(Company company) {

        // Handle company creation/update
        boolean isExistingCompany = company.getCompanyPoid() != null && company.getCompanyPoid() != 0;
        Long updatedCompanyId;
        String userId = UserContext.getUserId() != null ? UserContext.getUserId() : null;

        if (!isExistingCompany) {
            // Create new company
            updatedCompanyId = createNewCompany(company, userId);
        } else {
            // Update existing company
            updatedCompanyId = updateExistingCompany(company, userId);
        }

        return updatedCompanyId.toString();
    }

    private void processDivisions(Long companyPoid, List<CompanyDivisionEntity> divisions) {
        if (divisions != null && !divisions.isEmpty()) {
            // Check for duplicate divPoid in the same request
            Map<Long, String> divisionActions = new HashMap<>();
            for (CompanyDivisionEntity div : divisions) {
                if (div.getDivPoid() != null && div.actionType != null && !NO_CHANGE.equalsIgnoreCase(div.actionType)) {
                    if (divisionActions.containsKey(div.getDivPoid())) {
                        throw new ValidationException("Duplicate division operations detected for divPoid: " + div.getDivPoid());
                    }
                    divisionActions.put(div.getDivPoid(), div.actionType);
                }
            }
            
            divisions.forEach(division -> {
                // Skip processing if divPoid is null or actionType is null/noChange
                if (division.getDivPoid() == null ||
                        division.actionType == null ||
                        NO_CHANGE.equalsIgnoreCase(division.actionType)) {
                    return;
                }

                if (IS_CREATED.equalsIgnoreCase(division.actionType)) {
                    createDivision(companyPoid, division);
                } else if (IS_UPDATED.equalsIgnoreCase(division.actionType)) {
                    updateDivision(companyPoid, division);
                } else if (IS_DELETED.equalsIgnoreCase(division.actionType)) {
                    deleteDivision(companyPoid, division);
                }
            });
        }
    }

    private void createDivision(Long companyPoid, CompanyDivisionEntity division) {

        CompanyDivisionEntity existingDivision = companyDivisionRepository.findById_CompanyPoidAndDivPoid(companyPoid, division.getDivPoid());

        if (existingDivision == null) {
            // Handle null division name by fetching from master data
            String divisionName = division.getDivisionName();
            if (divisionName == null || divisionName.trim().isEmpty()) {
                Optional<DivisionMasterEntity> masterDiv =
                    divisionRepository.findById(division.getDivPoid());
                divisionName = masterDiv.map(DivisionMasterEntity::getDivisionName)
                    .orElseThrow(() -> new ValidationException("Division not found in master data for divPoid: " + division.getDivPoid()));
            }
            
            CompanyDivisionEntity companyDivision = new CompanyDivisionEntity();
            companyDivision.setDivisionName(divisionName);
            companyDivision.setRemarks(division.getRemarks());
            companyDivision.setLogoImageBase64(division.getLogoImageBase64());
            companyDivision.setCompanyDivAddress(division.getCompanyDivAddress());
            companyDivision.setCompanyDivAddressPos(division.getCompanyDivAddressPos());
            companyDivision.setDivPoid(division.getDivPoid());

            CompanyDivisionEntityKey key = new CompanyDivisionEntityKey();
            key.setCompanyPoid(companyPoid);
            key.setDetRowId(getNextDetRowIdForCompanyDivison(companyPoid));

            companyDivision.setId(key);

            companyDivisionRepository.saveAndFlush(companyDivision);
            String logDetail = String.format("Row Created on Company Division with DetRowId %s ", companyDivision.getId().getDetRowId());
            loggingService.createLogSummaryEntry(UserContext.getDocumentId(), companyPoid.toString(), logDetail);
        } else {
            throw new ValidationException("You are attempting to create the same division multiple times. Please review your selection. divisionId -> " + division.getDivPoid());
        }
    }

    private void updateDivision(Long companyPoid, CompanyDivisionEntity division) {
        CompanyDivisionEntity existingDivision = companyDivisionRepository
                .findById_CompanyPoidAndId_DetRowId(companyPoid, division.getDetRowId());

        if (existingDivision != null) {
            CompanyDivisionEntity oldCompanyData = new CompanyDivisionEntity();
            BeanUtils.copyProperties(existingDivision, oldCompanyData);

            existingDivision.setDivisionName(division.getDivisionName());
            existingDivision.setRemarks(division.getRemarks());
            existingDivision.setLogoImageBase64((division.getLogoImageBase64()));
            existingDivision.setCompanyDivAddress(division.getCompanyDivAddress());
            existingDivision.setCompanyDivAddressPos(division.getCompanyDivAddressPos());
            String logDetail = String.format("KeyId = COMPANY_POID %s: DET_ROW_ID %s", companyPoid, division.getDetRowId());
            loggingService.createLog(oldCompanyData, existingDivision, CompanyDivisionEntity.class, UserContext.getDocumentId(), companyPoid.toString(), logDetail);
            companyDivisionRepository.saveAndFlush(existingDivision);
        } else {
            throw new ValidationException("Cannot update a division that is not assigned to the company, detRowId -> " + division.getDetRowId());
        }
    }

    private void deleteDivision(Long companyPoid, CompanyDivisionEntity division) {
        // For delete, find existing division by divPoid first
        CompanyDivisionEntity existingDivision = companyDivisionRepository
                .findById_CompanyPoidAndDivPoid(companyPoid, division.getDivPoid());

        if (existingDivision != null) {
            Long detRowId = existingDivision.getId().getDetRowId();
            companyDivisionRepository.deleteById_CompanyPoidAndId_DetRowId(companyPoid, detRowId);
            loggingService.logDelete(existingDivision, UserContext.getDocumentId(), companyPoid.toString());
        } else {
            throw new ValidationException("Cannot delete a division that is not assigned to the company, divPoid -> " + division.getDivPoid());
        }
    }

    private Long createNewCompany(Company company, String userId) {
        if (companyRepository.existsByCompanyNameIgnoreCase(company.getCompanyName())) {
            throw new ValidationException("Company Name already exists, please enter unique name.");
        }
        if (companyRepository.existsByCompanyCodeIgnoreCase(company.getCompanyCode())) {
            throw new ValidationException("Company Code already exists, please enter unique code.");
        }

        if (company.getTinNumber() != null && !company.getTinNumber().trim().isEmpty() &&
                companyRepository.existsByTinNumberIgnoreCase(company.getTinNumber())) {
            throw new ValidationException("Tin number already exists, please enter unique tin number.");
        }

        Company newCompany = new Company();

        newCompany.setCompanyPoid(null);

        // Initialize all 8 tracking fields for new company
        LocalDateTime currentDateTime = com.asg.common.lib.utility.DateUtil.getCurrentDateTimeInUserTimeZone();
        newCompany.setFinancialDateUpdatedBy(userId);
        newCompany.setFinancialDateUpdatedDate(currentDateTime);
        newCompany.setTransDateUpdatedBy(userId);
        newCompany.setTransDateUpdatedDate(currentDateTime);
        newCompany.setReportDateUpdatedBy(userId);
        newCompany.setReportDateUpdatedDate(currentDateTime);
        newCompany.setInventoryDateUpdatedBy(userId);
        newCompany.setInventoryDateUpdatedDate(currentDateTime);
        // Set VAT filing audit fields on CREATE also
        newCompany.setVatLastFiledBy(userId);
        newCompany.setVatLastFiledCreatedDate(currentDateTime);


        // Set all other fields from the input company
        setCompanyFields(newCompany, company);

        newCompany = companyRepository.saveAndFlush(newCompany);

        String docId = UserContext.getDocumentId();
        String key = newCompany.getCompanyPoid().toString();

        loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, docId, key);

        // Handle divisions for new company
        processDivisions(newCompany.getCompanyPoid(), company.getDivisions());

        return newCompany.getCompanyPoid();
    }

    private Long updateExistingCompany(Company company, String userId) {

        Company existingCompany = companyRepository.findByCompanyPoid(company.getCompanyPoid());
        if (existingCompany == null) {
            throw new ValidationException("Company not found with poid: " + company.getCompanyPoid());
        }
        // Prevent Company Code modification during update
        if (!Objects.equals(company.getCompanyCode(), existingCompany.getCompanyCode())) {
            throw new ValidationException("Company Code cannot be modified after creation.");
        }
        if (companyRepository.existsByCompanyNameIgnoreCaseAndCompanyPoidNot(company.getCompanyName(), company.getCompanyPoid())) {
            throw new ValidationException("Company Name already exists, please enter unique name.");
        }
        if (companyRepository.existsByCompanyNameIgnoreCaseAndCompanyPoidNot(
                company.getCompanyName(), company.getCompanyPoid())) {
            throw new ValidationException("Company Name already exists, please enter unique name.");
        }

        // Make copy of old company for logging
        Company oldCompany = new Company();
        BeanUtils.copyProperties(existingCompany, oldCompany);


        // Check for field changes and update tracking fields
        LocalDateTime currentDateTime = com.asg.common.lib.utility.DateUtil.getCurrentDateTimeInUserTimeZone();

        if (!Objects.equals(company.getFinancialPeriodStart(), existingCompany.getFinancialPeriodStart()) ||
                !Objects.equals(company.getFinancialPeriodEnd(), existingCompany.getFinancialPeriodEnd())) {
            existingCompany.setFinancialDateUpdatedBy(userId);
            existingCompany.setFinancialDateUpdatedDate(currentDateTime);
        }

        if (!Objects.equals(company.getTransPeriodStart(), existingCompany.getTransPeriodStart()) ||
                !Objects.equals(company.getTransPeriodEnd(), existingCompany.getTransPeriodEnd())) {
            existingCompany.setTransDateUpdatedBy(userId);
            existingCompany.setTransDateUpdatedDate(currentDateTime);
        }

        if (!Objects.equals(company.getReportPeriodStart(), existingCompany.getReportPeriodStart()) ||
                !Objects.equals(company.getReportPeriodEnd(), existingCompany.getReportPeriodEnd())) {
            existingCompany.setReportDateUpdatedBy(userId);
            existingCompany.setReportDateUpdatedDate(currentDateTime);
        }

        if (!Objects.equals(company.getStockPeriodStart(), existingCompany.getStockPeriodStart()) ||
                !Objects.equals(company.getStockPeriodEnd(), existingCompany.getStockPeriodEnd())) {
            existingCompany.setInventoryDateUpdatedBy(userId);
            existingCompany.setInventoryDateUpdatedDate(currentDateTime);
        }

        // Track VAT filing changes
        if (!Objects.equals(company.getVatLastFiledDate(), existingCompany.getVatLastFiledDate()) ||
                !Objects.equals(company.getVatLastFiledBy(), existingCompany.getVatLastFiledBy())) {
            existingCompany.setVatLastFiledBy(userId);
            existingCompany.setVatLastFiledCreatedDate(currentDateTime);
        }

        setCompanyFields(existingCompany, company);

        try {
            existingCompany = companyRepository.saveAndFlush(existingCompany);

            String docId = UserContext.getDocumentId();
            String key = existingCompany.getCompanyPoid().toString();

            loggingService.logChanges(oldCompany, existingCompany, Company.class, docId, key, LogDetailsEnum.MODIFIED, "COMPANY_POID");

        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("GLOBAL_COMPANY_MAST_UK_VAT")) {
                throw new ValidationException("Tin number already exists, please enter unique tin number.");
            }
            throw e;
        }

        // Handle divisions for updated company
        processDivisions(existingCompany.getCompanyPoid(), company.getDivisions());

        return existingCompany.getCompanyPoid();
    }

    private void setCompanyFields(Company targetCompany, Company sourceCompany) {

        // Copy all fields from source to target
        targetCompany.setGroupPoid(1L);
        targetCompany.setCompanyCode(sourceCompany.getCompanyCode());
        targetCompany.setCompanyName(sourceCompany.getCompanyName());
        targetCompany.setCompanyName2(sourceCompany.getCompanyName2());
        targetCompany.setContactPerson(sourceCompany.getContactPerson());
        targetCompany.setTelephone(sourceCompany.getTelephone());
        targetCompany.setFax(sourceCompany.getFax());
        targetCompany.setEmail(sourceCompany.getEmail());
        targetCompany.setCountryId(sourceCompany.getCountryId());
        targetCompany.setStateId(sourceCompany.getStateId());
        targetCompany.setCompanyColor(sourceCompany.getCompanyColor());
        targetCompany.setAddress(sourceCompany.getAddress());
        targetCompany.setFinancialPeriodStart(sourceCompany.getFinancialPeriodStart());
        targetCompany.setFinancialPeriodEnd(sourceCompany.getFinancialPeriodEnd());
        targetCompany.setReportPeriodStart(sourceCompany.getReportPeriodStart());
        targetCompany.setReportPeriodEnd(sourceCompany.getReportPeriodEnd());
        targetCompany.setActive(sourceCompany.getActive());
        targetCompany.setSeqNo(sourceCompany.getSeqNo());
        targetCompany.setDeleted(sourceCompany.getDeleted());
        targetCompany.setTransPeriodStart(sourceCompany.getTransPeriodStart());
        targetCompany.setTransPeriodEnd(sourceCompany.getTransPeriodEnd());
        targetCompany.setProvisionalClosedDate(sourceCompany.getProvisionalClosedDate());
        targetCompany.setBankDetail(sourceCompany.getBankDetail());
        targetCompany.setBankPoid(sourceCompany.getBankPoid());
        targetCompany.setTinNumber(sourceCompany.getTinNumber());
        targetCompany.setAccountPerson(sourceCompany.getAccountPerson());
        targetCompany.setStockPeriodStart(sourceCompany.getStockPeriodStart());
        targetCompany.setStockPeriodEnd(sourceCompany.getStockPeriodEnd());
        targetCompany.setAccountEmail(sourceCompany.getAccountEmail());
        // Note: VAT filing, Financial/Trans/Report/Inventory tracking fields are managed by system in updateExistingCompany()
        targetCompany.setVatLastFiledDate(sourceCompany.getVatLastFiledDate());
        targetCompany.setVatFilingPeriod(sourceCompany.getVatFilingPeriod());
        targetCompany.setVatRegistrationDate(sourceCompany.getVatRegistrationDate());
        // vatLastFiledBy and vatLastFiledCreatedDate are system-managed

        targetCompany.setLogoImageBase64(sourceCompany.getLogoImageBase64());
        targetCompany.setDateFormat(sourceCompany.getDateFormat());
        targetCompany.setTimezoneId(sourceCompany.getTimezoneId());
        targetCompany.setCurrencyPoid(sourceCompany.getCurrencyPoid());
        targetCompany.setSubmissionPeriod(sourceCompany.getSubmissionPeriod());
        
        // Note: Financial/Trans/Report/Inventory date tracking fields are handled conditionally above

    }

    @Transactional
    public void softDeleteCompany(Long companyPoid, DeleteReasonDto deleteReasonDto) {
        Company company = companyRepository.findById(companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "companyPoid", companyPoid));
        
        documentDeleteService.deleteDocument(
                companyPoid,
                "GLOBAL_COMPANY_MASTER",
                "COMPANY_POID",
                deleteReasonDto,
                null
        );
    }

    public Long getNextDetRowIdForCompanyDivison(Long companyPoid) {
        Long max = companyDivisionRepository.findMaxDetRowIdByCompanyPoid(companyPoid);
        return (max == null) ? 1L : max + 1;
    }

    private CompanyDto toDto(Company entity) {
        if (entity == null) {
            return null;
        }
        CompanyDto dto = new CompanyDto();

        dto.setGroupPoid(entity.getGroupPoid());
        dto.setCompanyPoid(entity.getCompanyPoid());
        dto.setCompanyCode(entity.getCompanyCode());
        dto.setCompanyName(entity.getCompanyName());
        dto.setLabel(entity.getLabel());
        dto.setValue(entity.getValue());
        dto.setTimeZone(entity.getTimeZone());
        dto.setCompanyName2(entity.getCompanyName2());
        dto.setContactPerson(entity.getContactPerson());
        dto.setTelephone(entity.getTelephone());
        dto.setFax(entity.getFax());
        dto.setEmail(entity.getEmail());
        dto.setCountryId(entity.getCountryId());
        dto.setAddress(entity.getAddress());
        dto.setFinancialPeriodStart(entity.getFinancialPeriodStart());
        dto.setFinancialPeriodEnd(entity.getFinancialPeriodEnd());
        dto.setReportPeriodStart(entity.getReportPeriodStart());
        dto.setReportPeriodEnd(entity.getReportPeriodEnd());
        dto.setTransPeriodStart(entity.getTransPeriodStart());
        dto.setTransPeriodEnd(entity.getTransPeriodEnd());
        dto.setActive(entity.getActive());
        dto.setSeqNo(entity.getSeqNo());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setLastModifiedBy(entity.getLastModifiedBy());
        dto.setLastModifiedDate(entity.getLastModifiedDate());
        dto.setDeleted(entity.getDeleted());
        dto.setProvisionalClosedDate(entity.getProvisionalClosedDate());
        dto.setBankDetail(entity.getBankDetail());
        dto.setBankPoid(entity.getBankPoid());
        dto.setTinNumber(entity.getTinNumber());
        dto.setVatRegistrationDate(entity.getVatRegistrationDate());
        dto.setVatLastFiledDate(entity.getVatLastFiledDate());
        dto.setAccountPerson(entity.getAccountPerson());
        dto.setStockPeriodStart(entity.getStockPeriodStart());
        dto.setStockPeriodEnd(entity.getStockPeriodEnd());
        dto.setVatFilingPeriod(entity.getVatFilingPeriod());
        dto.setAccountEmail(entity.getAccountEmail());
        dto.setVatLastFiledBy(entity.getVatLastFiledBy());
        dto.setVatLastFiledCreatedDate(entity.getVatLastFiledCreatedDate());
        dto.setFinancialDateUpdatedBy(entity.getFinancialDateUpdatedBy());
        dto.setFinancialDateUpdatedDate(entity.getFinancialDateUpdatedDate());
        dto.setTransDateUpdatedBy(entity.getTransDateUpdatedBy());
        dto.setTransDateUpdatedDate(entity.getTransDateUpdatedDate());
        dto.setReportDateUpdatedBy(entity.getReportDateUpdatedBy());
        dto.setReportDateUpdatedDate(entity.getReportDateUpdatedDate());
        dto.setInventoryDateUpdatedBy(entity.getInventoryDateUpdatedBy());
        dto.setInventoryDateUpdatedDate(entity.getInventoryDateUpdatedDate());
        dto.setCountryCode(entity.getCountryCode());
        dto.setStateName(entity.getStateName());
        dto.setLogoImage(entity.getLogoImage());
        dto.setLogoImageBase64(entity.getLogoImageBase64());
        dto.setDateFormat(entity.getDateFormat());
        dto.setTimezoneId(entity.getTimezoneId());
        dto.setCurrency(entity.getCurrencyPoid() == null ? null : currencyService.getCurrencyDetailsByPoid(entity.getCurrencyPoid()));
        dto.setStateId(entity.getStateId());
        dto.setCompanyColor(entity.getCompanyColor());
        dto.setSubmissionPeriod(entity.getSubmissionPeriod());

        if (entity.getBankPoid() != null) {
            dto.setBankDet(lovDataService.getDetailsByPoidAndLovNameFast(entity.getBankPoid(), "BANK_MASTER"));
        }

        if (entity.getDivisions() != null) {
            List<CompanyDivisionDto> divisionDtos = entity.getDivisions().stream()
                    .map(this::mapDivision)
                    .collect(Collectors.toList());
            dto.setDivisions(divisionDtos);
        }
        return dto;
    }

    private CompanyDivisionDto mapDivision(CompanyDivisionEntity entity) {
        if (entity == null) {
            return null;
        }
        CompanyDivisionDto dto = new CompanyDivisionDto();

        dto.setDetRowId(entity.getId().getDetRowId());
        dto.setCompanyPoid(entity.getId().getCompanyPoid());
        dto.setDivPoid(entity.getDivPoid());
        dto.setRemarks(entity.getRemarks());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setLastModifiedBy(entity.getLastModifiedBy());
        dto.setLastModifiedDate(entity.getLastModifiedDate());
        dto.setLogoImageBase64(entity.getLogoImageBase64());
        dto.setCompanyDivAddress(entity.getCompanyDivAddress());
        dto.setDivisionName(entity.getDivisionName());
        dto.setCompanyDivAddressPos(entity.getCompanyDivAddressPos());
        dto.setActionType(entity.getActionType());

        return dto;
    }

}
