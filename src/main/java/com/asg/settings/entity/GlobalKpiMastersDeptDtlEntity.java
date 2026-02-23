package com.asg.settings.entity;

import com.asg.common.lib.entity.BaseEntity;
import com.asg.settings.entity.key.GlobalKpiMastersDtlId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "GLOBAL_KPI_MASTERS_DEPT_DTL")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@IdClass(GlobalKpiMastersDtlId.class)
public class GlobalKpiMastersDeptDtlEntity extends BaseEntity {

    @Column(name = "TRANSACTION_POID")
    @Id
    private Long transactionPoid;

    @Id
    @Column(name = "DET_ROW_ID" )
    private Long detRowId;

    @Column(name = "DEPT_POID")
    private Long deptPoid;

    @Column(name = "TARGET_VALUE")
    private Long targetValue;

}

