package com.asg.settings.entity;

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
public class UserRoleRightsEntity {
    @EmbeddedId
    private UserRoleRightsKey id;

    @Column(name = "DOC_ID", nullable = false)
    private String docId;

    @Column(name = "RIGHTS")
    private String rights;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY")
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;
}
