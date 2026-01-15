package com.asg.settings.service.impl;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.LovGetListDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.enums.AlertCheckTypeEnum;
import com.asg.common.lib.enums.FrequencyTypeEnum;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.utility.ASGHelperUtils;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.settings.dto.AlertAndRemainderDto;
import com.asg.settings.entity.AlertConfigEntity;
import com.asg.settings.entity.RoleEntity;
import com.asg.settings.repository.AlertConfigRepository;
import com.asg.settings.repository.RoleRepository;
import com.asg.settings.service.AlertConfigService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertConfigServiceImpl implements AlertConfigService {

    @Autowired
    private LoggingService loggingService;

    @Autowired
    private DocumentDeleteService documentDeleteService;

    private final AlertConfigRepository alertConfigRepository;
    private final DocumentSearchService documentService;
    private final RoleRepository roleRepository;

    public Map<String, Object> getAllAlertConfigs(String docId, FilterRequestDto request, Pageable pageable) {
        String operator = documentService.resolveOperator(request);
        String isDeleted = documentService.resolveIsDeleted(request);
        List<FilterDto> filters = documentService.resolveFilters(request);

        RawSearchResult raw = documentService.search(docId, filters, operator, pageable, isDeleted,
                "ALERT_NAME",   // label
                "CONFIG_POID");    // value);

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    @Transactional
    @Override
    public AlertAndRemainderDto createAlert(AlertAndRemainderDto request) {

        AlertConfigEntity entity = convertFromAlertDtoToAlertEntity(request);
        AlertConfigEntity alertConfigEntity = alertConfigRepository.save(entity);
        String docId = UserContext.getDocumentId();
        String key = alertConfigEntity.getConfigPoid().toString();

        loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, docId, key);
        return convertFromAlertEntityToAlertDto(alertConfigEntity);
    }

    private String getCurrentUser() {
        return UserContext.getUserId() != null ? String.valueOf(UserContext.getUserId()) : "SYSTEM";
    }


    public AlertAndRemainderDto getByAlertConfigId(Long configId) {
        AlertConfigEntity alertConfigEntity = alertConfigRepository.findByConfigPoid(configId);
        if (alertConfigEntity == null) {
            throw new ResourceNotFoundException("Alerts & Config", "configId", configId);
        }
        return convertFromAlertEntityToAlertDto(alertConfigEntity);
    }

    private AlertConfigEntity convertFromAlertDtoToAlertEntity(AlertAndRemainderDto dto) {

        AlertConfigEntity entity = new AlertConfigEntity();
        entity.setConfigPoid(dto.getConfigPoid());
        entity.setAlertName(dto.getAlertName());
        entity.setSqlQuery(dto.getSqlQuery());
        entity.setExpiryDateField(dto.getExpiryDateField());
        entity.setNotifyDays(dto.getNotifyDays());
        entity.setNotifyUserRolesPoid(ASGHelperUtils.convertListToString(dto.getNotifyUserRolesPoid()));
        entity.setActive(StringUtils.isBlank(dto.getActive()) ? "Y" : dto.getActive());
        entity.setSeqNo(dto.getSeqNo());
        entity.setCreatedBy(getCurrentUser());
        entity.setCreatedDate(LocalDateTime.now());
        entity.setLastModifiedBy(getCurrentUser());
        entity.setLastModifiedDate(LocalDateTime.now());
        entity.setAlertCheckType(dto.getAlertCheckType() != null ? dto.getAlertCheckType().getValue() : null);
        entity.setFrequencyType(dto.getFrequencyType() != null ? dto.getFrequencyType().name() : FrequencyTypeEnum.DAY.name());
        entity.setEscalateDays(dto.getEscalateDays());
        entity.setEscalateUserRolesPoid(ASGHelperUtils.convertListToString(dto.getEscalationUserRolesPoid()));
        entity.setDeleted(StringUtils.isBlank(dto.getDeleted()) ? "N" : dto.getDeleted());
        entity.setAlertEscalateFrequency(dto.getAlertEscalateFrequency() != null ? dto.getAlertEscalateFrequency() : 1);
        entity.setAlertNotifyFrequency(dto.getAlertNotifyFrequency() != null ? dto.getAlertNotifyFrequency() : 1);
        entity.setEscalateAlertSendMailDate(dto.getEscalateAlertSendMailDate() == null ? new Date() : dto.getEscalateAlertSendMailDate());
        entity.setNotifyAlertSendMailDate(dto.getNotifyAlertSendMailDate() == null ? new Date() : dto.getNotifyAlertSendMailDate());
        entity.setDailyRecurrence(dto.getDailyRecurrence());

        return entity;
    }


    private AlertAndRemainderDto convertFromAlertEntityToAlertDto(AlertConfigEntity entity) {
        AlertAndRemainderDto dto = new AlertAndRemainderDto();
        dto.setConfigPoid(entity.getConfigPoid());
        dto.setAlertName(entity.getAlertName());
        dto.setSqlQuery(entity.getSqlQuery());
        dto.setExpiryDateField(entity.getExpiryDateField());
        dto.setNotifyDays(entity.getNotifyDays());

        // --- Notify User Roles ---
        List<LovGetListDto> notifyRoles = new ArrayList<>();
        if (entity.getNotifyUserRolesPoid() != null && !entity.getNotifyUserRolesPoid().isBlank()) {
            String[] userRoles = entity.getNotifyUserRolesPoid().split(";");
            for (String userRoleString : userRoles) {
                if (userRoleString != null && !userRoleString.isBlank()) {
                    RoleEntity userRoleEntity = roleRepository.findByUserRolePoid(Long.valueOf(userRoleString.trim()));
                    if (userRoleEntity != null) {
                        LovGetListDto lovDto = new LovGetListDto();
                        lovDto.setPoid(userRoleEntity.getUserRolePoid());
                        lovDto.setCode(userRoleEntity.getUserRoleId());
                        lovDto.setLabel(userRoleEntity.getUserRoleName());
                        lovDto.setValue(userRoleEntity.getUserRolePoid());
                        lovDto.setDescription(userRoleEntity.getUserRoleName());
                        lovDto.setSeqNo(0);
                        notifyRoles.add(lovDto);
                    }
                }
            }
        }
        dto.setNotifyUserRolesPoid(ASGHelperUtils.convertFromStringToList(entity.getNotifyUserRolesPoid()));
        dto.setNotifyUserRolesPoidDet(notifyRoles);

        dto.setActive(entity.getActive());
        dto.setSeqNo(entity.getSeqNo());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate() != null ? java.sql.Timestamp.valueOf(entity.getCreatedDate()) : null);
        dto.setLastModifiedBy(entity.getLastModifiedBy());
        dto.setLastModifiedDate(entity.getLastModifiedDate() != null ? java.sql.Timestamp.valueOf(entity.getLastModifiedDate()) : null);

        dto.setAlertCheckType(AlertCheckTypeEnum.fromDbValue(entity.getAlertCheckType()));
        dto.setFrequencyType(FrequencyTypeEnum.fromDbValue(entity.getFrequencyType()));
        dto.setEscalateDays(entity.getEscalateDays());

        // --- Escalation User Roles ---
        List<LovGetListDto> escalationRoles = new ArrayList<>();
        if (entity.getEscalateUserRolesPoid() != null && !entity.getEscalateUserRolesPoid().isBlank()) {
            String[] escalationUserRoles = entity.getEscalateUserRolesPoid().split(";");
            for (String userRoleString : escalationUserRoles) {
                if (userRoleString != null && !userRoleString.isBlank()) {
                    RoleEntity userRoleEntity = roleRepository.findByUserRolePoid(Long.valueOf(userRoleString.trim()));
                    if (userRoleEntity != null) {
                        LovGetListDto lovDto = new LovGetListDto();
                        lovDto.setPoid(userRoleEntity.getUserRolePoid());
                        lovDto.setCode(userRoleEntity.getUserRoleId());
                        lovDto.setLabel(userRoleEntity.getUserRoleName());
                        lovDto.setValue(userRoleEntity.getUserRolePoid());
                        lovDto.setDescription(userRoleEntity.getUserRoleName());
                        lovDto.setSeqNo(0);
                        escalationRoles.add(lovDto);
                    }
                }
            }
        }
        dto.setEscalationUserRolesPoid(ASGHelperUtils.convertFromStringToList(entity.getEscalateUserRolesPoid()));
        dto.setEscalationUserRolesPoidDet(escalationRoles);

        dto.setDeleted(entity.getDeleted());
        dto.setAlertEscalateFrequency(entity.getAlertEscalateFrequency());
        dto.setAlertNotifyFrequency(entity.getAlertNotifyFrequency());
        dto.setEscalateAlertSendMailDate(entity.getEscalateAlertSendMailDate());
        dto.setNotifyAlertSendMailDate(entity.getNotifyAlertSendMailDate());
        dto.setDailyRecurrence(entity.getDailyRecurrence());

        return dto;
    }


    @Transactional
    public AlertAndRemainderDto updateAlertConfig(Long configPoid, AlertAndRemainderDto request) {

        AlertConfigEntity existingConfig = alertConfigRepository.findByConfigPoid(configPoid);
        if (existingConfig == null) {
            throw new ResourceNotFoundException("AlertConfig", "configPoid", configPoid);
        }
        AlertConfigEntity oldEntity = new AlertConfigEntity();
        BeanUtils.copyProperties(existingConfig, oldEntity);

        existingConfig.setAlertName(request.getAlertName());
        existingConfig.setAlertCheckType(request.getAlertCheckType() != null ? request.getAlertCheckType().getValue() : null);
        existingConfig.setSqlQuery(request.getSqlQuery());
        existingConfig.setExpiryDateField(request.getExpiryDateField());
        existingConfig.setNotifyDays(request.getNotifyDays());
        existingConfig.setFrequencyType(request.getFrequencyType().name());
        existingConfig.setAlertNotifyFrequency(request.getAlertNotifyFrequency());
        existingConfig.setEscalateDays(request.getEscalateDays());
        existingConfig.setAlertEscalateFrequency(request.getAlertEscalateFrequency());
        existingConfig.setNotifyUserRolesPoid(ASGHelperUtils.convertListToString(request.getNotifyUserRolesPoid()));
        existingConfig.setEscalateUserRolesPoid(ASGHelperUtils.convertListToString(request.getEscalationUserRolesPoid()));

        existingConfig.setSeqNo(request.getSeqNo());
        existingConfig.setDeleted(request.getDeleted());
        existingConfig.setActive(request.getActive());
        existingConfig.setLastModifiedDate(LocalDateTime.now());

        existingConfig.setLastModifiedDate(LocalDateTime.now());

        existingConfig.setLastModifiedBy(getCurrentUser());

        AlertConfigEntity updatedConfig = alertConfigRepository.save(existingConfig);
        String docId = UserContext.getDocumentId();
        String key = updatedConfig.getConfigPoid().toString();

        loggingService.logChanges(oldEntity, updatedConfig,
                AlertConfigEntity.class, docId, key, LogDetailsEnum.MODIFIED, "CONFIG_POID");
        return convertFromAlertEntityToAlertDto(updatedConfig);
    }

    @Override
    @Transactional
    public List<AlertAndRemainderDto> getInactiveAndDeletedAlerts() {

        List<AlertConfigEntity> inactiveOrDeletedAlerts = alertConfigRepository.findAllByActiveAndDeleted("N", "Y");

        return inactiveOrDeletedAlerts.stream()
                .map(this::convertFromAlertEntityToAlertDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void softDeleteByconfigPoid(Long configPoid, DeleteReasonDto deleteReasonDto) {
        AlertConfigEntity entity = alertConfigRepository.findByConfigPoid(configPoid);
        if (entity == null) {
            throw new ResourceNotFoundException("Alert Config", "configPoid", configPoid);
        }
        
        documentDeleteService.deleteDocument(
                configPoid,
                "GLOB_ALERT_CONFIG",
                "CONFIG_POID",
                deleteReasonDto,
                null
        );
    }

}
