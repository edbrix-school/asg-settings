package com.asg.settings.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class DocumenResponsetDto {
    private String docType;
    private String docId;
    private String docShortName;
    private String rights;
}
