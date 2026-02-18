package com.asg.settings.entity;

import com.asg.common.lib.entity.BaseEntity;
import com.asg.settings.entity.key.GlobalDocMasterAuthDtlId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "GLOBAL_DOC_MASTER_AUTH_DTL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocMasterAuthDtlEntity extends BaseEntity {

    @EmbeddedId
    private GlobalDocMasterAuthDtlId id;

    @Column(name = "SPECIAL_FIELD_NAME")
    private String specialFieldName;

    @Column(name = "AUTHORIZATION_REASON")
    private String authorizationReason;

    @Column(name = "VALUE_LIMIT")
    private Long valueLimit;

    @Column(name = "FIRST_USER_ROLE_POID")
    private Long firstUserRolePoid;

    @Column(name = "SECOND_USER_ROLE_POID")
    private Long secondUserRolePoid;

    @Column(name = "SQL_KEY_POID_NAME")
    private String sqlKeyPoidName;

    @Column(name = "SQL_TABLE_NAME")
    private String sqlTableName;

    @Column(name = "SQL_FIELD_NAME")
    private String sqlFieldName;

}

