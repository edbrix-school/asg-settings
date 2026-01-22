package com.asg.settings.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "GLOBAL_ADDRESS_MASTER")
@Getter
@Setter
public class AddressMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_master_seq")
    @SequenceGenerator(
            name = "address_master_seq",
            sequenceName = "GLOBAL_ADDRESS_MASTER_SEQ",
            allocationSize = 1
    )
    @Column(name = "ADDRESS_MASTER_POID")
    @AuditIgnore
    private Long addressMasterPoid;

    @Column(name = "GROUP_POID", nullable = false)
    @AuditIgnore
    private Long groupPoid;

    @Column(name = "ADDRESS_NAME", length = 100)
    private String addressName;

    @Column(name = "ADDRESS_NAME2", length = 100)
    private String addressName2;

    @Column(name = "PREFERRED_COMMUNICATION", length = 20)
    private String preferredCommunication; // EMAIL/FAX/BOTH

    @Column(name = "PARTY_TYPE", length = 100)
    private String partyType; // WCA / SHARAF / OTHER_NETWORKS

    // Newly added SRS fields
    @Column(name = "WHATSAPP_NO", length = 30)
    private String whatsappNo;

    @Column(name = "LINKEDIN", length = 250)
    private String linkedIn;

    @Column(name = "INSTAGRAM", length = 250)
    private String instagram;

    @Column(name = "FACEBOOK", length = 250)
    private String facebook;

    @Column(name = "REMARKS", length = 250)
    private String remarks;

    @Column(name = "COUNTRY_POID")
    private Long countryPoid;

    @Column(name = "CR_NUMBER", length = 100)
    private String crNumber;

    @Column(name = "ACTIVE", length = 1)
    private String active;

    @Column(name = "SEQNO")
    private Long seqno;

    @Column(name = "CREATED_BY", length = 20)
    @AuditIgnore
    private String createdBy;

    @Column(name = "CREATED_DATE")
    @AuditIgnore
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    @AuditIgnore
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    @AuditIgnore
    private LocalDateTime lastModifiedDate;

    @Column(name = "OLD_ACCTNO", length = 20)
    private String oldAcctNo;

    @Column(name = "DELETED", length = 1)
    @AuditIgnore
    private String deleted;

    @Column(name = "IS_FORWARDER", length = 1)
    private String isForwarder; // "Y" / "N"


    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "ADDRESS_MASTER_POID", referencedColumnName = "ADDRESS_MASTER_POID")
    private List<AddressDetails> details;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "COUNTRY_POID",              // column in GLOBAL_ADDRESS_MASTER (FK)
            referencedColumnName = "COUNTRY_POID", // PK column in GLOBAL_COUNTRY_MASTER
            insertable = false,
            updatable = false
    )
    private Country country;

}
