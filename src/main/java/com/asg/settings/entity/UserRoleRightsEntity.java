package com.asg.settings.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import com.asg.settings.entity.key.UserRoleRightsKey;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "GLOBAL_USER_ROLES_RIGHTS_DTL",
        uniqueConstraints = @UniqueConstraint(columnNames = {"USER_ROLE_POID", "DET_ROW_ID", "DOC_ID"})
)
@Data
public class UserRoleRightsEntity extends BaseEntity {
    @EmbeddedId
    @AuditIgnore
    private UserRoleRightsKey id;

    @Column(name = "DOC_ID", nullable = false)
    @AuditIgnore
    private String docId;

    @Column(name = "RIGHTS")
    private String rights;

}
