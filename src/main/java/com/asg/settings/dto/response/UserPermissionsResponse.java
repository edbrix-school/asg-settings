package com.asg.settings.dto.response;

import com.asg.settings.dto.PermissionDto;

import java.util.List;

public record UserPermissionsResponse(String userId, List<PermissionDto> permissions) {}