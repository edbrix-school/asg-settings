package com.asg.settings.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "GLOBAL_ADDRESS_DETAILS")
@Data
public class AddressDetails extends BaseEntity {

    @Id
    @Column(name = "ADDRESS_POID")
    @AuditIgnore
    private String addressPoid;// use String, not Long because Oracle stores them like 53394.1, 53394.2 (decimal). Long cannot hold the decimal part. Used String to preserve uniqueness.

    @Column(name = "ADDRESS_MASTER_POID", nullable = false)
    @AuditIgnore
    private Long addressMasterPoid;

    @Column(name = "ADDRESS_TYPE", length = 20)
    private String addressType;

    @Column(name = "OFF_TEL1", length = 30)
    private String offTel1;

    @Column(name = "OFF_TEL2", length = 30)
    private String offTel2;

    @Column(name = "CONTACT_PERSON", length = 50)
    private String contactPerson;

    @Column(name = "DESIGNATION", length = 50)
    private String designation;

    @Column(name = "MOBILE", length = 30)
    private String mobile;

    @Column(name = "FAX", length = 30)
    private String fax;

    @Column(name = "EMAIL1", length = 110)
    private String email;

    @Column(name = "EMAIL2", length = 110)
    private String email2;

    @Column(name = "WEBSITE", length = 100)
    private String website;

    @Column(name = "PO_BOX", length = 30)
    private String poBox;

    @Column(name = "OFF_NO", length = 100)
    private String offNo;

    @Column(name = "BLDG", length = 100)
    private String bldg;

    @Column(name = "ROAD", length = 100)
    private String road;

    //  FIX: Replace AREA + CITY with AREA_CITY
    @Column(name = "AREA_CITY", length = 100)
    private String areaCity;

    @Column(name = "CITY", length = 100)
    private String city;

    @Column(name = "STATE", length = 100)
    private String state;

    @Column(name = "COUNTRY_POID")
    private Long countryPoid;

    @Column(name = "LAND_MARK", length = 250)
    private String landMark;

    @Column(name = "OLD_ACCNO_REF", length = 20)
    @AuditIgnore
    private String oldAccnoRef;

    @Column(name = "OLD_GL_ACCTNO", length = 20)
    @AuditIgnore
    private String oldGlAcctno;

    @Column(name = "OLD_GL_ACCTNO_SUPPLIER", length = 20)
    @AuditIgnore
    private String oldGlAcctnoSupplier;

    @Column(name = "VERIFIED", length = 1)
    @AuditIgnore
    private String verified;

    @Column(name = "VERIFIED_BY", length = 100)
    @AuditIgnore
    private String verifiedBy;

    @Column(name = "VERIFIED_DATE")
    private LocalDate verifiedDate;

    @Column(name = "WHATSAPP_NO", length = 50)
    private String whatsappNo;

    @Column(name = "LINKEDIN", length = 100)
    private String linkedin;

    @Column(name = "INSTAGRAM", length = 100)
    private String instagram;

    @Column(name = "FACEBOOK", length = 100)
    private String facebook;

}
