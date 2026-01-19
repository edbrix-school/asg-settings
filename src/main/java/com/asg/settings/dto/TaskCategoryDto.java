package com.asg.settings.dto;

import com.asg.common.lib.dto.LovGetListDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class TaskCategoryDto {
    private Long categoryPoid;
    @Pattern(
            regexp = "^[A-Za-z0-9 ]+$",
            message = "Category description must contain only letters, numbers, and spaces"
    )
    @NotBlank(message = "Category description must not be empty")
    @Size(max = 100, message = "Category description must not exceed 100 characters")
    private String categoryDescription;
    private List<String> userRolePoid;
    private List<LovGetListDto> userRolePoidDet;
    private LovGetListDto categoryDet;
    @Size(max = 1, message = "Active must not exceed 1 characters")
    private String active;
    @PositiveOrZero(message = "Seq no must be zero or greater")
    private Integer seqNo;
    @Size(max = 1, message = "Deleted must not exceed 1 characters")
    private String deleted;
    @Size(max = 30, message = "Category code must not exceed 30 characters")
    private String categoryCode;
    @Valid
    private List<TaskCategoryDtlDto> subCategories;
    

    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
}
