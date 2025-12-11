package com.asg.settings.service;

import com.asg.settings.dto.BulkUpdateResponseDTO;
import com.asg.settings.dto.GlobalParameterResponse;
import com.asg.settings.dto.UpdateParameterRequestDTO;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ParameterService {

    BulkUpdateResponseDTO updateParameters(UpdateParameterRequestDTO updateParameterDTO);

    GlobalParameterResponse getUserParameters(String filter, Pageable pageable);

    GlobalParameterResponse getSystemParameters(Long userPoid, String filter, Pageable pageable);

    Integer getParameterValueByName(String parameterType, String parameterName);

    BigDecimal getParameterValueByNameAsDecimal(String parameterType, String parameterName);
}
