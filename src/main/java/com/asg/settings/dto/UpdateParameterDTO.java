package com.asg.settings.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateParameterDTO {
    private Long parameterPoid;
    private String parameterKeyId;
    private String parameterValue;
    /**
     * Action type for parameter operations: isCreated, isUpdated, isDeleted, noChange
     */
    private String actionType;
}
