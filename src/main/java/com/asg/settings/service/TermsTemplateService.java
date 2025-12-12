package com.asg.settings.service;

import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.settings.dto.TermsTemplateDtlDto;
import com.asg.settings.dto.TermsTemplateDto;
import com.asg.settings.dto.response.TemplateResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface TermsTemplateService {

    TermsTemplateDto getTermsTemplateAndClauses(Long termsPoid);

    void softDeleteByTermsPoid(Long termsPoid);

    List<TermsTemplateDtlDto> softDeleteClause(Long termsPoid, String clause);

    TemplateResponseDto addTemplateAndClause(TermsTemplateDto request, Long groupPoid, String loginUserPoid);

    TemplateResponseDto updateTemplateMetadata(Long termsPoid,TermsTemplateDto request,String loginUserPoid);

    TermsTemplateDtlDto addClause(Long termsPoid, TermsTemplateDtlDto dto);

    Map<String, Object> listTerms(String docId, FilterRequestDto request, Pageable pageable);
}
