package com.asg.settings.dto;

import com.asg.common.lib.dto.TimeZoneDto;

import java.sql.Date;
import java.time.LocalDate;

public record UserCompanyDto(
        Long companyId,
        String companyName,
        String countryCode,
        String stateName,
        LocalDate expiryDate,
        String deleted,
        TimeZoneDto timeZone,
        String dateFormat,
        String active,
        String actionType
) {}

