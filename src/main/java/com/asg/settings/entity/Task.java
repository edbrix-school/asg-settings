package com.asg.settings.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.sql.Date;
import java.time.LocalDateTime;

@Entity
@Table(name = "GLOBAL_TASK_HDR")
@Data
public class Task {

    @Id
    @Column(name = "TRANSACTION_POID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionPoid; // Primary Key

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "TRANSACTION_DATE")
    private Date transactionDate;

    @Column(name = "COMPANY_POID")
    private Long companyPoid;

    @Transient  //doesnt map to field in db
    @Schema(hidden = true)  //hides from swagger
    private String label;

    @Transient  //doesnt map to field in db
    @Schema(hidden = true)  //hides from swagger
    private Long value;

    @Column(name = "DOC_REF", length = 25)
    private String docRef;

    @NotBlank(message = "Task Description is required")
    @Column(name = "TASK_DESCRIPTION", length = 500)
    private String taskDescription;

    @NotBlank(message = "Task Category is required")
    @Column(name = "TASK_CATEGORY", length = 50)
    private String taskCategory;

    @NotBlank(message = "Task Sub Category is required")
    @Column(name = "TASK_SUB_CATEGORY", length = 50)
    private String taskSubCategory;

    @NotBlank(message = "Task Priority is required")
    @Pattern(
            regexp = "LOW|MEDIUM|NORMAL|HIGH",
            message = "Task Priority must be LOW, MEDIUM, NORMAL, or HIGH"
    )
    @Column(name = "TASK_PRIORITY", length = 50)
    private String taskPriority;

    @NotNull(message = "Task User Poid is required")
    @Column(name = "TASK_USER_POID")
    private Long taskUserPoid;

    @NotBlank(message = "Task Status is required")
    @Pattern(
            regexp = "PENDING|IN_PROGRESS|COMPLETED|CANCELLED|CLOSED",
            message = "Invalid Task Status"
    )
    @Column(name = "TASK_STATUS", length = 50)
    private String taskStatus;

    @Column(name = "PROGRESS_PERCENT")
    private Long progressPercent;

    @Column(name = "EST_HOURS")
    private Long estHours;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "DUE_DATE")
    private Date dueDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "DATE_CLOSED")
    private Date dateClosed;

    @Column(name = "DURATION_HRS")
    private Long durationHrs;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "START_DATE")
    private Date startDate;

    @Column(name = "ACTION_DETAILS", length = 1000)
    private String actionDetails;

    @Column(name = "ACTIONED_BY")
    private Long actionedBy;

    @Column(name = "RECURRING_DAYS")
    private Long recurringDays;

    @Column(name = "REF_DOC_ID", length = 50)
    private String refDocId;

    @Column(name = "REF_DOC_REF", length = 50)
    private String refDocRef;

    @Column(name = "REF_DOC_POID")
    private Long refDocPoid;

    @Column(name = "HOLD_REASON", length = 200)
    private String holdReason;

    @Column(name = "AUTO_TASK", length = 1)
    private String autoTask;

    @Column(name = "DELETED", length = 1)
    private String deleted;

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @JsonFormat(pattern = "dd-MMM-yyyy HH:mm:ss")
    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @JsonFormat(pattern = "dd-MMM-yyyy HH:mm:ss")
    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @Column(name = "FA_POID")
    private Long faPoid;

    @NotNull(message = "Task Reported By is required")
    @Column(name = "TASK_REPORTED_BY")
    private Long taskReportedBy;

    @Column(name = "RECURRING_SERIES_POID")
    private Long recurringSeriesPoid;

    @NotBlank(message = "Task Type is required")
    @Pattern(
            regexp = "New Requirement|Minor Enhancement|Bug Fix|Training|General Task|Testing|Initial Setup|Installation|Upgrade|Support Call|Meeting|Documentation",
            message = "Task Type is not supported"
    )
    @Column(name = "TASK_TYPE", length = 100)
    private String taskType;
}
