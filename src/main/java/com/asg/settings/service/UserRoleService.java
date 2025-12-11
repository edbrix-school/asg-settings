package com.asg.settings.service;

import com.asg.common.lib.dto.FilterRequestDto;

import com.asg.settings.dto.UserRoleRequestDto;
import com.asg.settings.dto.UserRolesDto;
import com.asg.settings.entity.RoleEntity;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface UserRoleService {

    UserRolesDto addUserRoles(UserRoleRequestDto userRoleRequestDto);
    RoleEntity getUserRoleByRolePoid(Long userRolePoid);
    UserRolesDto updateUserRoleByUserRolePoId(Long userRolePoid, UserRoleRequestDto userRoleRequestDto);
    Map<String, Object> listRoles(String docId, FilterRequestDto request, Pageable pageable);
    void softDeleteUserRole(Long userRolePoid);
    boolean existsByRoleId(String roleId, Long excludePoid);
    boolean existsByRoleName(String roleName, Long excludePoid);

}
