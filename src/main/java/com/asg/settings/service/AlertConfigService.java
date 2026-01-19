package com.asg.settings.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.settings.dto.AlertAndRemainderDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface AlertConfigService {
    Map<String, Object> getAllAlertConfigs(String docId, FilterRequestDto request, Pageable pageable);

    AlertAndRemainderDto createAlert(AlertAndRemainderDto request);

    AlertAndRemainderDto getByAlertConfigId(Long configId);

    AlertAndRemainderDto updateAlertConfig(Long configPoid, AlertAndRemainderDto updateRequest);

    List<AlertAndRemainderDto> getInactiveAndDeletedAlerts();

    void softDeleteByconfigPoid(Long configPoid, DeleteReasonDto deleteReasonDto);
}
