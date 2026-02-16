package com.asg.settings.entity;

import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "GLOBAL_USERS")
@Data
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_POID", nullable = false)
    private Long userPoid;

    @Column(name = "GROUP_POID", nullable = false)
    private Long groupPoid;

    @Column(name = "USER_ID", length = 20, nullable = false)
    private String userId;

    @Column(name = "USER_NAME", length = 100, nullable = false)
    private String userName;

    @Column(name = "USER_EMAIL", length = 50, nullable = false)
    private String email;

    @Column(name = "PWD", length = 256)
    private String pwd;

    @Column(name = "USER_LOCKED", length = 1)
    private String userLocked;

    @Column(name = "USER_MOBILE", length = 30)
    private String userMobile;

    @Column(name = "USER_LOCKED_REASON", length = 100)
    private String userLockedReason;

    @Column(name = "RESET_PWD_NEXT_LOGIN", length = 1)
    private String resetPasswordNextLogin;

    @Column(name = "ACTIVE", length = 1)
    private String active;

    @Column(name = "DELETED", length = 1)
    private String deleted;

    @Column(name = "DEFAULT_COMPANY_POID")
    private Long defaultCompanyPoid;

    @Column(name = "DEFAULT_LOCATION_POID")
    private Long defaultLocationPoid;

    @Column(name = "AUTHORIZATION_LEVEL")
    private Integer authorizationLevel;

    @Column(name = "SEQNO")
    private Integer seqno;

    @Column(name = "EXPIRY_DATE")
    private LocalDate expiryDate;

    @Column(name = "ALLOW_OFFICE365_LOGIN", length = 1)
    private String authenticationMethod = "N";
}
