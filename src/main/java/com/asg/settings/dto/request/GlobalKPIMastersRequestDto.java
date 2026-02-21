package com.asg.settings.dto.request;

import com.asg.settings.dto.response.GlobalKpiMastersCompanyDtlResponseDto;
import com.asg.settings.dto.response.GlobalKpiMastersDeptDtlResponseDto;
import com.asg.settings.dto.response.GlobalKpiMastersEmpDtlResponseDto;
import com.asg.settings.dto.response.GlobalKpiMastersLineDtlResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GlobalKPIMastersRequestDto {

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
    List <GlobalKpiMastersDeptDtlResponseDto> departmentWiseSettings;
}
