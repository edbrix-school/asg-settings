package com.asg.settings.dto;

import java.sql.Date;

public record UserCompanyDto(
        Long companyId,
        String companyName,
        String countryCode,
        String stateName,
        Date expiryDate,
        String deleted,
        TimeZoneDto timeZone,
        String dateFormat,
        String active,
        String actionType
) {}

