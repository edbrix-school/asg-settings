package com.asg.settings.dto;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class DocMasterApprovalDtlDto {

    private BigDecimal approvalLevel;
    private Map<String,String> userRolePoid;
    private Map<String,String> alternateUserRolePoid;

}
