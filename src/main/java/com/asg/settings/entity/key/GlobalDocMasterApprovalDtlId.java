package com.asg.settings.entity.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalDocMasterApprovalDtlId {

    @Column(name = "DOC_ID", length = 20, nullable = false)
    private String docId;

    @Column(name = "DET_ROW_ID", nullable = false)
    private BigDecimal detRowId;
}
