package com.asg.settings.service;

import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.repository.GroupRepository;
import com.asg.settings.dto.TermsTemplateDtlDto;
import com.asg.settings.dto.TermsTemplateDto;
import com.asg.settings.dto.response.TemplateResponseDto;
import com.asg.settings.entity.TermsTemplateDtlEntity;
import com.asg.settings.entity.TermsTemplateEntity;
import com.asg.settings.entity.key.TermsTemplateDtlKey;
import com.asg.settings.repository.DocumentRepository;
import com.asg.settings.repository.TermsTemplateDtlRepository;
import com.asg.settings.repository.TermsTemplateRepository;
import com.asg.settings.service.impl.TermsTemplateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TermsTemplateServiceImplTest {

    @Mock
    private TermsTemplateRepository termsTemplateRepository;

    @Mock
    private TermsTemplateDtlRepository termsTemplateDtlRepository;

    @InjectMocks
    private TermsTemplateServiceImpl termsTemplateService;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private GroupRepository groupRepository;

    private TermsTemplateEntity testTemplate;
    private TermsTemplateDtlEntity testClause1;
    private TermsTemplateDtlEntity testClause2;

    @BeforeEach
    void setUp() {
        testTemplate = new TermsTemplateEntity();
        testTemplate.setTermsPoid(1L);
        testTemplate.setTemplateId("TEMP-001");
        testTemplate.setTemplateName("Standard Terms");
        testTemplate.setActive("Y");
        testTemplate.setTermsCategory("GENERAL");

        TermsTemplateDtlKey key1 = new TermsTemplateDtlKey();
        key1.setTermsPoid(1L);
        key1.setDetRowId(1L);

        testClause1 = new TermsTemplateDtlEntity();
        testClause1.setId(key1);
        testClause1.setClauseNo("1");
        testClause1.setClauseDetails("First clause details");
        testClause1.setActive("Y");

        TermsTemplateDtlKey key2 = new TermsTemplateDtlKey();
        key2.setTermsPoid(1L);
        key2.setDetRowId(2L);

        testClause2 = new TermsTemplateDtlEntity();
        testClause2.setId(key2);
        testClause2.setClauseNo("2");
        testClause2.setClauseDetails("Second clause details");
        testClause2.setActive("Y");
    }

    @Test
    void updateTemplateMetadata_SuccessWithoutClauses() {
        TermsTemplateDto requestDto = new TermsTemplateDto();
        requestDto.setDocId("DOC-001");
        requestDto.setTemplateName("Updated Template");
        requestDto.setTermsCategory("UPDATED");
        requestDto.setActive("N");
        requestDto.setRemarks("Updated remarks");

        when(termsTemplateRepository.findByTermsPoid(1L)).thenReturn(Optional.of(testTemplate));
        when(documentRepository.existsByDocId("DOC-001")).thenReturn(true);
        when(termsTemplateRepository.save(any(TermsTemplateEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        TemplateResponseDto response = termsTemplateService.updateTemplateMetadata(1L, requestDto, "user-123");

        assertThat(response).isNotNull();
        assertThat(response.getTemplateId()).isEqualTo("TEMP-001");
        assertThat(response.getTermsPoid()).isEqualTo(1L);

        verify(termsTemplateRepository).findByTermsPoid(1L);
        verify(termsTemplateRepository).save(any(TermsTemplateEntity.class));
    }

    @Test
    void addTemplateAndClause_WithValidData_ShouldSaveTemplateAndClauses() {
        TermsTemplateDto requestDto = new TermsTemplateDto();
        requestDto.setDocId("DOC-001");
        requestDto.setTemplateName("Test Template");
        requestDto.setActive("Y");
        requestDto.setTermsCategory("TEST");

        TermsTemplateDtlDto clause1 = new TermsTemplateDtlDto();
        clause1.setClauseNo("001");
        clause1.setClauseDetails("Test Clause 1");
        clause1.setActive("Y");

        requestDto.setClauses(Arrays.asList(clause1));

        when(groupRepository.existsById(1L)).thenReturn(true);
        when(documentRepository.existsByDocId("DOC-001")).thenReturn(true);
        when(termsTemplateRepository.save(any(TermsTemplateEntity.class)))
                .thenAnswer(invocation -> {
                    TermsTemplateEntity template = invocation.getArgument(0);
                    template.setTermsPoid(1L);
                    template.setTemplateId("TEMP-001");
                    return template;
                });
        when(termsTemplateDtlRepository.getNextDetRowId(1L)).thenReturn(1L);

        TemplateResponseDto response = termsTemplateService.addTemplateAndClause(requestDto, 1L, "testUser");

        assertNotNull(response);
        assertEquals(1L, response.getTermsPoid());
        assertEquals("TEMP-001", response.getTemplateId());
        assertEquals("DOC-001", response.getDocId());

        verify(groupRepository).existsById(1L);
        verify(documentRepository).existsByDocId("DOC-001");
        verify(termsTemplateRepository).save(any(TermsTemplateEntity.class));
        verify(termsTemplateDtlRepository).saveAll(anyList());
    }

    @Test
    void addTemplateAndClause_WithNonExistentGroup_ShouldThrowException() {
        TermsTemplateDto requestDto = new TermsTemplateDto();
        requestDto.setDocId("DOC-001");
        requestDto.setTemplateName("Test Template");
        requestDto.setActive("Y");
        requestDto.setTermsCategory("TEST");

        when(groupRepository.existsById(999L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> termsTemplateService.addTemplateAndClause(requestDto, 999L, "testUser")
        );

        assertEquals("Group not found with groupPoid : '999'", exception.getMessage());
        verify(termsTemplateRepository, never()).save(any(TermsTemplateEntity.class));
    }

    @Test
    void getTermsTemplateAndClauses_WithValidTermsPoid_ShouldReturnTemplateWithClauses() {
        when(termsTemplateRepository.findByTermsPoidAndActive(1L, "Y")).thenReturn(testTemplate);
        when(termsTemplateDtlRepository.findAllById_TermsPoidAndActive(1L, "Y"))
                .thenReturn(Arrays.asList(testClause1, testClause2));

        TermsTemplateDto result = termsTemplateService.getTermsTemplateAndClauses(1L);

        assertThat(result).isNotNull();
        assertThat(result.getTermsPoid()).isEqualTo(1L);
        assertThat(result.getTemplateName()).isEqualTo("Standard Terms");
        assertThat(result.getClauses()).hasSize(2);
    }

    @Test
    void getTermsTemplateAndClauses_WithInvalidTermsPoid_ShouldThrowException() {
        when(termsTemplateRepository.findByTermsPoidAndActive(999L, "Y")).thenReturn(null);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> termsTemplateService.getTermsTemplateAndClauses(999L)
        );

        assertEquals("Terms & Conditions  not found with termsPoid : '999'", exception.getMessage());
    }

    @Test
    void softDeleteByTermsPoid_WithValidTermsPoid_ShouldMarkAsDeleted() {
        when(termsTemplateRepository.findByTermsPoid(1L)).thenReturn(Optional.of(testTemplate));
        when(termsTemplateDtlRepository.findAllById_TermsPoidAndActive(1L, "Y"))
                .thenReturn(Arrays.asList(testClause1, testClause2));

        termsTemplateService.softDeleteByTermsPoid(1L);

        assertThat(testTemplate.getActive()).isEqualTo("N");
        assertThat(testTemplate.getDeleted()).isEqualTo("Y");
        verify(termsTemplateDtlRepository).saveAll(anyList());
    }

    @Test
    void addClause_WithValidData_ShouldAddNewClause() {
        TermsTemplateDtlDto newClauseDto = new TermsTemplateDtlDto();
        newClauseDto.setClauseNo("003");
        newClauseDto.setClauseDetails("New clause details");
        newClauseDto.setActive("Y");

        when(termsTemplateRepository.findByTermsPoidAndActive(1L, "Y")).thenReturn(testTemplate);
        when(termsTemplateDtlRepository.getNextDetRowId(1L)).thenReturn(3L);
        when(termsTemplateDtlRepository.save(any(TermsTemplateDtlEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TermsTemplateDtlDto result = termsTemplateService.addClause(1L, newClauseDto);

        assertThat(result).isNotNull();
        assertThat(result.getClauseNo()).isEqualTo("003");
        verify(termsTemplateDtlRepository).save(any(TermsTemplateDtlEntity.class));
    }
}
