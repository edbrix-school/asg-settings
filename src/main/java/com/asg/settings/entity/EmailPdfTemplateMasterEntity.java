package com.asg.settings.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "GLOBAL_EMAIL_PDF_TEMPLATE_MST")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailPdfTemplateMasterEntity {

    @Id
    @Column(name = "TEMPLATE_POID")
    private Long templatePoid;

    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @Column(name = "TEMPLATE_NAME")
    private String templateName;

    @Column(name = "TEMPLATE_TYPE")
    private String type;

    @Column(name = "TEMPLATE_DOC_ID")
    private String templateDocId;

    @Column(name = "FIELDS_TO_USE")
    private String fieldsToUse;

    @Column(name = "SQL_QUERY")
    private String sqlQuery;

    @Column(name = "EMAIL_SUBJECT")
    private String emailSubject;

    @Column(name = "EMAIL_CONTENT", columnDefinition = "CLOB")
    private String emailContent;

    @Column(name = "PDF_CONTENT", columnDefinition = "CLOB")
    private String pdfContent;

    @Column(name = "ACTIVE")
    private String active;

    @Column(name = "SEQNO")
    private Integer seqNo;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private java.time.LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY")
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private java.time.LocalDateTime lastModifiedDate;

    @Column(name = "DELETED")
    private String deleted;

    @Column(name = "REMARKS")
    private String remarks;
}
