package com.asg.settings.entity.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TermsTemplateDtlKey implements Serializable {

    @Column(name = "TERMS_POID")
    private Long termsPoid;

    @Column(name = "DET_ROW_ID")
    private Long detRowId;
}
