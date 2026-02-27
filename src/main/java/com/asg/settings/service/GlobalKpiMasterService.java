package com.asg.settings.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.settings.dto.request.FetchRequestData;
import com.asg.settings.dto.request.GlobalKpiMastersRequestDto;
import com.asg.settings.dto.response.GlobalKpiMastersResponseDto;
import com.asg.settings.dto.response.KpiCompanyMasterResponseDto;
import com.asg.settings.dto.response.KpiEmployeeMasterResponseDto;
import com.asg.settings.dto.response.KpiLineMasterResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface GlobalKpiMasterService {
    GlobalKpiMastersResponseDto getById(Long globalKpiMastersPoid);

    Map<String, Object> list(FilterRequestDto filters, Pageable pageable);

    void delete(Long globalKpiMastersPoid, DeleteReasonDto deleteReasonDto);

    GlobalKpiMastersResponseDto create(GlobalKpiMastersRequestDto requestDto);

    GlobalKpiMastersResponseDto update(GlobalKpiMastersRequestDto requestDto, Long globalKpiMastersPoid);

    List<KpiLineMasterResponseDto> getAllLines(FetchRequestData requestData);

    List<KpiCompanyMasterResponseDto> getAllCompanies(FetchRequestData requestData);

    List<KpiEmployeeMasterResponseDto> getAllEmployees(FetchRequestData requestData);

    boolean checkKpiNameExists(String kpiName, Long kpiPoid);
}
