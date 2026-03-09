package com.asg.settings.dto.request;

import lombok.Data;

@Data
public class DocMasterApprovalDtlRequestDto {
    private Long detRowId;
    private Long approvalLevel;
    private Long userRolePoid;
    private Long alternateUserRolePoid;
    private String actionType; // isCreated, isUpdated, isDeleted, noChange
}
