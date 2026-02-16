package com.asg.settings.entity;


import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "GLOBAL_USER_ROLES")
public class RoleEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_role_seq")
    @SequenceGenerator(name = "user_role_seq", sequenceName = "GLOBAL_USER_ROLES_SEQ", allocationSize = 1)
    @Column(name = "USER_ROLE_POID")
    private Long userRolePoid;

    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @Column(name = "USER_ROLE_ID")
    private String userRoleId;

    @Column(name = "USER_ROLE_NAME")
    private String userRoleName;

    @Column(name = "USER_ROLE_NAME2")
    private String userRoleName2;

    @Column(name = "ACTIVE")
    private String active;

    @Column(name = "SEQNO")
    private Integer seqNo;

    @Column(name = "COMPANY_POID")
    private Long companyPoid;

    @Column(name = "DELETED")
    private String deleted;
}
