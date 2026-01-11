package com.asg.settings.service.impl;

import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.entity.DocumentEntity;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.exception.ValidationException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.utility.ASGHelperUtils;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.settings.dto.DocumentLightDto;
import com.asg.settings.dto.TermsTemplateDtlDto;
import com.asg.settings.dto.TermsTemplateDto;
import com.asg.settings.dto.response.TemplateResponseDto;
import com.asg.settings.entity.TermsTemplateDtlEntity;
import com.asg.settings.entity.TermsTemplateEntity;
import com.asg.settings.entity.key.TermsTemplateDtlKey;
import com.asg.settings.repository.DocumentRepository;
import com.asg.common.lib.repository.GroupRepository;
import com.asg.settings.repository.TermsTemplateDtlRepository;
import com.asg.settings.repository.TermsTemplateRepository;
import com.asg.settings.service.TermsTemplateService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TermsTemplateServiceImpl implements TermsTemplateService {

    private final TermsTemplateRepository termsTemplateRepository;
    private final TermsTemplateDtlRepository termsTemplateDtlRepository;
    private final DocumentRepository documentRepository;
    private final GroupRepository groupRepository;
    private final LoggingService loggingService;

    @Autowired
    DocumentSearchService documentService;

    public Map<String, Object> listTerms(String docId, FilterRequestDto request, Pageable pageable) {

        String operator = documentService.resolveOperator(request);
        String isDeleted = documentService.resolveIsDeleted(request);
        List<FilterDto> filters = documentService.resolveFilters(request);

        RawSearchResult raw = documentService.search(docId, filters, operator, pageable, isDeleted,
                "TEMPLATE_NAME",   // label
                "TERMS_POID");    // value);

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    @Override
    public TemplateResponseDto updateTemplateMetadata(Long termsPoid, TermsTemplateDto request, String loginUserPoid) {
        request.setTermsPoid(termsPoid);
        TermsTemplateEntity existingTemplate = termsTemplateRepository.findByTermsPoid(termsPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Terms & Conditions", "termsPoid", termsPoid));

        String docId = UserContext.getDocumentId();
        TermsTemplateEntity oldTemplate = new TermsTemplateEntity();
        BeanUtils.copyProperties(existingTemplate, oldTemplate);

        if (!documentRepository.existsByDocId(request.getDocId())) {
            throw new ResourceNotFoundException("Document", "docId", request.getDocId());
        }

        existingTemplate.setTemplateName(request.getTemplateName());
        existingTemplate.setTermsCategory(request.getTermsCategory());
        existingTemplate.setActive(request.getActive() != null ? request.getActive() : "Y");
        existingTemplate.setSeqNo(request.getSeqNo());
        existingTemplate.setRemarks(request.getRemarks());
        existingTemplate.setDocId(request.getDocId());
        existingTemplate.setLastModifiedBy(loginUserPoid);
        existingTemplate.setLastModifiedDate(LocalDateTime.now());
        TermsTemplateEntity updatedTemplate = termsTemplateRepository.save(existingTemplate);

        String headerKey = updatedTemplate.getTermsPoid().toString();

        loggingService.logChanges(oldTemplate, updatedTemplate, TermsTemplateEntity.class, docId, headerKey, LogDetailsEnum.MODIFIED, "TERMS_POID");

        if (request.getClauses() != null && !request.getClauses().isEmpty()) {
            List<TermsTemplateDtlEntity> clausesToSave = new ArrayList<>();
            List<TermsTemplateDtlEntity> clausesToDelete = new ArrayList<>();

            for (TermsTemplateDtlDto clauseDto : request.getClauses()) {
                String actionType = clauseDto.getActionType();

                if ("isDeleted".equalsIgnoreCase(actionType)) {
                    // Handle DELETE/isDeleted: Mark clause as inactive
                    if (clauseDto.getDetRowId() != null) {
                        TermsTemplateDtlKey key = new TermsTemplateDtlKey();
                        key.setTermsPoid(termsPoid);
                        key.setDetRowId(clauseDto.getDetRowId());

                        // Fetch existing clause from database
                        TermsTemplateDtlEntity existingClause = termsTemplateDtlRepository.findById(key)
                                .orElseThrow(() -> new ResourceNotFoundException("Clause", "detRowId", clauseDto.getDetRowId()));

                        TermsTemplateDtlEntity oldClause = new TermsTemplateDtlEntity();
                        BeanUtils.copyProperties(existingClause, oldClause);

                        existingClause.setActive("N"); // Mark as inactive
                        existingClause.setLastModifiedBy(loginUserPoid);
                        existingClause.setLastModifiedDate(LocalDateTime.now());
                        clausesToDelete.add(existingClause);
                        loggingService.logChanges(oldClause, existingClause, TermsTemplateDtlEntity.class, docId, headerKey + "-" + clauseDto.getDetRowId(), LogDetailsEnum.MODIFIED, "TERMS_TEMPLATE_DTL");
                    }

                } else if ("noChange".equalsIgnoreCase(actionType)) {
                    // Skip processing for noChange
                    continue;
                } else {
                    // Handle CREATE and UPDATE
                    TermsTemplateDtlEntity clause = new TermsTemplateDtlEntity();
                    TermsTemplateDtlKey key = new TermsTemplateDtlKey();
                    key.setTermsPoid(termsPoid);

                    TermsTemplateDtlEntity oldClause = null;
                    boolean isCreate = ("isCreated".equalsIgnoreCase(actionType) || clauseDto.getDetRowId() == null);

                    if ("isCreated".equalsIgnoreCase(actionType) || clauseDto.getDetRowId() == null) {
                        // CREATE new clause (isCreated)
                        Long nextAvailableId = termsTemplateDtlRepository.getNextDetRowId(termsPoid);
                        key.setDetRowId(nextAvailableId);
                        clause.setCreatedBy(loginUserPoid);
                        clause.setCreatedDate(LocalDateTime.now());

                    } else {
                        // UPDATE existing clause (isUpdated)
                        key.setDetRowId(clauseDto.getDetRowId());
                        oldClause = termsTemplateDtlRepository.findById(key).orElse(null);
                        clause.setLastModifiedBy(loginUserPoid);
                        clause.setLastModifiedDate(LocalDateTime.now());
                    }

                    clause.setId(key);
                    clause.setClauseNo(clauseDto.getClauseNo());
                    clause.setClauseDetails(clauseDto.getClauseDetails());
                    clause.setActive(clauseDto.getActive());
                    clausesToSave.add(clause);
                    loggingService.logChanges(oldClause, clause, TermsTemplateDtlEntity.class, docId, headerKey + "-" + key.getDetRowId(), isCreate ? LogDetailsEnum.CREATED : LogDetailsEnum.MODIFIED, "TERMS_TEMPLATE_DTL");
                }
            }
            // Save all CREATE/UPDATE operations
            if (!clausesToSave.isEmpty()) {
                termsTemplateDtlRepository.saveAll(clausesToSave);
            }

            // Save all DELETE operations (mark as inactive)
            if (!clausesToDelete.isEmpty()) {
                termsTemplateDtlRepository.saveAll(clausesToDelete);
            }
        }
        TemplateResponseDto response = new TemplateResponseDto();
        response.setTermsPoid(updatedTemplate.getTermsPoid());
        response.setTemplateId(updatedTemplate.getTemplateId());
        response.setDocId(updatedTemplate.getDocId());
        return response;
    }

    @Transactional
    @Override
    public TemplateResponseDto addTemplateAndClause(TermsTemplateDto templateRequestDto, Long groupPoid, String loginUserPoid) {
        if (groupPoid == null) {
            throw new ValidationException("groupPoid should not be null");
        }
        boolean exists = groupRepository.existsById(groupPoid);

        if (!exists) {
            throw new ResourceNotFoundException("Group", "groupPoid", groupPoid);
        }

        if (!documentRepository.existsByDocId(templateRequestDto.getDocId())) {
            throw new ResourceNotFoundException("Document", "docId", templateRequestDto.getDocId());
        }

        TermsTemplateEntity template = new TermsTemplateEntity();
        template.setGroupPoid(groupPoid);
        template.setDocId(templateRequestDto.getDocId());
        template.setTemplateName(templateRequestDto.getTemplateName());
        template.setActive(templateRequestDto.getActive() != null ? templateRequestDto.getActive() : "N");
        template.setSeqNo(templateRequestDto.getSeqNo());
        template.setRemarks(templateRequestDto.getRemarks());
        template.setCreatedBy(loginUserPoid);
        template.setCreatedDate(LocalDateTime.now());
        template.setTermsCategory(templateRequestDto.getTermsCategory());
        template.setDeleted("N");
        TermsTemplateEntity savedTemplate = termsTemplateRepository.save(template);

        String docId = UserContext.getDocumentId();
        String key = savedTemplate.getTermsPoid().toString();

        loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, docId, key);

        if (templateRequestDto.getClauses() != null && !templateRequestDto.getClauses().isEmpty()) {
            Long nextAvailableId = termsTemplateDtlRepository.getNextDetRowId(savedTemplate.getTermsPoid());

            List<TermsTemplateDtlEntity> listOfClauses = new ArrayList<>();
            long currentId = nextAvailableId;

            for (TermsTemplateDtlDto clause : templateRequestDto.getClauses()) {
                TermsTemplateDtlEntity termsTemplateDtlEntity = new TermsTemplateDtlEntity();
                TermsTemplateDtlKey keyDtl = new TermsTemplateDtlKey();
                keyDtl.setTermsPoid(savedTemplate.getTermsPoid());
                keyDtl.setDetRowId(clause.getDetRowId() != null ? clause.getDetRowId() : currentId++);
                termsTemplateDtlEntity.setId(keyDtl);
                termsTemplateDtlEntity.setClauseNo(clause.getClauseNo());
                termsTemplateDtlEntity.setClauseDetails(clause.getClauseDetails());
                termsTemplateDtlEntity.setActive(clause.getActive());
                termsTemplateDtlEntity.setCreatedBy(loginUserPoid);
                termsTemplateDtlEntity.setCreatedDate(LocalDateTime.now());

                listOfClauses.add(termsTemplateDtlEntity);
                loggingService.logChanges(null, termsTemplateDtlEntity, TermsTemplateDtlEntity.class, docId, key + "-" + keyDtl.getDetRowId(), LogDetailsEnum.CREATED, "TERMS_TEMPLATE_DTL");
            }
            termsTemplateDtlRepository.saveAll(listOfClauses);
        }
        TemplateResponseDto response = new TemplateResponseDto();
        response.setTermsPoid(savedTemplate.getTermsPoid());
        response.setTemplateId(savedTemplate.getTemplateId());
        response.setDocId(savedTemplate.getDocId());
        return response;
    }

    @Override
    public TermsTemplateDto getTermsTemplateAndClauses(Long termsPoid) {
        TermsTemplateEntity termsTemplateEntity = termsTemplateRepository.findByTermsPoid(termsPoid).orElseThrow(() -> new ResourceNotFoundException("Terms & Conditions ", "termsPoid", termsPoid));

        TermsTemplateDto termsTemplateDto = mapToDto(termsTemplateEntity);
        List<TermsTemplateDtlEntity> termsTemplateDtlEntityList =
                termsTemplateDtlRepository.findAllById_TermsPoidAndActive(termsPoid, "Y");
        List<TermsTemplateDtlDto> clauses = termsTemplateDtlEntityList.stream()
                .map(this::mapToDtlDto)
                .collect(Collectors.toList());
        termsTemplateDto.setClauses(clauses);
        return termsTemplateDto;
    }

    @Transactional
    @Override
    public void softDeleteByTermsPoid(Long termsPoid) {
        TermsTemplateEntity termsTemplateEntity = termsTemplateRepository.findByTermsPoid(termsPoid).orElseThrow(() -> new ResourceNotFoundException("Terms & Conditions", "termsPoid", termsPoid));

        termsTemplateEntity.setActive("N");
        termsTemplateEntity.setDeleted("Y");
        termsTemplateEntity.setLastModifiedDate(LocalDateTime.now());
        termsTemplateEntity.setLastModifiedBy(ASGHelperUtils.getCurrentUser());

        termsTemplateRepository.save(termsTemplateEntity);
        String docId = UserContext.getDocumentId();
        String key = termsPoid.toString();

        loggingService.createLogSummaryEntry(LogDetailsEnum.DELETED, docId, key);
        loggingService.logSimpleFieldChange(TermsTemplateEntity.class, docId, key, "deleted", "N", "Y", "Template soft deleted");
        loggingService.logSimpleFieldChange(TermsTemplateEntity.class, docId, key, "active", "Y", "N", "Template soft deleted");
        deleteClausesByTermsPoid(termsPoid);
    }

    private void deleteClausesByTermsPoid(Long termsPoid) {
        List<TermsTemplateDtlEntity> termsTemplateDtlEntityList = termsTemplateDtlRepository.findAllById_TermsPoidAndActive(termsPoid, "Y");
        termsTemplateDtlEntityList.forEach(t -> t.setActive("N"));
        termsTemplateDtlRepository.saveAll(termsTemplateDtlEntityList);
    }

    @Transactional
    @Override
    public List<TermsTemplateDtlDto> softDeleteClause(Long termsPoid, String clauseNo) {
        TermsTemplateEntity termsTemplateEntity = termsTemplateRepository.findByTermsPoidAndActive(termsPoid, "Y");

        if (termsTemplateEntity == null) {
            throw new ResourceNotFoundException("Terms Template", "termsPoid", termsPoid);
        }

        List<TermsTemplateDtlEntity> clauses = termsTemplateDtlRepository
                .findDistinctById_TermsPoidAndClauseNoAndActive(termsPoid, clauseNo, "Y");

        if (clauses.isEmpty()) {
            throw new ResourceNotFoundException("Active Terms Template Detail not found", "clauseNo", clauseNo);
        }
        LocalDateTime now = LocalDateTime.now();
        List<TermsTemplateDtlDto> deletedClauses = new ArrayList<>();

        for (TermsTemplateDtlEntity clause : clauses) {
            TermsTemplateDtlEntity oldClause = new TermsTemplateDtlEntity();
            BeanUtils.copyProperties(clause, oldClause);

            clause.setActive("N");
            clause.setLastModifiedDate(now);
            TermsTemplateDtlEntity deletedClause = termsTemplateDtlRepository.save(clause);
            deletedClauses.add(this.mapToDtlDto(deletedClause));
            String docId = UserContext.getDocumentId();
            String key = termsPoid + "-" + clause.getId().getDetRowId();
            loggingService.createLogSummaryEntry(LogDetailsEnum.MODIFIED, docId, String.valueOf(termsPoid));
            loggingService.logChanges(oldClause, clause, TermsTemplateDtlEntity.class, docId, key, LogDetailsEnum.MODIFIED, "TERMS_TEMPLATE_DTL");
        }

        return deletedClauses;
    }

    @Override
    public TermsTemplateDtlDto addClause(Long termsPoid, TermsTemplateDtlDto dto) {
        dto.setTermsPoid(termsPoid);
        TermsTemplateEntity termsTemplateEntity = termsTemplateRepository.findByTermsPoidAndActive(termsPoid, "Y");

        if (termsTemplateEntity == null) {
            throw new ResourceNotFoundException("Terms & Conditions ", "termsPoid", termsPoid);
        }

        Long detRowId = termsTemplateDtlRepository.getNextDetRowId(termsPoid);
        TermsTemplateDtlKey key = new TermsTemplateDtlKey();
        key.setTermsPoid(termsPoid);
        key.setDetRowId(detRowId);

        TermsTemplateDtlEntity entity = new TermsTemplateDtlEntity();
        entity.setId(key);
        entity.setClauseNo(dto.getClauseNo());
        entity.setClauseDetails(dto.getClauseDetails());
        entity.setActive(dto.getActive() != null ? dto.getActive() : "Y");
        entity.setCreatedDate(LocalDateTime.now());
        entity.setLastModifiedDate(LocalDateTime.now());
        TermsTemplateDtlEntity saved = termsTemplateDtlRepository.save(entity);

        dto.setDetRowId(saved.getId().getDetRowId());
        return dto;
    }

    private TermsTemplateDto mapToDto(TermsTemplateEntity termsTemplateEntity) {
        TermsTemplateDto termsTemplateDto = new TermsTemplateDto();
        BeanUtils.copyProperties(termsTemplateEntity, termsTemplateDto);
        if (termsTemplateEntity.getDocId() != null) {
            termsTemplateDto.setDocument(getDocumentLightDto(termsTemplateEntity.getDocId()));
        }
        return termsTemplateDto;
    }

    private TermsTemplateDtlDto mapToDtlDto(TermsTemplateDtlEntity entity) {
        TermsTemplateDtlDto dto = new TermsTemplateDtlDto();
        BeanUtils.copyProperties(entity, dto);
        dto.setDetRowId(entity.getId().getDetRowId());
        dto.setTermsPoid(entity.getId().getTermsPoid());
        // Don't set actionType in response - it's only for request operations
        return dto;
    }

    private DocumentLightDto getDocumentLightDto(String docId) {
        DocumentEntity documentEntity = documentRepository.findByDocId(docId);
        DocumentLightDto documentLightDto = new DocumentLightDto();
        if (documentEntity == null) {
            documentLightDto.setDocId(docId);
            return documentLightDto;
        }
        BeanUtils.copyProperties(documentEntity, documentLightDto);
        return documentLightDto;
    }
}
