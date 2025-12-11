package com.asg.settings.dto;

import com.asg.common.lib.enums.ParameterUpdateStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParameterUpdateResultDTO {
    private Long parameterPoid;
    private String parameterKeyId;
    private ParameterUpdateStatus status;
    private String errorMessage;
}
