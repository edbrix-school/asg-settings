package com.asg.settings.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TermsTemplateDtlDto {

    private Long termsPoid;

    private Long detRowId;

    @NotBlank(message = "Clause No cannot be blank")
    private String clauseNo;

    @NotBlank(message = "Clause Details cannot be blank")
    private String clauseDetails;

    @NotBlank(message = "Active cannot be blank")
    @Pattern(regexp = "^[YN]$", message = "must be either 'Y' or 'N'")
    private String active;

    // Action type for clause operations: isCreated, isUpdated, isDeleted, noChange
    private String actionType;
}
