package com.asg.settings.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.settings.dto.request.GlobalKPIMastersRequestDto;
import com.asg.settings.dto.response.GlobalKPIMastersResponseDto;
import com.asg.settings.dto.response.KpiCompanyMasterResponseDto;
import com.asg.settings.dto.response.KpiEmployeeMasterResponseDto;
import com.asg.settings.dto.response.KpiLineMasterResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface GlobalKPIMasterService {
    GlobalKPIMastersResponseDto getById(Long globalKpiMastersPoid);

    Map<String, Object> list(FilterRequestDto filters, Pageable pageable);

    void delete(Long globalKpiMastersPoid, DeleteReasonDto deleteReasonDto);

    GlobalKPIMastersResponseDto create(GlobalKPIMastersRequestDto requestDto);

    GlobalKPIMastersResponseDto update(GlobalKPIMastersRequestDto requestDto,Long globalKpiMastersPoid);

    List<KpiLineMasterResponseDto> getAllLines();

    List<KpiCompanyMasterResponseDto> getAllCompanies();

    List<KpiEmployeeMasterResponseDto> getAllEmployees();






}
