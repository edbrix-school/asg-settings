package com.asg.settings.dto.response;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GlobalKpiMastersLineDtlResponseDto {

    private Long globalKpiMastersPoid;
    private Long detRowId;

    private Long linePoid;
    private Long targetValue;

    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
    private String actionType;

}
