package com.asg.settings.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class UserAuthRoleDto implements Serializable {

    private Long userPoid;
    private Long detRowId;
    private LocalDate expiryDate;
    private Long userRolePoId;
    private String roleId;
    private String roleName;
    private String deleted;
    private String active;
    private String actionType;
}
