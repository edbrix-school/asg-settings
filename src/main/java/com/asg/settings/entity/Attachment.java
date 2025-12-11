package com.asg.settings.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "GLOBAL_ATTACHMENTS")
@Data
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SEQNO")   // Primary key for this table
    private Long seqNo;

    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @Column(name = "COMPANY_POID")
    private Long companyPoid;

    @Column(name = "DOC_ID")
    private String docId;  // e.g., "700-004"

    @Column(name = "DOC_KEY_POID")
    private Long docKeyPoid;

    @Column(name = "FILE_NAME")
    private String fileName; // original file name (e.g., "MTA CR.pdf")

    @Column(name = "FILE_REMARKS")
    private String fileRemarks;

    @Column(name = "CHECKLIST_NAME")
    private String checklistName;

    @Column(name = "CREATED_BY")
    private String createdBy; // if it's numeric user id, you may keep Long

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "FILE_NAME_MAPPED")
    private String fileNameMapped; // dynamic name stored in FS

    @Column(name = "EDI_JOB_POID")
    private Long ediJobPoid;

    @Column(name = "ACTIVE")
    private String active; // "Y" / "N" or NULL

    @Column(name = "DELETED")
    private String deleted; // "Y" / "N" or NULL
}
