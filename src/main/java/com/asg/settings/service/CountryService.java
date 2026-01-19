package com.asg.settings.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.CountryDto;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface CountryService {
    CountryDto getCountryById(Long countryPoid);
    CountryDto createCountry(CountryDto countryDto);
    CountryDto updateCountry(Long countryPoid, CountryDto countryDto);
    Map<String, Object> listCountries(String docId, FilterRequestDto request, Pageable pageable);
    void softDeleteCountry(Long countryPoid, DeleteReasonDto deleteReasonDto);
}



