package com.asg.settings.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DivisionUpdateRequest {
    private String divisionName;
    private String remarks;
    @PositiveOrZero(message = "Seq No must be a number >=0")
    private Integer seqNo;
    @Pattern(regexp = "Y|N", message = "Active must be Y or N")
    private String active;
    @NotBlank(message = "Updated By is required")
    private String updatedBy;
}
