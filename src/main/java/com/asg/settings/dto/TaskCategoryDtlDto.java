package com.asg.settings.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskCategoryDtlDto {
    private Long detRowId;
    private Long categoryPoid;
    private String subCategoryDescription;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String actionType;
}
