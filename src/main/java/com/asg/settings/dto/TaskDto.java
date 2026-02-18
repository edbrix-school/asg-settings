package com.asg.settings.dto;

import com.asg.common.lib.dto.LovGetListDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class TaskDto {

    private Long transactionPoid;
    private LocalDate transactionDate;
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

    private LocalDate dueDate;

    private LocalDate dateClosed;

    private Long durationHrs;

    private LocalDate startDate;

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
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
    private Long faPoid;
    private Long taskReportedBy;
    private LovGetListDto taskReportedByDet;
    private Long recurringSeriesPoid;
    private String taskType;
}
