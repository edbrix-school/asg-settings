package com.asg.settings.dto.response;


import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "Line Name is Mandatory")
    private Long linePoid;
    @NotNull(message = "Target Value is Mandatory")
    private Long targetValue;

    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
    private String actionType;

}
