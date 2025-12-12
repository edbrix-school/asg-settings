package com.asg.settings.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskSubCategoryDto {
    private Long poid;
    private String description;
    private String code;
    private String label;
    private Long value;
}