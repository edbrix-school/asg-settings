package com.asg.settings.entity;


import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import com.asg.settings.entity.key.GlobalKpiMastersDtlId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "GLOBAL_KPI_MASTERS_LINE_DTL")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(GlobalKpiMastersDtlId.class)
public class GlobalKpiMastersLineDtlEntity extends BaseEntity {

    @Id
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @Id
    @Column(name = "DET_ROW_ID" )
    private Long detRowId;

    @Column(name = "LINE_POID")
    private Long linePoid;

    @Column(name = "TARGET_VALUE")
    private Long targetValue;


}
