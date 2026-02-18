package com.asg.settings.entity;

import com.asg.common.lib.entity.BaseEntity;
import com.asg.settings.entity.key.TermsTemplateDtlKey;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name="GLOBAL_TERMS_TEMPLATE_DTL")
public class TermsTemplateDtlEntity extends BaseEntity {

    @EmbeddedId
    private TermsTemplateDtlKey id;

    @Column(name = "CLAUSE_NO")
    private String clauseNo;

    @Column(name = "CLAUSE_DETAILS")
    private String clauseDetails;

    @Column(name = "ACTIVE")
    private String active;

}
