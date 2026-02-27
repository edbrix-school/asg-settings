package com.asg.settings.dto.request;

import com.asg.settings.dto.response.GlobalKpiMastersCompanyDtlResponseDto;
import com.asg.settings.dto.response.GlobalKpiMastersDeptDtlResponseDto;
import com.asg.settings.dto.response.GlobalKpiMastersEmpDtlResponseDto;
import com.asg.settings.dto.response.GlobalKpiMastersLineDtlResponseDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GlobalKpiMastersRequestDto {

    private String kpiCode;
    @NotBlank(message = "KPI name is mandatory")
    private String kpiName;
    @NotBlank(message = "Department is mandatory")
    private String departments;
    @NotBlank(message = "KPI unit is mandatory")
    private String kpiUnit;
    @NotBlank(message = "Frequency is mandatory")
    private String frequency;
    private String sqlProcedure;
    private String sqlQueryLineKpi;
    private String sqlQueryCompanyKpi;
    private String sqlQueryEmpKpi;
    private String sqlQueryDeptKpi;
    private String active;
    private Long seqNo;

    List<GlobalKpiMastersLineDtlResponseDto> lineWiseSettings;
    List<GlobalKpiMastersCompanyDtlResponseDto> companyWiseSettings;
    List<GlobalKpiMastersEmpDtlResponseDto> employeeWiseSettings;
    List <GlobalKpiMastersDeptDtlResponseDto> departmentWiseSettings;
}
