package com.asg.settings.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
@Table(name = "GLOBAL_ALERT_CONFIG")
public class AlertConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "alert_config_seq")
    @SequenceGenerator(name = "alert_config_seq", sequenceName = "GLOBAL_ALERT_CONFIG_SEQ", allocationSize = 1)
    @Column(name = "CONFIG_POID", nullable = false)
    @AuditIgnore
    private Long configPoid;

    @Column(name = "ALERT_NAME", length = 100, nullable = false)
    @Size(max = 100)
    private String alertName;

    @Column(name = "SQL_QUERY", length = 4000, nullable = false)
    @Size(max = 4000)
    private String sqlQuery;

    @Column(name = "EXPIRY_DATE_FIELD", length = 50)
    @Size(max = 50)
    private String expiryDateField;

    @Column(name = "NOTIFY_DAYS")
    @Digits(integer = 4, fraction = 0)
    private Integer notifyDays;

    @Column(name = "NOTIFY_USER_ROLES_POID", length = 200, nullable = false)
    @Size(max = 200)
    private String notifyUserRolesPoid;

    @Column(name = "ACTIVE", length = 1)
    @Size(max = 1)
    private String active;

    @Column(name = "SEQNO", precision = 5, scale = 0)
    @Digits(integer = 5, fraction = 0)
    private Integer seqNo;

    @Column(name = "CREATED_BY", length = 20)
    @AuditIgnore
    private String createdBy;

    @Column(name = "CREATED_DATE")
    @AuditIgnore
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    @AuditIgnore
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    @AuditIgnore
    private LocalDateTime lastModifiedDate;

    @Column(name = "ALERT_CHECK_TYPE", length = 20)
    @Size(max = 20)
    private String alertCheckType;

    @Column(name = "ESCALATE_DAYS")
    @Digits(integer = 4, fraction = 0)
    private Integer escalateDays;

    @Column(name = "ESCALATE_USER_ROLES_POID", length = 200)
    @Size(max = 200)
    private String escalateUserRolesPoid;

    @Column(name = "DELETED", length = 1)
    @AuditIgnore
    private String deleted;

    @Column(name = "ALERT_ESCALATE_FREQUENCY")
    @Digits(integer = 4, fraction = 0)
    private Integer alertEscalateFrequency = 1;

    @Column(name = "ALERT_NOTIFY_FREQUENCY")
    @Digits(integer = 4, fraction = 0)
    private Integer alertNotifyFrequency = 1;

    @Column(name = "ESCALATE_ALERT_SEND_MAIL_DATE")
    @AuditIgnore
    private Date escalateAlertSendMailDate;

    @Column(name = "NOTIFY_ALERT_SEND_MAIL_DATE")
    @AuditIgnore
    private Date notifyAlertSendMailDate;

    @Column(name = "DAILY_RECURRENCE")
    @Digits(integer = 1, fraction = 0)
    @AuditIgnore
    private Integer dailyRecurrence;

    @Column(name = "FREQUENCY_TYPE", length = 20)
    @Size(max = 20)
    @AuditIgnore
    private String frequencyType = "DAY";
}

