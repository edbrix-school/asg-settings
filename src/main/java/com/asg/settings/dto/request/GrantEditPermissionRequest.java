package com.asg.settings.dto.request;

import lombok.Data;

@Data
public class GrantEditPermissionRequest {
    private Long docKeyPoid;
    private String reason;
    private String approvalStatus;
}
