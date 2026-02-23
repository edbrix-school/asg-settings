package com.asg.settings.utility;

import com.asg.common.lib.security.util.UserContext;
import com.asg.settings.dto.request.GlobalKPIMastersRequestDto;
import com.asg.settings.dto.response.*;
import com.asg.settings.entity.*;
import java.util.List;


public class GlobalKPIMasterMapper {

    // HEADER MAPPER
    public static GlobalKPIMastersResponseDto toDto(GlobalKpiMastersEntity entity, List<GlobalKpiMastersCompanyDtlResponseDto> companyDetails, List<GlobalKpiMastersEmpDtlResponseDto> employeeDetails
            , List<GlobalKpiMastersDeptDtlResponseDto> departmentDetails, List<GlobalKpiMastersLineDtlResponseDto> lineDetails) {
        if (entity == null) return null;

        return GlobalKPIMastersResponseDto.builder()
                .globalKpiMastersPoid(entity.getGlobalKpiMastersPoid())
                .groupPoid(entity.getGroupPoid())
                .kpiCode(entity.getKpiCode())
                .kpiName(entity.getKpiName())
                .departments(entity.getDepartments())
                .kpiUnit(entity.getKpiUnit())
                .frequency(entity.getFrequency())
                .lastExecuted(entity.getLastExecuted())
                .sqlProcedure(entity.getSqlProcedure())
                .sqlQueryLineKpi(entity.getSqlQueryLineKpi())
                .sqlQueryCompanyKpi(entity.getSqlQueryCompanyKpi())
                .sqlQueryEmpKpi(entity.getSqlQueryEmpKpi())
                .sqlQueryDeptKpi(entity.getSqlQueryDeptKpi())
                .active(entity.getActive())
                .seqNo(entity.getSeqNo())
                .createdBy(entity.getCreatedBy())
                .createdDate(entity.getCreatedDate())
                .lastModifiedBy(entity.getLastModifiedBy())
                .lastModifiedDate(entity.getLastModifiedDate())
                .deleted(entity.getDeleted())
                .lineWiseSettings(lineDetails)
                .companyWiseSettings(companyDetails)
                .employeeWiseSettings(employeeDetails)
                .departmentWiseSettings(departmentDetails)
                .build();
    }

    public static GlobalKpiMastersEntity toHeaderEntity(GlobalKPIMastersResponseDto dto) {
        if (dto == null) return null;

        return GlobalKpiMastersEntity.builder()
                .globalKpiMastersPoid(dto.getGlobalKpiMastersPoid())
                .groupPoid(dto.getGroupPoid())
                .kpiCode(dto.getKpiCode())
                .kpiName(dto.getKpiName())
                .departments(dto.getDepartments())
                .kpiUnit(dto.getKpiUnit())
                .frequency(dto.getFrequency())
                .lastExecuted(dto.getLastExecuted())
                .sqlProcedure(dto.getSqlProcedure())
                .sqlQueryLineKpi(dto.getSqlQueryLineKpi())
                .sqlQueryCompanyKpi(dto.getSqlQueryCompanyKpi())
                .sqlQueryEmpKpi(dto.getSqlQueryEmpKpi())
                .sqlQueryDeptKpi(dto.getSqlQueryDeptKpi())
                .active(dto.getActive())
                .seqNo(dto.getSeqNo())
                .createdBy(dto.getCreatedBy())
                .createdDate(dto.getCreatedDate())
                .lastModifiedBy(dto.getLastModifiedBy())
                .lastModifiedDate(dto.getLastModifiedDate())
                .deleted(dto.getDeleted())
                .build();
    }

    public static GlobalKpiMastersEntity toHeaderCreateEntity(
            GlobalKPIMastersRequestDto requestDto, GlobalKpiMastersEntity entity
    ) {

        if (requestDto == null) {
            return null;
        }

        entity.setGroupPoid(UserContext.getGroupPoid());
        entity.setKpiCode(requestDto.getKpiCode());
        entity.setKpiName(requestDto.getKpiName());
        entity.setDepartments(requestDto.getDepartments());
        entity.setKpiUnit(requestDto.getKpiUnit());
        entity.setFrequency(requestDto.getFrequency());
        entity.setSqlProcedure(requestDto.getSqlProcedure());
        entity.setSqlQueryLineKpi(requestDto.getSqlQueryLineKpi());
        entity.setSqlQueryCompanyKpi(requestDto.getSqlQueryCompanyKpi());
        entity.setSqlQueryEmpKpi(requestDto.getSqlQueryEmpKpi());
        entity.setSqlQueryDeptKpi(requestDto.getSqlQueryDeptKpi());

        entity.setActive(requestDto.getActive() != null ? requestDto.getActive() : "Y");
        entity.setSeqNo(requestDto.getSeqNo());
        entity.setDeleted("N");
        return entity;
    }

    //COMPANY DTL MAPPER
    public static GlobalKpiMastersCompanyDtlResponseDto toCompanyDtlDto(GlobalKpiMastersCompanyDtlEntity entity) {
        if (entity == null) return null;

        return GlobalKpiMastersCompanyDtlResponseDto.builder()
                .globalKpiMastersPoid(entity.getTransactionPoid())
                .detRowId(entity.getDetRowId())
                .companyPoid(entity.getCompanyPoid())
                .targetValue(entity.getTargetValue())
                .createdBy(entity.getCreatedBy())
                .createdDate(entity.getCreatedDate())
                .lastModifiedBy(entity.getLastModifiedBy())
                .lastModifiedDate(entity.getLastModifiedDate())
                .build();
    }

    public static GlobalKpiMastersCompanyDtlEntity toCompanyDtlEntity(
            GlobalKpiMastersCompanyDtlResponseDto dto, GlobalKpiMastersCompanyDtlEntity entity) {

        if (dto == null) {
            return null;
        }
        entity.setCompanyPoid(dto.getCompanyPoid());
        entity.setTargetValue(dto.getTargetValue());
        return entity;
    }

    public static List<GlobalKpiMastersCompanyDtlResponseDto> toCompanyDtlDtoList(
            List<GlobalKpiMastersCompanyDtlEntity> entities) {

        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(GlobalKPIMasterMapper::toCompanyDtlDto)
                .toList();
    }

    //DEPT DTL MAPPER
    public static GlobalKpiMastersDeptDtlResponseDto toDeptDtlDto(GlobalKpiMastersDeptDtlEntity entity) {
        if (entity == null) return null;

        return GlobalKpiMastersDeptDtlResponseDto.builder()
                .globalKpiMastersPoid(entity.getTransactionPoid())
                .detRowId(entity.getDetRowId())
                .deptPoid(entity.getDeptPoid())
                .targetValue(entity.getTargetValue())
                .createdBy(entity.getCreatedBy())
                .createdDate(entity.getCreatedDate())
                .lastModifiedBy(entity.getLastModifiedBy())
                .lastModifiedDate(entity.getLastModifiedDate())
                .build();
    }

    public static GlobalKpiMastersDeptDtlEntity toDeptDtlEntity(
            GlobalKpiMastersDeptDtlResponseDto dto, GlobalKpiMastersDeptDtlEntity entity) {

        if (dto == null) {
            return null;
        }
        entity.setDeptPoid(dto.getDeptPoid());
        entity.setTargetValue(dto.getTargetValue());
        return entity;
    }

    public static List<GlobalKpiMastersDeptDtlResponseDto> toDeptDtlDtoList(
            List<GlobalKpiMastersDeptDtlEntity> entities) {

        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(GlobalKPIMasterMapper::toDeptDtlDto)
                .toList();
    }

    // EMP DTL MAPPER

    public static GlobalKpiMastersEmpDtlResponseDto toEmpDtlDto(
            GlobalKpiMastersEmpDtlEntity entity) {

        if (entity == null) return null;

        return GlobalKpiMastersEmpDtlResponseDto.builder()
                .globalKpiMastersPoid(entity.getTransactionPoid())
                .detRowId(entity.getDetRowId())
                .empPoid(entity.getEmpPoid())
                .targetValue(entity.getTargetValue())
                .createdBy(entity.getCreatedBy())
                .createdDate(entity.getCreatedDate())
                .lastModifiedBy(entity.getLastModifiedBy())
                .lastModifiedDate(entity.getLastModifiedDate())
                .build();
    }

    public static GlobalKpiMastersEmpDtlEntity toEmpDtlEntity(
            GlobalKpiMastersEmpDtlResponseDto dto, GlobalKpiMastersEmpDtlEntity entity) {

        if (dto == null) {
            return null;
        }
        entity.setEmpPoid(dto.getEmpPoid());
        entity.setTargetValue(dto.getTargetValue());
        return entity;
    }


    public static List<GlobalKpiMastersEmpDtlResponseDto> toEmpDtlDtoList(
            List<GlobalKpiMastersEmpDtlEntity> entities) {

        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(GlobalKPIMasterMapper::toEmpDtlDto)
                .toList();
    }

    // LINE DTL MAPPER

    public static GlobalKpiMastersLineDtlResponseDto toLineDtlDto(
            GlobalKpiMastersLineDtlEntity entity) {

        if (entity == null) return null;

        return GlobalKpiMastersLineDtlResponseDto.builder()
                .globalKpiMastersPoid(entity.getTransactionPoid())
                .detRowId(entity.getDetRowId())
                .linePoid(entity.getLinePoid())
                .targetValue(entity.getTargetValue())
                .createdBy(entity.getCreatedBy())
                .createdDate(entity.getCreatedDate())
                .lastModifiedBy(entity.getLastModifiedBy())
                .lastModifiedDate(entity.getLastModifiedDate())
                .build();
    }

    public static GlobalKpiMastersLineDtlEntity toLineDtlEntity(
            GlobalKpiMastersLineDtlEntity entity,
            GlobalKpiMastersLineDtlResponseDto dto) {

        if (entity == null || dto == null) {
            return null;
        }

        entity.setLinePoid(dto.getLinePoid());
        entity.setTargetValue(dto.getTargetValue());
        return entity;
    }

    public static List<GlobalKpiMastersLineDtlResponseDto> toLineDtlDtoList(
            List<GlobalKpiMastersLineDtlEntity> entities) {

        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(GlobalKPIMasterMapper::toLineDtlDto)
                .toList();
    }

}
