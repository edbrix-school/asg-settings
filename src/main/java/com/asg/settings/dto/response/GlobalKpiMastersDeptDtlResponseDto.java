package com.asg.settings.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GlobalKpiMastersDeptDtlResponseDto {

    private Long globalKpiMastersPoid;
    private Long detRowId;
    private Long deptPoid;
    private Long targetValue;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
    private String actionType;

}
