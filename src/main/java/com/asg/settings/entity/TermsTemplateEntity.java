package com.asg.settings.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name="GLOBAL_TERMS_TEMPLATE_MASTER")
public class TermsTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="TERMS_POID")
    private Long termsPoid;

    @Column(name="GROUP_POID")
    private Long groupPoid;

    @Column(name = "TEMPLATE_ID", insertable = false, updatable = false)
    @Generated(GenerationTime.ALWAYS)
    private String templateId;

    @Column(name="DOC_ID")
    private String docId;

    @Column(name="TEMPLATE_NAME")
    private String templateName;

    @Column(name="ACTIVE")
    private String active;

    @Column(name="SEQNO")
    private Long seqNo;

    @Column(name="DELETED")
    private String deleted;

    @Column(name="REMARKS")
    private String remarks;

    @Column(name="CREATED_BY")
    private String createdBy;

    @Column(name="CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name="LASTMODIFIED_BY")
    private String lastModifiedBy;

    @Column(name="LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @Column(name="TERMS_CATEGORY")
    private String termsCategory;
}
