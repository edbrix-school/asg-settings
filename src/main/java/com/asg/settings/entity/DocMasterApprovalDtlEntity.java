package com.asg.settings.entity;

import com.asg.common.lib.entity.BaseEntity;
import com.asg.settings.entity.key.GlobalDocMasterApprovalDtlId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "GLOBAL_DOC_MASTER_APPROVAL_DTL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocMasterApprovalDtlEntity extends BaseEntity {

    @EmbeddedId
    private GlobalDocMasterApprovalDtlId id;

    @Column(name = "APPROVAL_LEVEL")
    private Long approvalLevel;

    @Column(name = "USER_ROLE_POID")
    private Long userRolePoid;

    @Column(name = "ALTERNATE_USER_ROLE_POID")
    private Long alternateUserRolePoid;

}

