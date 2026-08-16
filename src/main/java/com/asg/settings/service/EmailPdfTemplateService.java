package com.asg.settings.service;

import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.settings.dto.EmailPdfTemplateDto;
import com.asg.settings.entity.EmailPdfTemplateMasterEntity;
import com.asg.settings.repository.EmailPdfTemplateRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static com.asg.common.lib.utility.ASGHelperUtils.getCurrentUser;

@Service
public class EmailPdfTemplateService {

    @Autowired
    private EmailPdfTemplateRepository repository;

    @Autowired
    private DocumentSearchService documentService;

    @Autowired
    private LoggingService loggingService;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Autowired
    private com.asg.common.lib.service.DocumentDeleteService documentDeleteService;

    public Map<String, Object> listTemplates(String docId, FilterRequestDto request, Pageable pageable) {
        String operator = documentService.resolveOperator(request);
        String isDeleted = documentService.resolveIsDeleted(request);
        List<FilterDto> filters = documentService.resolveFilters(request);

        RawSearchResult raw = documentService.search(docId, filters, operator, pageable, isDeleted,
                "TEMPLATE_NAME",   // label
                "TEMPLATE_POID");    // value

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    public EmailPdfTemplateDto getTemplateById(Long templatePoid) {
        EmailPdfTemplateMasterEntity entity = repository.findByTemplatePoid(templatePoid);
        if (entity == null) {
            throw new ResourceNotFoundException("EmailPdfTemplate", "templatePoid", templatePoid.toString());
        }
        EmailPdfTemplateDto dto = new EmailPdfTemplateDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Transactional
    public EmailPdfTemplateMasterEntity createOrUpdateTemplate(EmailPdfTemplateDto dto) {
        EmailPdfTemplateMasterEntity oldEntity = null;
        EmailPdfTemplateMasterEntity entity;

        if (dto.getTemplatePoid() != null) {
            entity = repository.findByTemplatePoid(dto.getTemplatePoid());
            if (entity == null) {
                throw new ResourceNotFoundException("EmailPdfTemplate", "templatePoid", dto.getTemplatePoid().toString());
            }
            oldEntity = new EmailPdfTemplateMasterEntity();
            BeanUtils.copyProperties(entity, oldEntity);
        } else {
            entity = new EmailPdfTemplateMasterEntity();
            entity.setTemplatePoid(repository.getNextSequenceValue());
            entity.setCreatedBy(getCurrentUser());
            entity.setCreatedDate(java.time.LocalDateTime.now());
            entity.setDeleted("N");
        }

        entity.setGroupPoid(UserContext.getGroupPoid());
        entity.setTemplateDocId(dto.getTemplateDocId());
        entity.setTemplateName(dto.getTemplateName());
        entity.setType(dto.getType());
        entity.setEmailSubject(dto.getEmailSubject());
        entity.setEmailContent(dto.getEmailContent());
        entity.setPdfContent(dto.getPdfContent());
        entity.setFieldsToUse(dto.getFieldsToUse());
        entity.setSqlQuery(dto.getSqlQuery());
        entity.setSeqNo(dto.getSeqNo());
        entity.setActive(dto.getActive() != null ? dto.getActive() : "Y");
        entity.setRemarks(dto.getRemarks());
        entity.setLastModifiedBy(getCurrentUser());
        entity.setLastModifiedDate(java.time.LocalDateTime.now());

        EmailPdfTemplateMasterEntity saved = repository.save(entity);
        repository.flush();
        entityManager.clear();
        
        saved = repository.findByTemplatePoid(entity.getTemplatePoid());
        if (saved == null) {
            saved = entityManager.createQuery(
                "SELECT e FROM EmailPdfTemplateMasterEntity e WHERE e.templatePoid = (SELECT MAX(e2.templatePoid) FROM EmailPdfTemplateMasterEntity e2)",
                EmailPdfTemplateMasterEntity.class
            ).getSingleResult();
        }

        String docId = UserContext.getDocumentId();
        String key = saved.getTemplatePoid().toString();

        if (oldEntity == null) {
            loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, docId, key);
        } else {
            loggingService.logChanges(oldEntity, saved, EmailPdfTemplateMasterEntity.class, docId, key, LogDetailsEnum.MODIFIED, "TEMPLATE_POID");
        }

        return saved;
    }

    @Transactional
    public void softDeleteTemplate(Long templatePoid, com.asg.common.lib.dto.DeleteReasonDto deleteReasonDto) {
        EmailPdfTemplateMasterEntity entity = repository.findByTemplatePoid(templatePoid);
        if (entity == null) {
            throw new ResourceNotFoundException("EmailPdfTemplate", "templatePoid", templatePoid.toString());
        }
        
        documentDeleteService.deleteDocument(
                templatePoid,
                "GLOBAL_EMAIL_PDF_TEMPLATE_MST",
                "TEMPLATE_POID",
                deleteReasonDto,
                null
        );
    }
}
