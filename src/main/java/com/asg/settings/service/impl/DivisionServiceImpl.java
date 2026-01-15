package com.asg.settings.service.impl;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.settings.dto.request.DivisionCreateRequest;
import com.asg.settings.dto.request.DivisionUpdateRequest;
import com.asg.settings.dto.response.DivisionResponse;
import com.asg.settings.entity.DivisionMasterEntity;
import com.asg.settings.repository.DivisionRepository;
import com.asg.settings.service.DivisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DivisionServiceImpl implements DivisionService {

    private final DivisionRepository divisionRepository;

    @Autowired
    private LoggingService loggingService;

    @Autowired
    private DocumentSearchService documentService;

    @Autowired
    private DocumentDeleteService documentDeleteService;

    @Override
    public DivisionResponse createDivision(DivisionCreateRequest request) {
        // validate active flag
        if (request.getActive() != null && !request.getActive().matches("Y|N")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Active must be Y or N");
        }

        DivisionMasterEntity entity = new DivisionMasterEntity();
        entity.setDivisionCode(request.getDivisionCode());
        entity.setDivisionName(request.getDivisionName());
        entity.setDescription(request.getRemarks());  // remarks -> description
        entity.setSeqNo(request.getSeqNo());
        entity.setActive(request.getActive() != null ? request.getActive() : "N");
        entity.setDeleted("N");
        entity.setCreatedBy(request.getCreatedBy());
        entity.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        DivisionMasterEntity saved = divisionRepository.save(entity);
        String docId = UserContext.getDocumentId();
        String key = saved.getDivisionId().toString();

        loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, docId, key);

        return mapToResponse(saved);
    }

    @Override
    public Optional<DivisionResponse> getDivisionById(Long id) {
        return divisionRepository.findById(id)
                .filter(this::isNotDeleted)
                .map(this::mapToResponse);
    }


    @Override
    public DivisionResponse updateDivision(Long id, DivisionUpdateRequest request) {
        DivisionMasterEntity entity = divisionRepository.findById(id)
                .filter(this::isNotDeleted)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Division not found"));

        DivisionMasterEntity oldEntity = new DivisionMasterEntity();
        BeanUtils.copyProperties(entity, oldEntity);

        if (request.getDivisionName() != null) {
            entity.setDivisionName(request.getDivisionName());
        }

        if (request.getRemarks() != null) {
            entity.setDescription(request.getRemarks());
        }

        if (request.getSeqNo() != null) {
            entity.setSeqNo(request.getSeqNo());
        }

        if (request.getActive() != null) {
            if (!request.getActive().matches("Y|N")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Active must be Y or N");
            }
            entity.setActive(request.getActive());
        }

        entity.setUpdatedBy(request.getUpdatedBy());
        entity.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        DivisionMasterEntity updated = divisionRepository.save(entity);

        String docId = UserContext.getDocumentId();
        String key = updated.getDivisionId().toString();

        loggingService.logChanges(oldEntity, updated, DivisionMasterEntity.class, docId, key, LogDetailsEnum.MODIFIED, "DIVISION_ID");
        return mapToResponse(updated);
    }

    @Override
    public void softDeleteDivision(Long id, DeleteReasonDto deleteReasonDto) {
        DivisionMasterEntity entity = divisionRepository.findById(id)
                .filter(this::isNotDeleted)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Division not found"));
        
        documentDeleteService.deleteDocument(
                id,
                "GLOBAL_DIVISION_MASTER",
                "DIVISION_POID",
                deleteReasonDto,
                null
        );
    }

    @Override
    public void activateDivision(Long id, String updatedBy) {
        DivisionMasterEntity entity = divisionRepository.findById(id)
                .filter(this::isNotDeleted)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Division not found"));
        entity.setActive("Y");
        entity.setUpdatedBy(updatedBy);
        entity.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        divisionRepository.save(entity);
    }

    @Override
    public void deactivateDivision(Long id, String updatedBy) {
        DivisionMasterEntity entity = divisionRepository.findById(id)
                .filter(this::isNotDeleted)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Division not found"));
        entity.setActive("N");
        entity.setUpdatedBy(updatedBy);
        entity.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        divisionRepository.save(entity);
    }

    @Override
    public boolean existsByDivisionCodeAndDeleted(String divisionCode, String deleted) {
        return divisionRepository.existsByDivisionCodeAndDeleted(divisionCode, deleted);
    }

    @Override
    public Map<String, Object> listDivisions(String documentId, FilterRequestDto request, Pageable pageable) {
        String operator = documentService.resolveOperator(request);
        String isDeleted = documentService.resolveIsDeleted(request);
        List<FilterDto> filters = documentService.resolveFilters(request);

        RawSearchResult raw = documentService.search(documentId, filters, operator, pageable, isDeleted,
                "DIVISION_NAME",   // label
                "DIVISION_POID");  // value - using correct primary key field

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    private DivisionResponse mapToResponse(DivisionMasterEntity entity) {
        DivisionResponse response = new DivisionResponse();
        response.setDivisionId(entity.getDivisionId());
        response.setDivisionCode(entity.getDivisionCode());
        response.setDivisionName(entity.getDivisionName());
        response.setRemarks(entity.getDescription()); // map description -> remarks
        response.setSeqNo(entity.getSeqNo());
        response.setActive(entity.getActive()); // map active (Y/N string)
        response.setDeleted(entity.getDeleted());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedBy(entity.getUpdatedBy());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    private boolean isNotDeleted(DivisionMasterEntity entity) {
        String deleted = entity.getDeleted();
        return deleted == null || deleted.equals("N");
    }
}

