package com.asg.settings.dto;

import com.asg.common.lib.dto.LovGetListDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
public class TaskDto {

    private Long transactionPoid;
    private Date transactionDate;
    private Long companyPoid;

    private String label;
    private Long value;

    private String docRef;
    private String taskDescription;
    private String taskCategory;
    private LovGetListDto taskCategoryDet;
    private String taskSubCategory;
    private LovGetListDto taskSubCategoryDet;
    private String taskPriority;
    private Long taskUserPoid;
    private LovGetListDto taskUserDet;
    private String taskStatus;
    private Long progressPercent;
    private Long estHours;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dueDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateClosed;

    private Long durationHrs;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    private String actionDetails;
    private Long actionedBy;
    private Long recurringDays;
    private String refDocId;
    private String refDocRef;
    private Long refDocPoid;
    private String holdReason;
    private String autoTask;
    private String deleted;
    private String createdBy;

    @JsonFormat(pattern = "dd-MMM-yyyy HH:mm:ss")
    private Date createdDate;

    private String lastModifiedBy;

    @JsonFormat(pattern = "dd-MMM-yyyy HH:mm:ss")
    private Date lastModifiedDate;

    private Long faPoid;
    private Long taskReportedBy;
    private LovGetListDto taskReportedByDet;
    private Long recurringSeriesPoid;
    private String taskType;
}
