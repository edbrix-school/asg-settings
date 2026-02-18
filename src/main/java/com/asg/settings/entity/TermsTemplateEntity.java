package com.asg.settings.entity;

import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

@Data
@Entity
@Table(name="GLOBAL_TERMS_TEMPLATE_MASTER")
public class TermsTemplateEntity extends BaseEntity {

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

    @Column(name="TERMS_CATEGORY")
    private String termsCategory;
}
