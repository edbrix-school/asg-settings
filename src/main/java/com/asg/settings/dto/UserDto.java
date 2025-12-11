package com.asg.settings.dto;

import java.util.Date;
import java.util.List;

public record UserDto(String userId,
                      Long userPoid,
                      String userName,
                      String label,
                      Long value,
                      Long groupPoid,
                      DetailsDto defaultCompany,
                      Date joinedDate,
                      // List<UserRoleDto> roles,
                      List<UserAuthRoleDto> roles,
                      List<UserCompanyDto> companies,
                      String countryCode,
                      String mobileNumber,
                      String emailId,
                      DetailsDto defaultLocation,
                      Date autoInactiveDate,
                      Integer seqNo,
                      String active,
                      String authenticationMethod,
                      String locked,
                      String lockedReason,
                      String resetPasswordForNextLogin,
                      String createdBy,
                      Date createdDate,
                      String modifiedBy,
                      Date modifiedDate) {

    public UserDto {
        if (userId != null) {
            userId = userId.toUpperCase();
        }
    }
}

