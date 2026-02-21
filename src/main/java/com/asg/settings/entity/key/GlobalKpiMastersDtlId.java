package com.asg.settings.entity.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class GlobalKpiMastersDtlId implements Serializable {

    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @Column(name = "DET_ROW_ID")
    private Long detRowId;
}