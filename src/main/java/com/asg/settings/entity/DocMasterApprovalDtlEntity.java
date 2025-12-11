package com.asg.settings.entity;

import com.asg.settings.entity.key.GlobalDocMasterApprovalDtlId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "GLOBAL_DOC_MASTER_APPROVAL_DTL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocMasterApprovalDtlEntity {

    @EmbeddedId
    private GlobalDocMasterApprovalDtlId id;

    @Column(name = "APPROVAL_LEVEL")
    private BigDecimal approvalLevel;

    @Column(name = "USER_ROLE_POID")
    private Long userRolePoid;

    @Column(name = "ALTERNATE_USER_ROLE_POID")
    private Long alternateUserRolePoid;

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private Timestamp createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private Timestamp lastModifiedDate;

}

