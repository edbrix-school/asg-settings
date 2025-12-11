package com.asg.settings.dto;

import com.asg.settings.entity.UserEntity;
import lombok.Data;

import java.util.List;

@Data
public class UserResponse {
    private List<UserEntity> users;
    private Integer totalRecords;
    private Long userRolePoid;
    private String message;
    private String status;
}
