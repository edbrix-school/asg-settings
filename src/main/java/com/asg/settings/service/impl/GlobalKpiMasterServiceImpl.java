package com.asg.settings.service.impl;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.dto.request.LogRequestDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.exception.ValidationException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.settings.dto.request.FetchRequestData;
import com.asg.settings.dto.request.GlobalKpiMastersRequestDto;
import com.asg.settings.dto.response.*;
import com.asg.settings.entity.*;
import com.asg.settings.repository.*;
import com.asg.settings.service.GlobalKpiMasterService;
import com.asg.settings.utility.GlobalKpiMasterMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class GlobalKpiMasterServiceImpl implements GlobalKpiMasterService {

    private final GlobalKpiMastersRepository globalKpiMastersRepository;
    private final GlobalKpiMastersCompanyDtlRepository companyDtlRepository;
    private final KpiMasterRepository procRepository;
    private final GlobalKpiMastersLineDtlRepository lineDtlRepository;
    private final GlobalKpiMastersEmpDtlRepository empDtlRepository;
    private final GlobalKpiMastersDeptDtlRepository deptDtlRepository;
    private final DocumentSearchService documentService;
    private final DocumentDeleteService deleteService;
    private final LoggingService loggingService;

    private static final String ACTION_NOCHANGES = "NOCHANGES";
    private static final String ACTION_ISCREATED = "ISCREATED";
    private static final String ACTION_ISUPDATED = "ISUPDATED";
    private static final String ACTION_ISDELETED = "ISDELETED";

    @Override
    public GlobalKpiMastersResponseDto getById(Long globalKpiMastersPoid) {

        GlobalKpiMastersEntity entity = findHeaderEntityById(globalKpiMastersPoid);

        // company details
        List<GlobalKpiMastersCompanyDtlEntity> companyDtlEntities = companyDtlRepository
                .findByTransactionPoid(globalKpiMastersPoid);
        List<GlobalKpiMastersCompanyDtlResponseDto> companyDtlResponseDtoList = GlobalKpiMasterMapper
                .toCompanyDtlDtoList(companyDtlEntities);
        // line details
        List<GlobalKpiMastersLineDtlEntity> lineDtlEntities = lineDtlRepository
                .findByTransactionPoid(globalKpiMastersPoid);
        List<GlobalKpiMastersLineDtlResponseDto> lineDtlResponseDtoList = GlobalKpiMasterMapper
                .toLineDtlDtoList(lineDtlEntities);
        // employee details
        List<GlobalKpiMastersEmpDtlEntity> empDtlEntities = empDtlRepository
                .findByTransactionPoid(globalKpiMastersPoid);
        List<GlobalKpiMastersEmpDtlResponseDto> empDtlResponseDtoList = GlobalKpiMasterMapper
                .toEmpDtlDtoList(empDtlEntities);
        // department details
        List<GlobalKpiMastersDeptDtlEntity> deptDtlEntities = deptDtlRepository
                .findByTransactionPoid(globalKpiMastersPoid);
        List<GlobalKpiMastersDeptDtlResponseDto> deptDtlResponseDtoList = GlobalKpiMasterMapper
                .toDeptDtlDtoList(deptDtlEntities);

        return GlobalKpiMasterMapper.toDto(entity, companyDtlResponseDtoList, empDtlResponseDtoList,
                deptDtlResponseDtoList, lineDtlResponseDtoList);

    }

    @Override
    public Map<String, Object> list(FilterRequestDto filters, Pageable pageable) {
        String operator = documentService.resolveOperator(filters);
        String isDeleted = documentService.resolveIsDeleted(filters);
        List<FilterDto> filterList = documentService.resolveFilters(filters);

        RawSearchResult raw = documentService.search(
                UserContext.getDocumentId(),
                filterList,
                operator,
                pageable,
                isDeleted,
                "GLOBAL_KPI_MASTERS_POID",
                "KPI_NAME");

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());
        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    @Override
    public void delete(Long globalKpiMastersPoid, DeleteReasonDto deleteReasonDto) {

        findHeaderEntityById(globalKpiMastersPoid);
        deleteService.deleteDocument(globalKpiMastersPoid, "GLOBAL_KPI_MASTERS",
                "GLOBAL_KPI_MASTERS_POID", deleteReasonDto, null);

    }

    @Override
    @Transactional
    public GlobalKpiMastersResponseDto create(GlobalKpiMastersRequestDto requestDto) {

        if (checkKpiNameExists(requestDto.getKpiName(), null)){
            throw new ValidationException("KPI Name already exists");
        }

        GlobalKpiMastersEntity entity = GlobalKpiMasterMapper.toHeaderCreateEntity(requestDto,
                new GlobalKpiMastersEntity());
        GlobalKpiMastersEntity saved = globalKpiMastersRepository.save(entity);
        saveChildTables(requestDto, saved.getGlobalKpiMastersPoid());
        loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, UserContext.getDocumentId(),
                saved.getGlobalKpiMastersPoid().toString());
        return getById(saved.getGlobalKpiMastersPoid());
    }

    @Override
    public GlobalKpiMastersResponseDto update(GlobalKpiMastersRequestDto requestDto, Long globalKpiMastersPoid) {

        if (checkKpiNameExists(requestDto.getKpiName(), globalKpiMastersPoid)){
            throw new ValidationException("KPI Name already exists");
        }

        GlobalKpiMastersEntity existingEntity = findHeaderEntityById(globalKpiMastersPoid);
        GlobalKpiMastersEntity oldEntity = new GlobalKpiMastersEntity();
        BeanUtils.copyProperties(existingEntity, oldEntity);
        GlobalKpiMastersEntity entity = GlobalKpiMasterMapper.toHeaderCreateEntity(requestDto, existingEntity);
        globalKpiMastersRepository.save(entity);
        updateChildTables(requestDto, entity.getGlobalKpiMastersPoid());
        loggingService.logChanges(oldEntity, existingEntity, GlobalKpiMastersEntity.class, UserContext.getDocumentId(),
                globalKpiMastersPoid.toString(), LogDetailsEnum.MODIFIED, "GLOBAL_KPI_MASTERS_POID");
        return getById(globalKpiMastersPoid);
    }

    @Override
    public List<KpiLineMasterResponseDto> getAllLines(FetchRequestData requestData) {

        List<KpiLineMasterResponseDto> lines =
                procRepository.getAllLines();

        if (requestData == null
                || requestData.getPoid() == null
                || requestData.getPoid().isEmpty()) {
            return lines;
        }

        Set<Long> excluded = new HashSet<>(requestData.getPoid());

        return lines.stream()
                .filter(line -> !excluded.contains(line.getLinePoid()))
                .toList();
    }

    @Override
    public List<KpiCompanyMasterResponseDto> getAllCompanies(FetchRequestData requestData) {

        List<KpiCompanyMasterResponseDto> companies =
                procRepository.getAllCompanies();

        if (requestData == null
                || requestData.getPoid() == null
                || requestData.getPoid().isEmpty()) {
            return companies;
        }

        Set<Long> excluded = new HashSet<>(requestData.getPoid());

        return companies.stream()
                .filter(company -> !excluded.contains(company.getCompanyPoid()))
                .toList();
    }

    @Override
    public List<KpiEmployeeMasterResponseDto> getAllEmployees(FetchRequestData requestData) {

        List<KpiEmployeeMasterResponseDto> employees =
                procRepository.getAllEmployees();

        if (requestData == null
                || requestData.getPoid() == null
                || requestData.getPoid().isEmpty()) {
            return employees;
        }

        Set<Long> excluded = new HashSet<>(requestData.getPoid());

        return employees.stream()
                .filter(emp -> !excluded.contains(emp.getEmployeePoid()))
                .toList();
    }

    @Override
    public boolean checkKpiNameExists(String kpiName, Long kpiPoid) {

        if (kpiName == null || kpiName.trim().isEmpty()) {
            return false;
        }

        if (kpiPoid == null || kpiPoid == 0) {
            return globalKpiMastersRepository.existsByKpiNameIgnoreCase(
                    kpiName
            );
        }

        return globalKpiMastersRepository
                .existsByKpiNameIgnoreCaseAndGlobalKpiMastersPoidNot(
                        kpiName,  kpiPoid
                );
    }

    private void updateChildTables(GlobalKpiMastersRequestDto requestDto, Long globalKpiMastersPoid) {

        // Line - Wise
        if (requestDto.getLineWiseSettings() != null && !requestDto.getLineWiseSettings().isEmpty()) {

            List<GlobalKpiMastersLineDtlEntity> entitiesToUpdate = new ArrayList<>();
            List<GlobalKpiMastersLineDtlEntity> entitiesToDelete = new ArrayList<>();
            List<GlobalKpiMastersLineDtlResponseDto> entitiesToCreate = new ArrayList<>();
            List<LogRequestDto<GlobalKpiMastersLineDtlEntity>> logRequests = new ArrayList<>();

            for (GlobalKpiMastersLineDtlResponseDto dto : requestDto.getLineWiseSettings()) {

                String action = resolveAction(dto.getActionType());

                switch (action) {

                    case ACTION_NOCHANGES -> {
                    }

                    case ACTION_ISDELETED -> {
                        if (dto.getDetRowId() != null) {

                            GlobalKpiMastersLineDtlEntity entity = lineDtlRepository
                                    .findByTransactionPoidAndDetRowId(globalKpiMastersPoid, dto.getDetRowId())
                                    .orElseThrow(() -> new ResourceNotFoundException("Line Details", "DetRowId",
                                            dto.getDetRowId()));
                            entitiesToDelete.add(entity);
                        } else {
                            throw new ValidationException("Linewise Settings Detail DetRowId is null");
                        }
                    }

                    case ACTION_ISCREATED -> {
                        entitiesToCreate.add(dto);
                    }

                    case ACTION_ISUPDATED -> {
                        if (dto.getDetRowId() != null) {
                            GlobalKpiMastersLineDtlEntity entity = lineDtlRepository
                                    .findByTransactionPoidAndDetRowId(globalKpiMastersPoid, dto.getDetRowId())
                                    .orElseThrow(() -> new ResourceNotFoundException("Line Details", "DetRowId",
                                            dto.getDetRowId()));
                            GlobalKpiMastersLineDtlEntity oldEntity = new GlobalKpiMastersLineDtlEntity();
                            BeanUtils.copyProperties(entity, oldEntity);
                            GlobalKpiMasterMapper.toLineDtlEntity(entity, dto);
                            entitiesToUpdate.add(entity);
                            String logDetail = String.format("KeyId: GLOBAL_KPI_MASTERS_POID:%s DET_ROW_ID:%s",
                                    globalKpiMastersPoid, dto.getDetRowId());
                            logRequests.add(new LogRequestDto<>(oldEntity, entity, GlobalKpiMastersLineDtlEntity.class,
                                    UserContext.getDocumentId(), globalKpiMastersPoid.toString(), logDetail));

                        } else {
                            throw new ValidationException("Line wise Settings Detail DetRowId is null for update");
                        }
                    }
                }
            }

            if (!entitiesToDelete.isEmpty()) {
                lineDtlRepository.deleteAll(entitiesToDelete);
                entitiesToDelete.forEach(entity -> loggingService.logDelete(entity, UserContext.getDocumentId(),
                        globalKpiMastersPoid.toString()));
            }

            if (!entitiesToUpdate.isEmpty()) {
                lineDtlRepository.saveAll(entitiesToUpdate);
            }

            if (!logRequests.isEmpty()) {
                loggingService.createLogBatch(logRequests);
            }

            if (!entitiesToCreate.isEmpty()) {
                saveLineDetails(entitiesToCreate, globalKpiMastersPoid);
            }
        }

        // Company - Wise
        if (requestDto.getCompanyWiseSettings() != null && !requestDto.getCompanyWiseSettings().isEmpty()) {

            List<GlobalKpiMastersCompanyDtlEntity> toUpdate = new ArrayList<>();
            List<GlobalKpiMastersCompanyDtlEntity> toDelete = new ArrayList<>();
            List<GlobalKpiMastersCompanyDtlResponseDto> toSave = new ArrayList<>();
            List<LogRequestDto<GlobalKpiMastersCompanyDtlEntity>> logRequests = new ArrayList<>();

            for (GlobalKpiMastersCompanyDtlResponseDto dto : requestDto.getCompanyWiseSettings()) {

                String action = resolveAction(dto.getActionType());

                switch (action) {

                    case ACTION_NOCHANGES -> {
                    }

                    case ACTION_ISDELETED -> {
                        if (dto.getDetRowId() == null)
                            throw new ValidationException("Company wise Settings Detail DetRowId is null");

                        GlobalKpiMastersCompanyDtlEntity entity = companyDtlRepository
                                .findByTransactionPoidAndDetRowId(globalKpiMastersPoid, dto.getDetRowId())
                                .orElseThrow(() -> new ResourceNotFoundException("Company Details", "DetRowId",
                                        dto.getDetRowId()));

                        toDelete.add(entity);

                    }

                    case ACTION_ISCREATED -> toSave.add(dto);

                    case ACTION_ISUPDATED -> {
                        if (dto.getDetRowId() == null)
                            throw new ValidationException("Company wise Settings Detail DetRowId is null for update");

                        GlobalKpiMastersCompanyDtlEntity entity = companyDtlRepository
                                .findByTransactionPoidAndDetRowId(globalKpiMastersPoid, dto.getDetRowId())
                                .orElseThrow(() -> new ResourceNotFoundException("Company Details", "DetRowId",
                                        dto.getDetRowId()));

                        GlobalKpiMastersCompanyDtlEntity oldEntity = new GlobalKpiMastersCompanyDtlEntity();
                        BeanUtils.copyProperties(entity, oldEntity);

                        GlobalKpiMasterMapper.toCompanyDtlEntity(dto, entity);

                        toUpdate.add(entity);

                        String logDetail = String.format("KeyId: GLOBAL_KPI_MASTERS_POID:%s DET_ROW_ID:%s",
                                globalKpiMastersPoid, dto.getDetRowId());
                        logRequests.add(new LogRequestDto<>(oldEntity, entity, GlobalKpiMastersCompanyDtlEntity.class,
                                UserContext.getDocumentId(), globalKpiMastersPoid.toString(), logDetail));
                    }
                }
            }

            if (!toDelete.isEmpty()) {
                companyDtlRepository.deleteAll(toDelete);
                toDelete.forEach(entity -> loggingService.logDelete(entity, UserContext.getDocumentId(),
                        globalKpiMastersPoid.toString()));

            }
            if (!toUpdate.isEmpty())
                companyDtlRepository.saveAll(toUpdate);
            if (!logRequests.isEmpty())
                loggingService.createLogBatch(logRequests);
            if (!toSave.isEmpty()) {
                saveCompanyDetails(toSave, globalKpiMastersPoid);
            }
        }

        // Department-Wise
        if (requestDto.getDepartmentWiseSettings() != null && !requestDto.getDepartmentWiseSettings().isEmpty()) {

            List<GlobalKpiMastersDeptDtlEntity> entitiesToUpdate = new ArrayList<>();
            List<GlobalKpiMastersDeptDtlEntity> entitiesToDelete = new ArrayList<>();
            List<LogRequestDto<GlobalKpiMastersDeptDtlEntity>> logRequests = new ArrayList<>();
            List<GlobalKpiMastersDeptDtlResponseDto> toSave = new ArrayList<>();

            for (GlobalKpiMastersDeptDtlResponseDto dto : requestDto.getDepartmentWiseSettings()) {

                String action = resolveAction(dto.getActionType());

                switch (action) {

                    case ACTION_NOCHANGES -> {
                    }

                    case ACTION_ISDELETED -> {
                        if (dto.getDetRowId() != null) {

                            GlobalKpiMastersDeptDtlEntity entity = deptDtlRepository
                                    .findByTransactionPoidAndDetRowId(globalKpiMastersPoid, dto.getDetRowId())
                                    .orElseThrow(() -> new ResourceNotFoundException("Department Details", "DetRowId",
                                            dto.getDetRowId()));

                            entitiesToDelete.add(entity);

                        } else {
                            throw new ValidationException("Department wise Settings Detail DetRowId is null");
                        }
                    }

                    case ACTION_ISCREATED -> {
                        toSave.add(dto);
                    }

                    case ACTION_ISUPDATED -> {
                        if (dto.getDetRowId() != null) {

                            GlobalKpiMastersDeptDtlEntity entity = deptDtlRepository
                                    .findByTransactionPoidAndDetRowId(globalKpiMastersPoid, dto.getDetRowId())
                                    .orElseThrow(() -> new ResourceNotFoundException("Department Details", "DetRowId",
                                            dto.getDetRowId()));

                            GlobalKpiMastersDeptDtlEntity oldEntity = new GlobalKpiMastersDeptDtlEntity();
                            BeanUtils.copyProperties(entity, oldEntity);

                            GlobalKpiMasterMapper.toDeptDtlEntity(dto, entity);

                            entitiesToUpdate.add(entity);

                            String logDetail = String.format("KeyId: GLOBAL_KPI_MASTERS_POID:%s DET_ROW_ID:%s",
                                    globalKpiMastersPoid, dto.getDetRowId());

                            logRequests.add(new LogRequestDto<>(oldEntity, entity,
                                    GlobalKpiMastersDeptDtlEntity.class,
                                    UserContext.getDocumentId(),
                                    globalKpiMastersPoid.toString(),
                                    logDetail));

                        } else {
                            throw new ValidationException(
                                    "Department wise Settings Detail DetRowId is null for update");
                        }
                    }
                }
            }

            if (!entitiesToDelete.isEmpty()) {
                deptDtlRepository.deleteAll(entitiesToDelete);
                entitiesToDelete.forEach(entity -> loggingService.logDelete(entity, UserContext.getDocumentId(),
                        globalKpiMastersPoid.toString()));
            }
            if (!toSave.isEmpty()) {
                saveDepartmentDetails(toSave, globalKpiMastersPoid);
            }

            if (!entitiesToUpdate.isEmpty()) {
                deptDtlRepository.saveAll(entitiesToUpdate);
            }

            if (!logRequests.isEmpty()) {
                loggingService.createLogBatch(logRequests);
            }
        }

        // Employee-Wise
        if (requestDto.getEmployeeWiseSettings() != null && !requestDto.getEmployeeWiseSettings().isEmpty()) {

            List<GlobalKpiMastersEmpDtlEntity> entitiesToUpdate = new ArrayList<>();
            List<GlobalKpiMastersEmpDtlEntity> entitiesToDelete = new ArrayList<>();
            List<LogRequestDto<GlobalKpiMastersEmpDtlEntity>> logRequests = new ArrayList<>();
            List<GlobalKpiMastersEmpDtlResponseDto> toSave = new ArrayList<>();

            for (GlobalKpiMastersEmpDtlResponseDto dto : requestDto.getEmployeeWiseSettings()) {

                String action = resolveAction(dto.getActionType());

                switch (action) {

                    case ACTION_NOCHANGES -> {
                    }

                    case ACTION_ISDELETED -> {
                        if (dto.getDetRowId() != null) {

                            GlobalKpiMastersEmpDtlEntity entity = empDtlRepository
                                    .findByTransactionPoidAndDetRowId(globalKpiMastersPoid, dto.getDetRowId())
                                    .orElseThrow(() -> new ResourceNotFoundException("Employee Details", "DetRowId",
                                            dto.getDetRowId()));

                            entitiesToDelete.add(entity);

                        } else {
                            throw new ValidationException("Employeewise Settings Detail DetRowId is null");
                        }
                    }

                    case ACTION_ISCREATED -> {
                        toSave.add(dto);
                    }

                    case ACTION_ISUPDATED -> {
                        if (dto.getDetRowId() != null) {

                            GlobalKpiMastersEmpDtlEntity entity = empDtlRepository
                                    .findByTransactionPoidAndDetRowId(globalKpiMastersPoid, dto.getDetRowId())
                                    .orElseThrow(() -> new ResourceNotFoundException("Employee Details", "DetRowId",
                                            dto.getDetRowId()));

                            GlobalKpiMastersEmpDtlEntity oldEntity = new GlobalKpiMastersEmpDtlEntity();
                            BeanUtils.copyProperties(entity, oldEntity);

                            GlobalKpiMasterMapper.toEmpDtlEntity(dto, entity);

                            entitiesToUpdate.add(entity);

                            String logDetail = String.format("KeyId: GLOBAL_KPI_MASTERS_POID:%s DET_ROW_ID:%s",
                                    globalKpiMastersPoid, dto.getDetRowId());

                            logRequests.add(new LogRequestDto<>(oldEntity, entity,
                                    GlobalKpiMastersEmpDtlEntity.class,
                                    UserContext.getDocumentId(),
                                    globalKpiMastersPoid.toString(),
                                    logDetail));

                        } else {
                            throw new ValidationException("Employee wise Settings Detail DetRowId is null for update");
                        }
                    }
                }
            }

            if (!entitiesToDelete.isEmpty()) {
                empDtlRepository.deleteAll(entitiesToDelete);
                entitiesToDelete.forEach(entity -> loggingService.logDelete(entity, UserContext.getDocumentId(),
                        globalKpiMastersPoid.toString()));
            }

            if (!entitiesToUpdate.isEmpty()) {
                empDtlRepository.saveAll(entitiesToUpdate);
            }
            if (!toSave.isEmpty()) {
                saveEmployeeDetails(toSave, globalKpiMastersPoid);
            }

            if (!logRequests.isEmpty()) {
                loggingService.createLogBatch(logRequests);
            }
        }
    }

    private void saveChildTables(GlobalKpiMastersRequestDto requestDto,
            Long globalKpiMastersPoid) {

        saveLineDetails(requestDto.getLineWiseSettings(), globalKpiMastersPoid);
        saveCompanyDetails(requestDto.getCompanyWiseSettings(), globalKpiMastersPoid);
        saveEmployeeDetails(requestDto.getEmployeeWiseSettings(), globalKpiMastersPoid);
        saveDepartmentDetails(requestDto.getDepartmentWiseSettings(), globalKpiMastersPoid);
    }

    private void saveLineDetails(List<GlobalKpiMastersLineDtlResponseDto> requestDto,
            Long poid) {

        if (requestDto == null ||
                requestDto.isEmpty())
            return;
        List<GlobalKpiMastersLineDtlEntity> toSave = new ArrayList<>();

        AtomicLong counter = new AtomicLong(lineDtlRepository.findMaxDetRowId(poid));

        requestDto.forEach(dto -> {

            Long detRowId = counter.incrementAndGet();

            GlobalKpiMastersLineDtlEntity entity = GlobalKpiMasterMapper.toLineDtlEntity(
                    new GlobalKpiMastersLineDtlEntity(), dto);
            entity.setTransactionPoid(poid);
            entity.setDetRowId(detRowId);
            toSave.add(entity);
        });
        lineDtlRepository.saveAll(toSave);
        toSave.forEach(kpi -> {
            String logDetail = String.format("Row Created on Line wise details with DetRowId %s ",
                    kpi.getDetRowId().toString());
            loggingService.createLogSummaryEntry(UserContext.getDocumentId(), poid.toString(), logDetail);
        });
    }

    private void saveCompanyDetails(List<GlobalKpiMastersCompanyDtlResponseDto> requestDto,
            Long poid) {

        if (requestDto == null ||
                requestDto.isEmpty())
            return;

        List<GlobalKpiMastersCompanyDtlEntity> toSave = new ArrayList<>();

        AtomicLong counter = new AtomicLong(companyDtlRepository.findMaxDetRowId(poid));

        requestDto.forEach(dto -> {

            Long detRowId = counter.incrementAndGet();

            GlobalKpiMastersCompanyDtlEntity entity = GlobalKpiMasterMapper.toCompanyDtlEntity(
                    dto, new GlobalKpiMastersCompanyDtlEntity());

            entity.setTransactionPoid(poid);
            entity.setDetRowId(detRowId);
            toSave.add(entity);
        });
        companyDtlRepository.saveAll(toSave);
        toSave.forEach(cat -> {
            String logDetail = String.format("Row Created on Company wise details with DetRowId %s ",
                    cat.getDetRowId().toString());
            loggingService.createLogSummaryEntry(UserContext.getDocumentId(), poid.toString(), logDetail);
        });
    }

    private void saveEmployeeDetails(List<GlobalKpiMastersEmpDtlResponseDto> requestDto,
            Long poid) {

        if (requestDto == null ||
                requestDto.isEmpty())
            return;
        List<GlobalKpiMastersEmpDtlEntity> toSave = new ArrayList<>();

        AtomicLong counter = new AtomicLong(empDtlRepository.findMaxDetRowId(poid));

        requestDto.forEach(dto -> {

            Long detRowId = counter.incrementAndGet();

            GlobalKpiMastersEmpDtlEntity entity = GlobalKpiMasterMapper.toEmpDtlEntity(
                    dto, new GlobalKpiMastersEmpDtlEntity());

            entity.setTransactionPoid(poid);
            entity.setDetRowId(detRowId);
            toSave.add(entity);
        });
        empDtlRepository.saveAll(toSave);
        toSave.forEach(cat -> {
            String logDetail = String.format("Row Created on Employee wise details with DetRowId %s ",
                    cat.getDetRowId().toString());
            loggingService.createLogSummaryEntry(UserContext.getDocumentId(), poid.toString(), logDetail);
        });
    }

    private void saveDepartmentDetails(List<GlobalKpiMastersDeptDtlResponseDto> requestDto,
            Long poid) {

        if (requestDto == null ||
                requestDto.isEmpty())
            return;

        List<GlobalKpiMastersDeptDtlEntity> toSave = new ArrayList<>();

        AtomicLong counter = new AtomicLong(deptDtlRepository.findMaxDetRowId(poid));

        requestDto.forEach(dto -> {

            Long detRowId = counter.incrementAndGet();

            GlobalKpiMastersDeptDtlEntity entity = GlobalKpiMasterMapper.toDeptDtlEntity(
                    dto, new GlobalKpiMastersDeptDtlEntity());

            entity.setTransactionPoid(poid);
            entity.setDetRowId(detRowId);

            toSave.add(entity);
        });
        deptDtlRepository.saveAll(toSave);
        toSave.forEach(cat -> {
            String logDetail = String.format("Row Created on Department wise details with DetRowId %s ",
                    cat.getDetRowId().toString());
            loggingService.createLogSummaryEntry(UserContext.getDocumentId(), poid.toString(), logDetail);
        });
    }

    private GlobalKpiMastersEntity findHeaderEntityById(Long globalKpiMastersPoid) {
        return globalKpiMastersRepository.findById(globalKpiMastersPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Global KPI Master", "Global KPI Master Poid ",
                        globalKpiMastersPoid));
    }

    private String resolveAction(String rawAction) {
        String action = (rawAction == null || rawAction.trim().isEmpty()) ? ACTION_NOCHANGES
                : rawAction.trim().toUpperCase();
        return switch (action) {
            case "ISCREATED", "CREATED", "NEW" -> ACTION_ISCREATED;
            case "ISUPDATED", "UPDATED" -> ACTION_ISUPDATED;
            case "ISDELETED", "DELETED" -> ACTION_ISDELETED;
            default -> ACTION_NOCHANGES;
        };
    }
}
