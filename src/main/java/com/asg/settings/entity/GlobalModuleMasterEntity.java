package com.asg.settings.entity;

import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "GLOBAL_MODULE_MASTER")
public class GlobalModuleMasterEntity extends BaseEntity {

    @Id
    @Column(name = "MODULE_ID", length = 20, nullable = false)
    private String moduleId;

    @Column(name = "MODULE_NAME", length = 50)
    private String moduleName;

    @Column(name = "MODULE_NAME2", length = 50)
    private String moduleName2;

    @Column(name = "GROUP_CODE", length = 20, nullable = false)
    private String groupCode;

    @Column(name = "MODULE_SHORT_NAME", length = 20)
    private String moduleShortName;

    @Column(name = "ACTIVE", length = 1)
    private String active;

    @Column(name = "SEQNO", precision = 5)
    private Integer seqNo;

    @Column(name = "MODULE_POID", nullable = false)
    private Long modulePoid;

    @Column(name = "DELETED", length = 1)
    private String deleted;

    @Column(name = "DIVISION", length = 1)
    private String division;
}

