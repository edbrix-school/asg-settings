package com.asg.settings.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "GLOBAL_DIVISION_MASTER")
@Data
public class DivisionMasterEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DIVISION_POID")
    @AuditIgnore
    private Long divisionId;

    @Column(name = "DIVISION_CODE", unique = true, nullable = false)
    @AuditIgnore
    private String divisionCode;

    @Column(name = "DIVISION_NAME", nullable = false)
    private String divisionName;

    // Using REMARKS column for description field (as per DB)
    @Column(name = "REMARKS")
    private String description;

    @Column(name = "ACTIVE")
    private String active;

    @Column(name = "DELETED", length = 1)
    @AuditIgnore
    private String deleted;

    @Column(name = "SEQNO")
    private Integer seqNo;

}
