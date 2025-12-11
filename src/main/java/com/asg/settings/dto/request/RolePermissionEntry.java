package com.asg.settings.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RolePermissionEntry {
    @NotNull
    private Long detRowId;

    @NotBlank
    private String docId;

    @Pattern(regexp = "\\d{6}", message = "Rights must be exactly 6 digits")
    private String rights;

    @NotBlank
    private String createdBy;

    @NotBlank
    private String lastModifiedBy;
}
