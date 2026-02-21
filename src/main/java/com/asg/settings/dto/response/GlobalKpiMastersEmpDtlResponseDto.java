package com.asg.settings.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Builder
@Getter
@Service
@AllArgsConstructor
@NoArgsConstructor
public class GlobalKpiMastersEmpDtlResponseDto {

    private Long globalKpiMastersPoid;
    private Long detRowId;

    private Long empPoid;
    private Long targetValue;

    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
    private String actionType;

}
