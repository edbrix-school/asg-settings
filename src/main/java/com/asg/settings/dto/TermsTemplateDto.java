package com.asg.settings.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class TermsTemplateDto {
    private Long termsPoid;
    private Long groupPoid;
    private String templateId;
    @NotBlank(message = "Doc Id is required")
    private String docId;
    @Valid
    private DocumentLightDto document;
    @NotBlank(message = "Template Name is required")
    private String templateName;
    @NotBlank(message = "Active cannot be blank")
    @Pattern(regexp = "^[YN]$", message = "must be either 'Y' or 'N'")
    private String active;
    private Long seqNo;
    private String deleted;
    private String remarks;
    @NotBlank(message = "Terms Category is required")
    private String termsCategory;
    @Valid
    private List<TermsTemplateDtlDto> clauses;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
}
