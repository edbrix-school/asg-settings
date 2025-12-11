package com.asg.settings.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RolePermissionRequest {
    @NotNull
    private Long roleId;

    @NotEmpty
    private List<RolePermissionEntry> permissions;
}
