package com.asg.settings.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.sql.Date;
import java.util.List;

@Data
public class CreateUserRequest {
    @NotBlank(message = "User ID is mandatory")
    @Size(max = 20, message = "userId must not exceed 20 characters")
    private String userId;
    private Long userPoid;
    @NotBlank(message = "User Name is mandatory")
    @Size(max = 100, message = "userName must not exceed 100 characters")
    private String userName;
    @Size(max = 30, message = "userMobile must not exceed 30 characters")
    private String userMobile;
    @NotBlank(message = "User Email is mandatory")
    @Email(message = "Invalid email format")
    @Size(max = 50, message = "userEmail must not exceed 50 characters")
    private String userEmail;
    private Date expiryDate;
    @Size(max = 1, message = "userLocked must not exceed 1 character")
    private String userLocked;
    @Size(max = 100, message = "userLockedReason must not exceed 100 characters")
    private String userLockedReason;
    @Size(max = 1, message = "active must not exceed 1 character")
    private String active;
    private Integer seqNo;
    @Size(max = 1, message = "resetPwdNextLogin must not exceed 1 character")
    private String resetPwdNextLogin;
    @Size(max = 256, message = "pwd must not exceed 256 characters")
    private String pwd;
    private Long defaultLocationPoid;
    private Long defaultCompanyPoid;
    @NotBlank(message = "Authentication Method is mandatory")
    @Pattern(regexp = "^[A-Za-z]$", message = "Authentication Method must be exactly one letter")
    @Size(max = 1, message = "authenticationMethod must exactly 1 character")
    private String authenticationMethod;
    private List<UserRoleDto> userRoles;
    private List<UserCompanyDto> userCompanies;
}
