package com.asg.settings.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "GLOBAL_COUNTRY_MASTER")
public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COUNTRY_POID")
    @AuditIgnore
    private Long countryPoid;

    @Column(name = "GROUP_POID")
    @AuditIgnore
    private Long groupPoid;

    @Column(name = "COUNTRY_CODE")
    @AuditIgnore
    private String countryCode;

    @Column(name = "COUNTRY_NAME")
    private String countryName;

    @Column(name = "COUNTRY_NAME2")
    private String countryName2;

    @Column(name = "REGION_POID")
    @AuditIgnore
    private Long regionPoid;

    @Column(name = "ACTIVE")
    private String active;

    @Column(name = "SEQNO")
    private Integer seqNo;

    @Column(name = "CREATED_BY")
    @AuditIgnore
    private String createdBy;

    @Column(name = "CREATED_DATE")
    @AuditIgnore
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY")
    @AuditIgnore
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    @AuditIgnore
    private LocalDateTime lastModifiedDate;

    @Column(name = "DELETED")
    @AuditIgnore
    private String deleted;

    @Column(name = "MANIFEST_COUNTRY_REMARK")
    @AuditIgnore
    private String manifestCountryRemark;

//    @Column(name = "OFAC_BAN")
//    private Boolean ofacBan;
//
//    @Column(name = "TRADE_BAN")
//    private Boolean tradeBan;

    @Column(name = "COUNTRY_TICKET_RATE")
    private Double countryTicketRate;
}
