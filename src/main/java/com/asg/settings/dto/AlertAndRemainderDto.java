package com.asg.settings.dto;

import com.asg.common.lib.dto.LovGetListDto;
import com.asg.common.lib.enums.AlertCheckTypeEnum;
import com.asg.common.lib.enums.FrequencyTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class AlertAndRemainderDto {

    private Long configPoid;
    @NotBlank(message = "Alert name is mandatory")
    @Size(max = 100, message = "Alert name must not exceed 100 characters")
    private String alertName;

    @NotBlank(message = "SQL query is mandatory")
    @Size(max = 4000, message = "SQL query must not exceed 4000 characters")
    private String sqlQuery;
    @Size(max = 50, message = "Expiry date field must not exceed 50 characters")
    private String expiryDateField;
    private Integer notifyDays;
    @NotEmpty
    private List<String> notifyUserRolesPoid;
    private List<LovGetListDto> notifyUserRolesPoidDet;

    @Size(max = 1, message = "Active must not exceed 1 characters")
    private String active;
    private Integer seqNo;
    @Size(max = 20, message = "Created by must not exceed 20 characters")
    private String createdBy;
    private LocalDateTime createdDate;
    @Size(max = 20, message = "Last modified by must not exceed 20 characters")
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;

    private AlertCheckTypeEnum alertCheckType;
    private Integer escalateDays;
    private List<String> escalationUserRolesPoid;
    private List<LovGetListDto> escalationUserRolesPoidDet;
    @Size(max = 1, message = "Deleted must not exceed 1 characters")
    private String deleted;
    private Integer alertEscalateFrequency;
    private Integer alertNotifyFrequency;
    private LocalDate escalateAlertSendMailDate;
    private LocalDate notifyAlertSendMailDate;
    private Integer dailyRecurrence;
    private FrequencyTypeEnum frequencyType;
}