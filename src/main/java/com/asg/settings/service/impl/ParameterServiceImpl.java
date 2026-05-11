package com.asg.settings.service.impl;

import com.asg.common.lib.enums.BulkUpdateStatus;
import com.asg.common.lib.enums.GlobalParameterTypeEnum;
import com.asg.common.lib.enums.ParameterUpdateStatus;
import com.asg.settings.dto.*;
import com.asg.settings.entity.GlobalParameterEntity;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.settings.repository.GlobalParameterRepository;
import com.asg.settings.repository.ParameterRepository;
import com.asg.settings.service.ParameterService;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ParameterServiceImpl implements ParameterService {

    private static final String IS_UPDATED = "isUpdated";
    private static final String NO_CHANGE = "noChange";

    private final ParameterRepository parameterRepository;
    private final GlobalParameterRepository globalParameterRepository;
    private final LoggingService loggingService;
    private final EntityManager entityManager;

    @Autowired
    public ParameterServiceImpl(ParameterRepository parameterRepository,
                                GlobalParameterRepository globalParameterRepository,
                                LoggingService loggingService,
                                EntityManager entityManager) {
        this.parameterRepository = parameterRepository;
        this.globalParameterRepository = globalParameterRepository;
        this.loggingService = loggingService;
        this.entityManager = entityManager;
    }


    public BulkUpdateResponseDTO updateParameters(UpdateParameterRequestDTO request) {
        List<ParameterUpdateResultDTO> results = new ArrayList<>();
        int successCount = 0;
        String docId = UserContext.getDocumentId();

        for (UpdateParameterDTO param : request.getParameters()) {
            String action = param.getActionType();
            if (action == null || action.isBlank()) {
                action = IS_UPDATED;
            }

            if (NO_CHANGE.equalsIgnoreCase(action)) {
                successCount++;
                continue;
            }

            if (!IS_UPDATED.equalsIgnoreCase(action)) {
                String message = String.format("Unsupported actionType '%s' for parameter updates. Only '%s' and '%s' are supported.",
                        action, IS_UPDATED, NO_CHANGE);
                log.warn(message);
                results.add(new ParameterUpdateResultDTO(
                        param.getParameterPoid(),
                        param.getParameterKeyId(),
                        ParameterUpdateStatus.FAILED,
                        message
                ));
                continue;
            }

            try {
                GlobalParameterEntity oldParam = globalParameterRepository.findById(param.getParameterPoid()).orElse(null);
                GlobalParameterEntity oldParamCopy = null;
                if (oldParam != null) {
                    oldParamCopy = new GlobalParameterEntity();
                    BeanUtils.copyProperties(oldParam, oldParamCopy);
                    entityManager.detach(oldParam);
                }

                String status = parameterRepository.callUpdateProcedure(
                        request.getLoginUserPoid(),
                        param.getParameterPoid(),
                        param.getParameterKeyId(),
                        param.getParameterValue()
                );

                ParameterUpdateStatus updateStatus = ParameterUpdateStatus.fromString(status);

                if (updateStatus == ParameterUpdateStatus.SUCCESS) {
                    successCount++;
                    GlobalParameterEntity newParam = globalParameterRepository.findById(param.getParameterPoid()).orElse(null);

                    if (oldParamCopy != null && newParam != null) {
                        String logDetail = String.format("KeyId = PARAMETER_POID %s", param.getParameterPoid());
                        loggingService.createLog(oldParamCopy, newParam, GlobalParameterEntity.class, docId,
                                param.getParameterPoid().toString(), logDetail);
                    }
                }

                results.add(new ParameterUpdateResultDTO(
                        param.getParameterPoid(),
                        param.getParameterKeyId(),
                        updateStatus,
                        updateStatus == ParameterUpdateStatus.SUCCESS ? null : status
                ));

            } catch (Exception ex) {
                log.warn("Update failed for parameterPoid {}: {}", param.getParameterPoid(), ex.getMessage());
                results.add(new ParameterUpdateResultDTO(
                        param.getParameterPoid(), param.getParameterKeyId(), ParameterUpdateStatus.FAILED, ex.getMessage()
                ));
            }
        }


        BulkUpdateStatus overallStatus;
        if (successCount == request.getParameters().size()) {
            overallStatus = BulkUpdateStatus.SUCCESS;
        } else if (successCount == 0) {
            overallStatus = BulkUpdateStatus.FAILED;
        } else {
            overallStatus = BulkUpdateStatus.PARTIAL_SUCCESS;
        }

        BulkUpdateResponseDTO response = new BulkUpdateResponseDTO();
        response.setOverallStatus(overallStatus.name());
        response.setResults(results);
        return response;
    }


    private List<ParameterDto> getParameterDtoList(List<GlobalParameterEntity> entities) {
        return entities.stream()
                .map(entity -> {
                    ParameterDto dto = new ParameterDto();
                    BeanUtils.copyProperties(entity, dto);
                    dto.setActionType("");
                    return dto;
                })
                .toList();
    }


    @Override
    public GlobalParameterResponse getUserParameters(String filter, Pageable pageable) {
        String searchValue = (filter == null || filter.isBlank()) ? null : filter.trim();
        Pageable pageableInsensitive = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                normalizeSort(pageable.getSort())
        );
        return this.getParameters(GlobalParameterTypeEnum.USER.name(), null, searchValue, pageableInsensitive);
    }

    @Override
    public GlobalParameterResponse getSystemParameters(Long userPoid, String filter, Pageable pageable) {
        String searchValue = (filter == null || filter.isBlank()) ? null : filter.trim();
        Pageable pageableInsensitive = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                normalizeSort(pageable.getSort())
        );
        return this.getParameters(GlobalParameterTypeEnum.SYSTEM.name(), userPoid, searchValue, pageableInsensitive);
    }

    private GlobalParameterResponse getParameters(String parameterType, Long userPoid, String filter, Pageable pageable) {
        Page<GlobalParameterEntity> pagedEntities =
                globalParameterRepository.findAllByParameterType(parameterType, filter, pageable);

        List<ParameterDto> parameterDtoList = this.getParameterDtoList(pagedEntities.getContent());

        GlobalParameterResponse response = new GlobalParameterResponse();
        response.setGlobalParameters(parameterDtoList);
        response.setPage(pageable.getPageNumber());
        response.setSize(pageable.getPageSize());
        response.setTotalElements(pagedEntities.getTotalElements());

        if ("SYSTEM".equalsIgnoreCase(parameterType) && userPoid != null) {
            boolean privileged = globalParameterRepository.hasSystemPrivilege(userPoid) > 0;
            response.setPrivileged(privileged);
        }
        return response;
    }

    @Override
    public Integer getParameterValueByName(String parameterType, String parameterName) {
        if ("SYSTEM".equalsIgnoreCase(parameterType)) {
            return globalParameterRepository.findParameterValueByParameterName(GlobalParameterTypeEnum.SYSTEM.name(), parameterName);
        } else return null;
    }

    // Do case-insensitive sorting, keep nulls last, and append PARAMETER_POID as stable tiebreaker
    private Sort normalizeSort(Sort sort) {
        List<Sort.Order> orders = sort.stream()
                .map(o -> isStringField(o.getProperty())
                        ? o.ignoreCase().nullsLast()
                        : o.nullsLast())
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        boolean alreadyHasPoid = orders.stream().anyMatch(o -> "parameterPoid".equals(o.getProperty()));
        if (!alreadyHasPoid) {
            orders.add(Sort.Order.asc("parameterPoid").nullsLast());
        }
        return Sort.by(orders);
    }

    //Helper to identify sorting for only string fields by matching
    private boolean isStringField(String field) {
        try {
            return GlobalParameterEntity.class
                    .getDeclaredField(field)
                    .getType()
                    .equals(String.class);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public BigDecimal getParameterValueByNameAsDecimal(String parameterType, String parameterName) {
        if ("SYSTEM".equalsIgnoreCase(parameterType)) {
            return globalParameterRepository.findParameterValueByParameterNameAsDecimal(GlobalParameterTypeEnum.SYSTEM.name(), parameterName);
        } else if ("USER".equalsIgnoreCase(parameterType)) {
            return globalParameterRepository.findParameterValueByParameterNameAsDecimal(GlobalParameterTypeEnum.USER.name(), parameterName);
        }
        return null;
    }

    @Override
    public String getParameterValue(String parameterName) {
        return globalParameterRepository.findParameterValueByName(parameterName).orElse(null);
    }

}
