package com.asg.settings.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "GLOBAL_TASK_CATEGORY")
public class TaskCategoryEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CATEGORY_POID", nullable = false)
    @AuditIgnore
    private Long categoryPoid;

    @Column(name = "CATEGORY_CODE", length = 30)
    private String categoryCode;

    @Column(name = "CATEGORY_DESCRIPTION", length = 100, nullable = false)
    private String categoryDescription;

    @Column(name = "USER_ROLE_POID", length = 100)
    private String userRolePoid;

    @Column(name = "ACTIVE", length = 1)
    private String active = "Y";

    @Column(name = "SEQNO", precision = 5, scale = 0)
    private Integer seqNo;

    @Column(name = "DELETED", length = 1)
    @AuditIgnore
    private String deleted = "N";

}