package com.asg.settings.dto;

import lombok.Data;

import java.util.List;

@Data
public class ModuleDto {

    private String moduleShortName;
    private List<DocumenResponsetDto> documents;
}
