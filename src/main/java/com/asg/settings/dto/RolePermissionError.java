package com.asg.settings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RolePermissionError {
    private String docId;
    private String error;
}
