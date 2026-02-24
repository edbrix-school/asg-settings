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

    @Column(name = "TEMPLATE_NAME", length = 200)
    private String templateName;

    @Column(name = "TEMPLATE_TYPE", length = 20)
    private String type;

    @Column(name = "TEMPLATE_DOC_ID", length = 50)
    private String templateDocId;

    @Column(name = "FIELDS_TO_USE", length = 4000)
    private String fieldsToUse;

    @Column(name = "SQL_QUERY", length = 8000)
    private String sqlQuery;

    @Column(name = "EMAIL_SUBJECT", length = 1000)
    private String emailSubject;

    @Column(name = "EMAIL_CONTENT", columnDefinition = "CLOB")
    private String emailContent;

    @Column(name = "PDF_CONTENT", columnDefinition = "CLOB")
    private String pdfContent;

    @Column(name = "ACTIVE", length = 1)
    private String active;

    @Column(name = "SEQNO")
    private Integer seqNo;

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private java.sql.Timestamp createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private java.sql.Timestamp lastModifiedDate;

    @Column(name = "DELETED", length = 1)
    private String deleted;

    @Column(name = "REMARKS", length = 1000)
    private String remarks;
}
