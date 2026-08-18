package com.asg.settings.repository;

import com.asg.settings.entity.EmailPdfTemplateMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailPdfTemplateRepository extends JpaRepository<EmailPdfTemplateMasterEntity, Long> {

    EmailPdfTemplateMasterEntity findByTemplatePoid(Long templatePoid);

    @Query("SELECT e FROM EmailPdfTemplateMasterEntity e WHERE e.deleted IS NULL OR e.deleted = 'N' ORDER BY e.seqNo")
    List<EmailPdfTemplateMasterEntity> findAllActive();

    @Query("SELECT e FROM EmailPdfTemplateMasterEntity e WHERE e.templateName = :templateName AND (e.deleted IS NULL OR e.deleted = 'N')")
    EmailPdfTemplateMasterEntity findByTemplateName(String templateName);

    @Query(value = "SELECT nextval('GLOBAL_EMAIL_PDF_TEMPLATE_SEQ')", nativeQuery = true)
    Long getNextSequenceValue();
}
