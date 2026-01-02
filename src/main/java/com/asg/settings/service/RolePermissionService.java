package com.asg.settings.service;

import com.asg.common.lib.dto.UserRoleRightsDetDto;
import com.asg.common.lib.dto.UserRoleRightsDto;
import com.asg.settings.dto.request.RightsUpdateRequest;
import com.asg.settings.dto.request.RolePermissionRequest;
import com.asg.settings.dto.response.RolePermissionResponse;

import java.util.List;

public interface RolePermissionService {
    RolePermissionResponse addPermissions(RolePermissionRequest request);

    RolePermissionResponse updatePermissions(Long roleId, RightsUpdateRequest request);

    String loadDefaultRights(Long loginUserPoid, Long userRolePoid);

    List<UserRoleRightsDto> getUserRoleRights(Long userRolePoid);

    UserRoleRightsDetDto getUserRoleRightsDetByRolePoid(Long userRolePoid);
}
