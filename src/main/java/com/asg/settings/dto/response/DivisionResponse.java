package com.asg.settings.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DivisionResponse {
    private Long divisionId;
    private String divisionCode;
    private String divisionName;
    private String companyName;
    private String address;
    private String remarks;
    private String active;
    private Integer seqNo;
    private String deleted;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
