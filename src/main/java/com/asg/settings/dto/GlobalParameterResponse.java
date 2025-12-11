package com.asg.settings.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Data
public class GlobalParameterResponse {

    private List<ParameterDto> globalParameters;
    private Boolean privileged;
    private int page;
    private int size;
    private long totalElements;


}