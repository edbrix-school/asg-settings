package com.asg.settings.service.impl;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.exception.ValidationException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.settings.dto.request.GlobalKPIMastersRequestDto;
import com.asg.settings.dto.response.*;
import com.asg.settings.entity.*;
import com.asg.settings.entity.key.GlobalKpiMastersDtlId;
import com.asg.settings.repository.*;
import com.asg.settings.service.GlobalKPIMasterService;
import com.asg.settings.utility.GlobalKPIMasterMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class GlobalKPIMasterServiceImpl implements GlobalKPIMasterService {

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
    public GlobalKPIMastersResponseDto getById(Long globalKpiMastersPoid) {

        GlobalKpiMastersEntity entity = findHeaderEntityById(globalKpiMastersPoid);

        //company details
       List<GlobalKpiMastersCompanyDtlEntity>  companyDtlEntities = companyDtlRepository.findByIdTransactionPoid(globalKpiMastersPoid);
       List<GlobalKpiMastersCompanyDtlResponseDto>  companyDtlResponseDtoList = GlobalKPIMasterMapper.toCompanyDtlDtoList(companyDtlEntities);
       //line details
       List<GlobalKpiMastersLineDtlEntity> lineDtlEntities = lineDtlRepository.findByIdTransactionPoid(globalKpiMastersPoid);
       List<GlobalKpiMastersLineDtlResponseDto> lineDtlResponseDtoList = GlobalKPIMasterMapper.toLineDtlDtoList(lineDtlEntities);
       //employee details
       List<GlobalKpiMastersEmpDtlEntity> empDtlEntities = empDtlRepository.findByIdTransactionPoid(globalKpiMastersPoid);
       List<GlobalKpiMastersEmpDtlResponseDto> empDtlResponseDtoList = GlobalKPIMasterMapper.toEmpDtlDtoList(empDtlEntities);
       //department details
       List<GlobalKpiMastersDeptDtlEntity> deptDtlEntities = deptDtlRepository.findByIdTransactionPoid(globalKpiMastersPoid);
       List<GlobalKpiMastersDeptDtlResponseDto> deptDtlResponseDtoList = GlobalKPIMasterMapper.toDeptDtlDtoList(deptDtlEntities);

      return GlobalKPIMasterMapper.toDto(entity,companyDtlResponseDtoList,empDtlResponseDtoList,deptDtlResponseDtoList,lineDtlResponseDtoList);

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
                "KPI_NAME"
        );

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());
        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    @Override
    public void delete(Long globalKpiMastersPoid, DeleteReasonDto deleteReasonDto) {

        findHeaderEntityById(globalKpiMastersPoid);
        deleteService.deleteDocument(globalKpiMastersPoid,"GLOBAL_KPI_MASTERS",
                "GLOBAL_KPI_MASTERS_POID",deleteReasonDto,null);

    }

    @Override
    @Transactional
    public GlobalKPIMastersResponseDto create(GlobalKPIMastersRequestDto requestDto) {

        GlobalKpiMastersEntity entity = GlobalKPIMasterMapper.toHeaderCreateEntity(requestDto, new GlobalKpiMastersEntity());
        entity.setCreatedBy(UserContext.getUserName());
        entity.setCreatedDate(LocalDateTime.now());
        GlobalKpiMastersEntity SavedEntity = globalKpiMastersRepository.save(entity);
        saveChildTables(requestDto, SavedEntity.getGlobalKpiMastersPoid());
        return getById(SavedEntity.getGlobalKpiMastersPoid());
    }

    @Override
    public GlobalKPIMastersResponseDto update(GlobalKPIMastersRequestDto requestDto,Long globalKpiMastersPoid) {

        GlobalKpiMastersEntity existingEntity = findHeaderEntityById(globalKpiMastersPoid);
        GlobalKpiMastersEntity entity = GlobalKPIMasterMapper.toHeaderCreateEntity(requestDto, existingEntity);
        entity.setLastModifiedBy(UserContext.getUserName());
        entity.setLastModifiedDate(LocalDateTime.now());
          globalKpiMastersRepository.save(entity);
        updateChildTables(requestDto, entity.getGlobalKpiMastersPoid());
        return getById(globalKpiMastersPoid);
    }

    @Override
    public List<KpiLineMasterResponseDto> getAllLines() {
        return procRepository.getAllLines();
    }

    @Override
    public List<KpiCompanyMasterResponseDto> getAllCompanies() {
        return procRepository.getAllCompanies();
    }

    @Override
    public List<KpiEmployeeMasterResponseDto> getAllEmployees() {
        return procRepository.getAllEmployees();
    }
    private void updateChildTables(GlobalKPIMastersRequestDto requestDto, Long globalKpiMastersPoid) {

        if (requestDto.getLineWiseSettings()!=null && !requestDto.getLineWiseSettings().isEmpty()){
            for (GlobalKpiMastersLineDtlResponseDto dto : requestDto.getLineWiseSettings()){
                String action = resolveAction(dto.getActionType());
                switch (action) {
                    case ACTION_NOCHANGES -> {
                    }

                    case ACTION_ISDELETED -> {
                        if (dto.getDetRowId() != null){
                            lineDtlRepository.deleteByIdTransactionPoidAndIdDetRowId(globalKpiMastersPoid, dto.getDetRowId());
                        String logDetail = String.format("Row Deleted on Linewise Settings with DetRowId: %s", dto.getDetRowId());
                        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), globalKpiMastersPoid.toString(), logDetail);
                    }
                        else {
                            throw new ValidationException("Linewise Settings Detail DetRowId is null");

                        }
                }
                    case ACTION_ISCREATED->{
                        saveLineDetails(requestDto, globalKpiMastersPoid, UserContext.getUserName(), LocalDateTime.now());
                    }
                    case  ACTION_ISUPDATED->{
                        if (dto.getDetRowId() != null){
                            GlobalKpiMastersLineDtlEntity entity = lineDtlRepository.findByIdTransactionPoidAndIdDetRowId(globalKpiMastersPoid, dto.getDetRowId())
                                    .orElseThrow(()-> new ResourceNotFoundException("Line Details","DetRowId",dto.getDetRowId()));
                            GlobalKPIMasterMapper.toLineDtlEntity(entity,dto);
                            entity.setLastModifiedBy(UserContext.getUserName());
                            entity.setLastModifiedDate(LocalDateTime.now());
                            lineDtlRepository.save(entity);

                        }
                        }
                }
            }
        }

        if (requestDto.getCompanyWiseSettings()!=null && !requestDto.getCompanyWiseSettings().isEmpty()){
            for (GlobalKpiMastersCompanyDtlResponseDto dto : requestDto.getCompanyWiseSettings()){
                String action = resolveAction(dto.getActionType());
                switch (action) {
                    case ACTION_NOCHANGES -> {
                    }

                    case ACTION_ISDELETED -> {
                        if (dto.getDetRowId() != null){
                            companyDtlRepository.deleteByIdTransactionPoidAndIdDetRowId(globalKpiMastersPoid, dto.getDetRowId());
                            String logDetail = String.format("Row Deleted on Companywise Settings with DetRowId: %s", dto.getDetRowId());
                            loggingService.createLogSummaryEntry(UserContext.getDocumentId(), globalKpiMastersPoid.toString(), logDetail);
                        }
                        else {
                            throw new ValidationException("Companywise Settings Detail DetRowId is null");

                        }
                    }
                    case ACTION_ISCREATED->{
                        saveCompanyDetails(requestDto, globalKpiMastersPoid, UserContext.getUserName(), LocalDateTime.now());
                    }
                    case  ACTION_ISUPDATED->{
                        if (dto.getDetRowId() != null){
                            GlobalKpiMastersCompanyDtlEntity entity = companyDtlRepository.findByIdTransactionPoidAndIdDetRowId(globalKpiMastersPoid, dto.getDetRowId())
                                    .orElseThrow(()-> new ResourceNotFoundException("Company Details", "DetRowId", dto.getDetRowId()));
                            GlobalKPIMasterMapper.toCompanyDtlEntity(dto, entity);
                            entity.setLastModifiedBy(UserContext.getUserName());
                            entity.setLastModifiedDate(LocalDateTime.now());
                            companyDtlRepository.save(entity);
                            String logDetail = String.format("Row Updated on Companywise Settings with DetRowId: %s", dto.getDetRowId());
                            loggingService.createLogSummaryEntry(UserContext.getDocumentId(), globalKpiMastersPoid.toString(), logDetail);
                        }


                    }

                }

            }
        }

 if (requestDto.getDepartmentWiseSettings() !=null && !requestDto.getDepartmentWiseSettings().isEmpty()){
            for (GlobalKpiMastersDeptDtlResponseDto dto : requestDto.getDepartmentWiseSettings()){
                String action = resolveAction(dto.getActionType());
                switch (action) {
                    case ACTION_NOCHANGES -> {
                    }

                    case ACTION_ISDELETED -> {
                        if (dto.getDetRowId() != null){
                            deptDtlRepository.deleteByIdTransactionPoidAndIdDetRowId(globalKpiMastersPoid, dto.getDetRowId());
                            String logDetail = String.format("Row Deleted on Departmentwise Settings with DetRowId: %s", dto.getDetRowId());
                            loggingService.createLogSummaryEntry(UserContext.getDocumentId(), globalKpiMastersPoid.toString(), logDetail);
                        }
                        else {
                            throw new ValidationException("Departmentwise Settings Detail DetRowId is null");

                        }
                    }
                    case ACTION_ISCREATED->{
                        saveDepartmentDetails(requestDto, globalKpiMastersPoid, UserContext.getUserName(), LocalDateTime.now());
                    }
                    case  ACTION_ISUPDATED->{
                        if (dto.getDetRowId() != null){
                            GlobalKpiMastersDeptDtlEntity entity = deptDtlRepository.findByIdTransactionPoidAndIdDetRowId(globalKpiMastersPoid, dto.getDetRowId())
                                    .orElseThrow(()-> new ResourceNotFoundException("Department Details", "DetRowId", dto.getDetRowId()));
                            GlobalKPIMasterMapper.toDeptDtlEntity(dto, entity);
                            entity.setLastModifiedBy(UserContext.getUserName());
                            entity.setLastModifiedDate(LocalDateTime.now());
                            deptDtlRepository.save(entity);
                            String logDetail = String.format("Row Updated on Departmentwise Settings with DetRowId: %s", dto.getDetRowId());
                            loggingService.createLogSummaryEntry(UserContext.getDocumentId(), globalKpiMastersPoid.toString(), logDetail);
                        }


                    }

                }

            }

        }

 if (requestDto.getEmployeeWiseSettings() !=null && !requestDto.getEmployeeWiseSettings().isEmpty()){
            for (GlobalKpiMastersEmpDtlResponseDto dto : requestDto.getEmployeeWiseSettings()){
                String action = resolveAction(dto.getActionType());
                switch (action) {
                    case ACTION_NOCHANGES -> {
                    }

                    case ACTION_ISDELETED -> {
                        if (dto.getDetRowId() != null){
                            empDtlRepository.deleteByIdTransactionPoidAndIdDetRowId(globalKpiMastersPoid, dto.getDetRowId());
                            String logDetail = String.format("Row Deleted on Employeewise Settings with DetRowId: %s", dto.getDetRowId());
                            loggingService.createLogSummaryEntry(UserContext.getDocumentId(), globalKpiMastersPoid.toString(), logDetail);
                        }
                        else {
                            throw new ValidationException("Employeewise Settings Detail DetRowId is null");

                        }
                    }
                    case ACTION_ISCREATED->{
                        saveEmployeeDetails(requestDto, globalKpiMastersPoid, UserContext.getUserName(), LocalDateTime.now());
                    }
                    case  ACTION_ISUPDATED->{
                        if (dto.getDetRowId() != null){
                            GlobalKpiMastersEmpDtlEntity entity = empDtlRepository.findByIdTransactionPoidAndIdDetRowId(globalKpiMastersPoid, dto.getDetRowId())
                                    .orElseThrow(()-> new ResourceNotFoundException("Employee Details", "DetRowId", dto.getDetRowId()));
                            GlobalKPIMasterMapper.toEmpDtlEntity(dto, entity);
                            entity.setLastModifiedBy(UserContext.getUserName());
                            entity.setLastModifiedDate(LocalDateTime.now());
                            empDtlRepository.save(entity);
                            String logDetail = String.format("Row Updated on Employeewise Settings with DetRowId: %s", dto.getDetRowId());
                            loggingService.createLogSummaryEntry(UserContext.getDocumentId(), globalKpiMastersPoid.toString(), logDetail);
                        }


                    }

                }

            }
 }

    }


    private void saveChildTables(GlobalKPIMastersRequestDto requestDto,
                                 Long globalKpiMastersPoid) {

        String currentUser = UserContext.getUserName();
        LocalDateTime now = LocalDateTime.now();

        saveLineDetails(requestDto, globalKpiMastersPoid, currentUser, now);
        saveCompanyDetails(requestDto, globalKpiMastersPoid, currentUser, now);
        saveEmployeeDetails(requestDto, globalKpiMastersPoid, currentUser, now);
        saveDepartmentDetails(requestDto, globalKpiMastersPoid, currentUser, now);
    }

    private void saveLineDetails(GlobalKPIMastersRequestDto requestDto,
                                 Long poid,
                                 String user,
                                 LocalDateTime now) {

        if (requestDto.getLineWiseSettings() == null ||
                requestDto.getLineWiseSettings().isEmpty()) return;
        List<GlobalKpiMastersLineDtlEntity>  toSave =  new ArrayList<>();

        AtomicLong counter =
                new AtomicLong(lineDtlRepository.findMaxDetRowId(poid));

        requestDto.getLineWiseSettings().forEach(dto -> {

            Long detRowId = counter.incrementAndGet();

            GlobalKpiMastersLineDtlEntity entity =
                    GlobalKPIMasterMapper.toLineDtlEntity(
                            new GlobalKpiMastersLineDtlEntity(), dto);

            entity.setId(GlobalKpiMastersDtlId.builder()
                    .transactionPoid(poid)
                    .detRowId(detRowId)
                    .build());

            entity.setCreatedBy(user);
            entity.setCreatedDate(now);
            toSave.add(entity);
        });
        lineDtlRepository.saveAll(toSave);
    }


    private void saveCompanyDetails(GlobalKPIMastersRequestDto requestDto,
                                    Long poid,
                                    String user,
                                    LocalDateTime now) {

        if (requestDto.getCompanyWiseSettings() == null ||
                requestDto.getCompanyWiseSettings().isEmpty()) return;

        List<GlobalKpiMastersCompanyDtlEntity> toSave =  new ArrayList<>();

        AtomicLong counter =
                new AtomicLong(companyDtlRepository.findMaxDetRowId(poid));

        requestDto.getCompanyWiseSettings().forEach(dto -> {

            Long detRowId = counter.incrementAndGet();

            GlobalKpiMastersCompanyDtlEntity entity =
                    GlobalKPIMasterMapper.toCompanyDtlEntity(
                            dto, new GlobalKpiMastersCompanyDtlEntity());

            entity.setId(GlobalKpiMastersDtlId.builder()
                    .transactionPoid(poid)
                    .detRowId(detRowId)
                    .build());

            entity.setCreatedBy(user);
            entity.setCreatedDate(now);
            toSave.add(entity);
        });
        companyDtlRepository.saveAll(toSave);
    }

    private void saveEmployeeDetails(GlobalKPIMastersRequestDto requestDto,
                                     Long poid,
                                     String user,
                                     LocalDateTime now) {

        if (requestDto.getEmployeeWiseSettings() == null ||
                requestDto.getEmployeeWiseSettings().isEmpty()) return;
        List<GlobalKpiMastersEmpDtlEntity> toSave = new ArrayList<>();

        AtomicLong counter =
                new AtomicLong(empDtlRepository.findMaxDetRowId(poid));

        requestDto.getEmployeeWiseSettings().forEach(dto -> {

            Long detRowId = counter.incrementAndGet();

            GlobalKpiMastersEmpDtlEntity entity =
                    GlobalKPIMasterMapper.toEmpDtlEntity(
                            dto, new GlobalKpiMastersEmpDtlEntity());

            entity.setId(GlobalKpiMastersDtlId.builder()
                    .transactionPoid(poid)
                    .detRowId(detRowId)
                    .build());

            entity.setCreatedBy(user);
            entity.setCreatedDate(now);
            toSave.add(entity);
        });
        empDtlRepository.saveAll(toSave);
    }

    private void saveDepartmentDetails(GlobalKPIMastersRequestDto requestDto,
                                       Long poid,
                                       String user,
                                       LocalDateTime now) {

        if (requestDto.getDepartmentWiseSettings() == null ||
                requestDto.getDepartmentWiseSettings().isEmpty()) return;

        List<GlobalKpiMastersDeptDtlEntity> toSave =  new ArrayList<>();

        AtomicLong counter =
                new AtomicLong(deptDtlRepository.findMaxDetRowId(poid));

        requestDto.getDepartmentWiseSettings().forEach(dto -> {

            Long detRowId = counter.incrementAndGet();

            GlobalKpiMastersDeptDtlEntity entity =
                    GlobalKPIMasterMapper.toDeptDtlEntity(
                            dto, new GlobalKpiMastersDeptDtlEntity());

            entity.setId(GlobalKpiMastersDtlId.builder()
                    .transactionPoid(poid)
                    .detRowId(detRowId)
                    .build());

            entity.setCreatedBy(user);
            entity.setCreatedDate(now);

            toSave.add(entity);
        });
        deptDtlRepository.saveAll(toSave);
    }


    private GlobalKpiMastersEntity findHeaderEntityById(Long globalKpiMastersPoid) {
      return globalKpiMastersRepository.findById(globalKpiMastersPoid).orElseThrow(() -> new ResourceNotFoundException("Global KPI Master","Global KPI Master Poid ",globalKpiMastersPoid));
    }


    private String resolveAction(String rawAction) {
        String action = (rawAction == null || rawAction.trim().isEmpty()) ? ACTION_NOCHANGES : rawAction.trim().toUpperCase();
        return switch (action) {
            case "ISCREATED", "CREATED", "NEW" -> ACTION_ISCREATED;
            case "ISUPDATED", "UPDATED" -> ACTION_ISUPDATED;
            case "ISDELETED", "DELETED" -> ACTION_ISDELETED;
            default -> ACTION_NOCHANGES;
        };
    }
}
