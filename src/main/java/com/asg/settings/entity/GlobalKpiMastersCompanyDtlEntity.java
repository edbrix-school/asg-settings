package com.asg.settings.entity;


import com.asg.settings.entity.key.GlobalKpiMastersDtlId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "GLOBAL_KPI_MASTERS_COMPANY_DTL")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalKpiMastersCompanyDtlEntity {

    @EmbeddedId
    private GlobalKpiMastersDtlId id;

    @Column(name = "COMPANY_POID")
    private Long companyPoid;

    @Column(name = "TARGET_VALUE")
    private Long targetValue;

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

}
