package com.asg.settings.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ParameterDto {

    private String parameterName;
    private String parameterKeyIdType;
    private String parameterKeyId;
    private String parameterValue;
    private String parameterDetails;
    private Long parameterPoid;
    private String category;
    private String parameterType;
}
