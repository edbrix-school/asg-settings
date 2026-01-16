package com.asg.settings.service.impl;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.exception.ResourceAlreadyExistsException;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.common.lib.dto.CountryDto;
import com.asg.settings.entity.Country;
import com.asg.settings.repository.CountryRepository;
import com.asg.common.lib.repository.GroupRepository;
import com.asg.settings.service.CountryService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class CountryServiceImpl implements CountryService {

    private final LoggingService loggingService;
    private final CountryRepository countryRepository;
    private final GroupRepository groupRepository;
    private final DocumentSearchService documentService;
    private final DocumentDeleteService documentDeleteService;

    @Override
    public CountryDto getCountryById(Long countryPoid) {

        if (!countryRepository.existsByCountryPoid(countryPoid)) {
            throw new ResourceNotFoundException("Country", "countryPoid", countryPoid);
        }
        Country country = countryRepository.findByCountryPoid(countryPoid);
        CountryDto countryDto = new CountryDto();
        BeanUtils.copyProperties(country, countryDto);

        // Set audit fields
        countryDto.setCreatedBy(country.getCreatedBy());
        countryDto.setCreatedDate(country.getCreatedDate() != null ? country.getCreatedDate().atOffset(java.time.ZoneOffset.UTC) : null);
        countryDto.setLastModifiedBy(country.getLastModifiedBy());
        countryDto.setLastModifiedDate(country.getLastModifiedDate() != null ? country.getLastModifiedDate().atOffset(java.time.ZoneOffset.UTC) : null);

        return countryDto;
    }

    @Override
    public CountryDto createCountry(CountryDto countryDto) {

        if (countryRepository.existsByCountryCode(countryDto.getCountryCode())) {
            throw new ResourceAlreadyExistsException("Country Code", countryDto.getCountryCode());
        }

        if (countryRepository.existsByCountryName(countryDto.getCountryName())) {
            throw new ResourceAlreadyExistsException("Country Name", countryDto.getCountryName());
        }

        groupRepository.findById(countryDto.getGroupPoid()).orElseThrow(() -> new ResourceNotFoundException("Group", "groupPoid", countryDto.getGroupPoid()));

        Country country = new Country();
        country.setCountryCode(countryDto.getCountryCode());
        country.setGroupPoid(countryDto.getGroupPoid());
        country.setCountryName(countryDto.getCountryName());
        country.setCountryName2(countryDto.getCountryName2());
        country.setGroupPoid(countryDto.getGroupPoid());

        country.setActive(countryDto.getActive() != null ? countryDto.getActive() : "Y");
        country.setSeqNo(countryDto.getSeqNo());
        country.setCreatedBy(getCurrentUser());
        country.setCreatedDate(LocalDateTime.now());
        country.setDeleted("N");
        country.setCountryTicketRate(countryDto.getCountryTicketRate());

        Country savedCountry = countryRepository.save(country);
        String docId = UserContext.getDocumentId();
        String key = savedCountry.getCountryPoid().toString();

        loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, docId, key);
        return this.convertToDto(savedCountry);
    }

    private CountryDto convertToDto(Country savedCountry) {
        CountryDto responseDto = new CountryDto();
        responseDto.setCountryPoid(savedCountry.getCountryPoid());
        responseDto.setCountryCode(savedCountry.getCountryCode());
        responseDto.setCountryName(savedCountry.getCountryName());
        responseDto.setCountryName2(savedCountry.getCountryName2());
        responseDto.setGroupPoid(savedCountry.getGroupPoid());

        responseDto.setActive(savedCountry.getActive());
        responseDto.setSeqNo(savedCountry.getSeqNo());
        responseDto.setCountryTicketRate(savedCountry.getCountryTicketRate());

        // Set audit fields
        responseDto.setCreatedBy(savedCountry.getCreatedBy());
        responseDto.setCreatedDate(savedCountry.getCreatedDate() != null ? savedCountry.getCreatedDate().atOffset(java.time.ZoneOffset.UTC) : null);
        responseDto.setLastModifiedBy(savedCountry.getLastModifiedBy());
        responseDto.setLastModifiedDate(savedCountry.getLastModifiedDate() != null ? savedCountry.getLastModifiedDate().atOffset(java.time.ZoneOffset.UTC) : null);

        return responseDto;
    }

    @Override
    public Map<String, Object> listCountries(String docId, FilterRequestDto request, Pageable pageable) {
        String operator = documentService.resolveOperator(request);
        String isDeleted = documentService.resolveIsDeleted(request);
        List<FilterDto> filters = documentService.resolveFilters(request);

        RawSearchResult raw = documentService.search(docId, filters, operator, pageable, isDeleted,
                "COUNTRY_NAME",   // label
                "COUNTRY_POID");  // value

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    @Override
    @Transactional
    public CountryDto updateCountry(Long countryPoid, CountryDto countryDto) {

        Country existingCountry = countryRepository.findById(countryPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Country", "countryPoid", countryPoid));
        // Make a copy of old country for logging
        Country oldCountry = new Country();
        BeanUtils.copyProperties(existingCountry, oldCountry);


        if (countryRepository.existsByCountryCodeIgnoreCaseAndCountryPoidNot(countryDto.getCountryCode(), countryDto.getCountryPoid())) {
            throw new ResourceAlreadyExistsException("Country Code already exists, please enter unique code.", countryDto.getCountryCode());
        }
        if (countryRepository.existsByCountryNameIgnoreCaseAndCountryPoidNot(countryDto.getCountryName(), countryDto.getCountryPoid())) {
            throw new ResourceAlreadyExistsException("Country Name already exists, please enter unique name.", countryDto.getCountryName());
        }

        groupRepository.findById(countryDto.getGroupPoid()).orElseThrow(() -> new ResourceNotFoundException("Group", "groupPoid", countryDto.getGroupPoid()));
        if (countryDto.getCountryName() != null) {
            existingCountry.setCountryName(countryDto.getCountryName());
        }
        if (countryDto.getCountryName2() != null) {
            existingCountry.setCountryName2(countryDto.getCountryName2());
        }
        if (countryDto.getCountryTicketRate() != null) {
            existingCountry.setCountryTicketRate(countryDto.getCountryTicketRate());
        }

        if (countryDto.getActive() != null) {

            existingCountry.setActive(countryDto.getActive());
        }
        if (countryDto.getGroupPoid() != null) {
            existingCountry.setGroupPoid(countryDto.getGroupPoid());
        }

        if (countryDto.getSeqNo() != null) {
            existingCountry.setSeqNo(countryDto.getSeqNo());
        }
        existingCountry.setLastModifiedDate(LocalDateTime.now());

        Country updatedCountry = countryRepository.save(existingCountry);
        String docId = UserContext.getDocumentId();
        String key = updatedCountry.getCountryPoid().toString();

        loggingService.logChanges(oldCountry, updatedCountry, Country.class, docId, key, LogDetailsEnum.MODIFIED, "COUNTRY_POID");

        CountryDto responseDto = new CountryDto();
        BeanUtils.copyProperties(updatedCountry, responseDto);

        responseDto.setActive(updatedCountry.getActive());

        // Set audit fields
        responseDto.setCreatedBy(updatedCountry.getCreatedBy());
        responseDto.setCreatedDate(updatedCountry.getCreatedDate() != null ? updatedCountry.getCreatedDate().atOffset(java.time.ZoneOffset.UTC) : null);
        responseDto.setLastModifiedBy(updatedCountry.getLastModifiedBy());
        responseDto.setLastModifiedDate(updatedCountry.getLastModifiedDate() != null ? updatedCountry.getLastModifiedDate().atOffset(java.time.ZoneOffset.UTC) : null);

        return responseDto;
    }

    @Override
    @Transactional
    public void softDeleteCountry(Long countryPoid, DeleteReasonDto deleteReasonDto) {
        Country existingCountry = countryRepository.findById(countryPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Country", "countryPoid", countryPoid));
        
        documentDeleteService.deleteDocument(
                countryPoid,
                "GLOBAL_COUNTRY_MASTER",
                "COUNTRY_POID",
                deleteReasonDto,
                null
        );
    }
    private String getCurrentUser() {
        return UserContext.getUserId() != null ? String.valueOf(UserContext.getUserId()) : "SYSTEM";
    }

}
