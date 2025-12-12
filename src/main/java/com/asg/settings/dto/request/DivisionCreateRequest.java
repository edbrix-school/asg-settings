package com.asg.settings.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class DivisionCreateRequest {
    @NotBlank(message = "Division Code is required")
    private String divisionCode;

    @NotBlank(message = "Division Name is required")
    private String divisionName;

    private String remarks;

    @NotNull(message = "Seq No is required")
    @PositiveOrZero(message = "SeqNo must be a number >=0")
    private Integer seqNo;

    @Pattern(regexp = "Y|N", message = "Active must be Y or N")
    private String active;

    @NotBlank(message = "CreatedBy is required")
    private String createdBy;
}
