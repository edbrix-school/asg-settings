package com.asg.settings.entity;


import com.asg.common.lib.entity.BaseEntity;
import com.asg.settings.entity.key.GlobalKpiMastersDtlId;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "GLOBAL_KPI_MASTERS_EMP_DTL")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(GlobalKpiMastersDtlId.class)
public class GlobalKpiMastersEmpDtlEntity  extends BaseEntity {

    @Id
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @Id
    @Column(name = "DET_ROW_ID" )
    private Long detRowId;

    @Column(name = "EMP_POID")
    private Long empPoid;

    @Column(name = "TARGET_VALUE")
    private Long targetValue;



}
