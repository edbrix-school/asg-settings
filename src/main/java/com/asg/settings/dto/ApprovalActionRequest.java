package com.asg.settings.dto;

import lombok.Data;

@Data
public class ApprovalActionRequest {
    private Long docKeyPoid;
    private String action;   // enum values as string: SUBMIT_FOR_APPROVAL, APPROVE, etc.
    private String comments; // optional
}
