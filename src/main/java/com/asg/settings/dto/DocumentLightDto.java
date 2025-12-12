package com.asg.settings.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DocumentLightDto {
    @NotBlank(message = "Doc Id is required")
    private String docId;
    private String docShortName;
    private String docShortName2;
    private String docName;
    private String docName2;
    private String moduleId;
    private String docType;
    private String isoDocument;
    private BigDecimal docPoid;
}