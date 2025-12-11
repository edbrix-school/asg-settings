package com.asg.settings.dto.response;

import com.asg.settings.dto.RolePermissionError;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RolePermissionResponse {
    private String status;
    private String message;
    private List<RolePermissionError> errors;
}
