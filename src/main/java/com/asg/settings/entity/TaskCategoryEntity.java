package com.asg.settings.entity;

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
public class TaskCategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CATEGORY_POID", nullable = false)
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
    private String deleted = "N";

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

}