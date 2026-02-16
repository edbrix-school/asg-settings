package com.asg.settings.dto;

import com.asg.common.lib.dto.DetailsDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record UserDto(String userId,
                      Long userPoid,
                      String userName,
                      String label,
                      Long value,
                      Long groupPoid,
                      DetailsDto defaultCompany,
                      LocalDateTime joinedDate,
                      // List<UserRoleDto> roles,
                      List<UserAuthRoleDto> roles,
                      List<UserCompanyDto> companies,
                      String countryCode,
                      String mobileNumber,
                      String emailId,
                      DetailsDto defaultLocation,
                      LocalDate autoInactiveDate,
                      Integer seqNo,
                      String active,
                      String authenticationMethod,
                      String locked,
                      String lockedReason,
                      String resetPasswordForNextLogin,
                      String createdBy,
                      LocalDateTime createdDate,
                      String modifiedBy,
                      LocalDateTime modifiedDate) {

    public UserDto {
        if (userId != null) {
            userId = userId.toUpperCase();
        }
    }
}

