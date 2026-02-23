package com.asg.settings.entity;

import com.asg.common.lib.entity.BaseEntity;
import com.asg.settings.entity.key.GlobalKpiMastersDtlId;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "GLOBAL_KPI_MASTERS_COMPANY_DTL")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(GlobalKpiMastersDtlId.class)
public class GlobalKpiMastersCompanyDtlEntity extends BaseEntity {

    @Column(name = "TRANSACTION_POID")
    @Id
    private Long transactionPoid;

    @Id
    @Column(name = "DET_ROW_ID" )
    private Long detRowId;

    @Column(name = "COMPANY_POID")
    private Long companyPoid;

    @Column(name = "TARGET_VALUE")
    private Long targetValue;
}
