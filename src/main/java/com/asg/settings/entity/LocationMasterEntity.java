package com.asg.settings.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "GLOBAL_LOCATION_MASTER")
public class LocationMasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "global_location_master_seq")
    @SequenceGenerator(
            name = "global_location_master_seq",
            sequenceName = "GLOBAL_LOCATION_MASTER_SEQ",
            allocationSize = 1
    )
    @Column(name = "LOCATION_POID", nullable = false)
    private Long locationPoid;

    @Column(name = "COMPANY_POID")
    private Long company;

    @Column(name = "LOCATION_CODE", nullable = false, length = 20)
    private String locationCode;

    @Column(name = "LOCATION_NAME", nullable = false, length = 100)
    private String locationName;

    @Column(name = "LOCATION_NAME2", length = 100)
    private String locationName2;

    @Column(name = "ACTIVE", length = 1)
    private String active;

    @Column(name = "SEQNO")
    private Integer seqNo;

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @Column(name = "DELETED", length = 1)
    private String deleted;

    @Column(name = "STAFF_SALE_DISCOUNT")
    private Double staffSaleDiscount;

    @Column(name = "STAFF_DISCOUNT")
    private Double staffDiscount;

    @Column(name = "LOYALTY_CUSTOMER_DISCOUNT")
    private Double loyaltyCustomerDiscount;

    @Column(name = "DISCOUNT_ENABLED", length = 1)
    private String discountEnabled = "Y";

    @Column(name = "SITE_SUPERVISOR_USER_POID")
    private Long siteSupervisorUserPoid;

    @Column(name = "OWN_LOCATION", length = 1)
    private String ownLocation;

    @Column(name = "INVENTORY_LOCATION", length = 1)
    private String inventoryLocation;

}
