package com.asg.settings.entity.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalDocMasterAuthDtlId implements Serializable {

    @Column(name = "DOC_ID", length = 20, nullable = false)
    private String docId;

    @Column(name = "DET_ROW_ID", nullable = false)
    private Long detRowId;
}