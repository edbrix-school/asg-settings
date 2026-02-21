package com.asg.settings.entity;


import com.asg.settings.entity.key.GlobalKpiMastersDtlId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "GLOBAL_KPI_MASTERS_DEPT_DTL")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalKpiMastersDeptDtlEntity {

    @EmbeddedId
    private GlobalKpiMastersDtlId id;

    @Column(name = "DEPT_POID")
    private Long deptPoid;

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

