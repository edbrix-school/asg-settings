package com.asg.settings.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRoleRequestDto {

    private Long userRolePoid;

    @NotBlank(message = "User Role Id is required")
    @Size(max = 20)
    @Pattern(
            regexp = "^[A-Z0-9_]+$",
            message = "User Role Id must contain only uppercase letters, numbers, and underscores, no spaces or special characters"
    )
    private String userRoleId;

    @NotBlank(message = "User Role Name is required")
    @Size(min = 3, max = 100)
    private String userRoleName;

    @Size(max = 100)
    private String userRoleName2;

    @NotNull(message = "Group Poid is required")
    private Long groupPoid;

    private Integer seqNo;

    @NotNull(message = "Active is required")
    private String active;

    @NotNull(message = "Company Poid is required")
    private Long companyPoid;

    private String deleted;
}
