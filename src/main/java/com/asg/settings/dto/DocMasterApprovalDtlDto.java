package com.asg.settings.dto;
import lombok.Data;

import java.util.Map;

@Data
public class DocMasterApprovalDtlDto {

    private Long detRowId;
    private Long approvalLevel;
    private Map<String,String> userRolePoid;
    private Map<String,String> alternateUserRolePoid;

}
