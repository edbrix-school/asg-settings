package com.asg.settings.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GlobalKpiMastersResponseDto {

    private Long globalKpiMastersPoid;
    private Long groupPoid;
    private String kpiCode;
    private String kpiName;
    private String departments;
    private String kpiUnit;
    private String frequency;
    private LocalDateTime lastExecuted;
    private String sqlProcedure;
    private String sqlQueryLineKpi;
    private String sqlQueryCompanyKpi;
    private String sqlQueryEmpKpi;
    private String sqlQueryDeptKpi;
    private String active;
    private Long seqNo;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
    private String deleted;

    List<GlobalKpiMastersLineDtlResponseDto> lineWiseSettings;
    List<GlobalKpiMastersCompanyDtlResponseDto> companyWiseSettings;
    List<GlobalKpiMastersEmpDtlResponseDto> employeeWiseSettings;
    List<GlobalKpiMastersDeptDtlResponseDto> departmentWiseSettings;

}
