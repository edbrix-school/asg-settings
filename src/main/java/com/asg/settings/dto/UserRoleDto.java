package com.asg.settings.dto;

import java.sql.Date;

public record UserRoleDto(Long userRolePoId, String roleId, String roleName, Date expiryDate, String deleted, String active, String actionType) {}

