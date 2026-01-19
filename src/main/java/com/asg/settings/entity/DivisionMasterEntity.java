package com.asg.settings.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Table(name = "GLOBAL_DIVISION_MASTER")
@Data
public class DivisionMasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DIVISION_POID")
    private Long divisionId;

    @Column(name = "DIVISION_CODE", unique = true, nullable = false)
    private String divisionCode;

    @Column(name = "DIVISION_NAME", nullable = false)
    private String divisionName;

    // Using REMARKS column for description field (as per DB)
    @Column(name = "REMARKS")
    private String description;

    @Column(name = "ACTIVE")
    private String active;

    @Column(name = "DELETED", length = 1)
    private String deleted;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private Timestamp createdAt;

    @Column(name = "LASTMODIFIED_BY")
    private String updatedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private Timestamp updatedAt;

    @Column(name = "SEQNO")
    private Integer seqNo;

}
