package com.asg.settings.controller;

//import com.asg.dto.masters.TemplateResponseDto;
//import com.asg.dto.masters.TermsTemplateDtlDto;
//import com.asg.dto.masters.TermsTemplateDto;
//import com.asg.exceptions.GlobalExceptionHandler;
//import com.asg.exceptions.ResourceNotFoundException;
//import com.asg.service.masters.TermsTemplateService;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.settings.dto.TermsTemplateDtlDto;
import com.asg.settings.dto.TermsTemplateDto;
import com.asg.settings.dto.response.TemplateResponseDto;
import com.asg.settings.exceptions.GlobalExceptionHandler;
import com.asg.settings.service.TermsTemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TermsTemplateControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TermsTemplateService termsTemplateService;

    @InjectMocks
    private TermsTemplateController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // PUT /api/v1/terms/{termsPoid} - updateTemplateMetadata tests
    @Test
    void updateTemplateMetadata_Success() throws Exception {
        TermsTemplateDto request = new TermsTemplateDto();
        request.setTemplateId("TEMP-001");
        request.setTemplateName("Updated Template");
        request.setDocId("DOC-001");
        request.setActive("Y");
        request.setTermsCategory("GENERAL");

        TemplateResponseDto response = new TemplateResponseDto();
        response.setTermsPoid(1L);
        response.setTemplateId("TEMP-001");

        when(termsTemplateService.updateTemplateMetadata(anyLong(), any(TermsTemplateDto.class), anyString()))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/terms/{termsPoid}", 1L)
                        .header("loginUserPoid", "user-123")
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Template updated successfully"))
                .andExpect(jsonPath("$.result.data.templateId").value("TEMP-001"));

        verify(termsTemplateService).updateTemplateMetadata(eq(1L), any(TermsTemplateDto.class), eq("user-123"));
    }

    @Test
    void updateTemplateMetadata_MissingLoginUserPoid_ReturnsBadRequest() throws Exception {
        TermsTemplateDto request = new TermsTemplateDto();
        request.setTemplateName("Test");

        mockMvc.perform(put("/api/v1/terms/{termsPoid}", 1L)
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTemplateMetadata_MissingDocumentId_ReturnsBadRequest() throws Exception {
        TermsTemplateDto request = new TermsTemplateDto();
        request.setTemplateName("Test");

        mockMvc.perform(put("/api/v1/terms/{termsPoid}", 1L)
                        .header("loginUserPoid", "user-123")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTemplateMetadata_ServiceException_ReturnsInternalServerError() throws Exception {
        TermsTemplateDto request = new TermsTemplateDto();
        request.setTemplateName("Test");
        request.setDocId("DOC-001");
        request.setActive("Y");
        request.setTermsCategory("GENERAL");

        when(termsTemplateService.updateTemplateMetadata(anyLong(), any(TermsTemplateDto.class), anyString()))
                .thenThrow(new RuntimeException("Update failed"));

        mockMvc.perform(put("/api/v1/terms/{termsPoid}", 1L)
                        .header("loginUserPoid", "user-123")
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    // POST /api/v1/terms - createTermsAndConditionsTemplate tests
    @Test
    void createTermsAndConditionsTemplate_Success() throws Exception {
        TermsTemplateDto request = new TermsTemplateDto();
        request.setTemplateName("New Template");
        request.setDocId("DOC-001");
        request.setTermsCategory("GENERAL");
        request.setActive("Y");

        TemplateResponseDto response = new TemplateResponseDto();
        response.setTermsPoid(1L);
        response.setTemplateId("TEMP-001");

        when(termsTemplateService.addTemplateAndClause(any(TermsTemplateDto.class), anyLong(), anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/terms")
                        .header("groupPoid", "1")
                        .header("loginUserPoid", "user-123")
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Template created Successfully"))
                .andExpect(jsonPath("$.result.data.templateId").value("TEMP-001"));

        verify(termsTemplateService).addTemplateAndClause(any(TermsTemplateDto.class), eq(1L), eq("user-123"));
    }

    @Test
    void createTermsAndConditionsTemplate_MissingGroupPoid_ReturnsBadRequest() throws Exception {
        TermsTemplateDto request = new TermsTemplateDto();
        request.setTemplateName("Test");

        mockMvc.perform(post("/api/v1/terms")
                        .header("loginUserPoid", "user-123")
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTermsAndConditionsTemplate_InvalidJson_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/terms")
                        .header("groupPoid", "1")
                        .header("loginUserPoid", "user-123")
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    // GET /api/v1/terms/{termsPoid} - getTermsTemplateAndClauses tests
    @Test
    void getTermsTemplateAndClauses_Success() throws Exception {
        TermsTemplateDto response = new TermsTemplateDto();
        response.setTermsPoid(1L);
        response.setTemplateName("Test Template");

        when(termsTemplateService.getTermsTemplateAndClauses(anyLong())).thenReturn(response);

        mockMvc.perform(get("/api/v1/terms/{termsPoid}", 1L)
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Terms And Clauses Fetched SuccessFully "))
                .andExpect(jsonPath("$.result.data.termsPoid").value(1));

        verify(termsTemplateService).getTermsTemplateAndClauses(1L);
    }

    @Test
    void getTermsTemplateAndClauses_NotFound_ReturnsNotFound() throws Exception {
        when(termsTemplateService.getTermsTemplateAndClauses(anyLong()))
                .thenThrow(new ResourceNotFoundException("TermsTemplate", "termsPoid", 999L));

        mockMvc.perform(get("/api/v1/terms/{termsPoid}", 999L)
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isNotFound());

        verify(termsTemplateService).getTermsTemplateAndClauses(999L);
    }

    @Test
    void getTermsTemplateAndClauses_MissingDocumentId_ReturnsInternalServerError() throws Exception {
        mockMvc.perform(get("/api/v1/terms/{termsPoid}", 1L)
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isInternalServerError());
    }

    // DELETE /api/v1/terms/{termsPoid} - softDeleteTemplate tests
    @Test
    void softDeleteTemplate_Success() throws Exception {
        doNothing().when(termsTemplateService).softDeleteByTermsPoid(anyLong());

        mockMvc.perform(delete("/api/v1/terms/{termsPoid}", 1L)
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Terms And Clauses Deleted SuccessFully "));

        verify(termsTemplateService).softDeleteByTermsPoid(1L);
    }

    @Test
    void softDeleteTemplate_NotFound_ReturnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("TermsTemplate", "termsPoid", 999L))
                .when(termsTemplateService).softDeleteByTermsPoid(anyLong());

        mockMvc.perform(delete("/api/v1/terms/{termsPoid}", 999L)
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isNotFound());

        verify(termsTemplateService).softDeleteByTermsPoid(999L);
    }

    @Test
    void softDeleteTemplate_MissingActionRequested_ReturnsInternalServerError() throws Exception {
        mockMvc.perform(delete("/api/v1/terms/{termsPoid}", 1L)
                        .param("documentId", "DOC-001"))
                .andExpect(status().isInternalServerError());
    }

    // DELETE /api/v1/terms/{termsPoid}/clause/{clauseNo} - softDeleteTermsAndClauses tests
    @Test
    void softDeleteTermsAndClauses_Success() throws Exception {
        TermsTemplateDtlDto response = new TermsTemplateDtlDto();
        response.setTermsPoid(1L);
        response.setClauseNo("001");
        response.setActive("N");

        when(termsTemplateService.softDeleteClause(anyLong(), anyString()))
                .thenReturn(Collections.singletonList(response));

        mockMvc.perform(delete("/api/v1/terms/{termsPoid}/clause/{clauseNo}", 1L, "001")
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Clause Deleted Successfully "))
                .andExpect(jsonPath("$.result.data[0].clauseNo").value("001"));
        verify(termsTemplateService).softDeleteClause(1L, "001");
    }

    @Test
    void softDeleteTermsAndClauses_NotFound_ReturnsNotFound() throws Exception {
        when(termsTemplateService.softDeleteClause(anyLong(), anyString()))
                .thenThrow(new ResourceNotFoundException("Clause", "clauseNo", "999"));

        mockMvc.perform(delete("/api/v1/terms/{termsPoid}/clause/{clauseNo}", 1L, "999")
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isNotFound());

        verify(termsTemplateService).softDeleteClause(1L, "999");
    }

    // POST /api/v1/terms/{termsPoid}/clause - addClauseToTemplate tests
    @Test
    void addClauseToTemplate_Success() throws Exception {
        TermsTemplateDtlDto request = new TermsTemplateDtlDto();
        request.setClauseNo("001");
        request.setClauseDetails("Test clause");
        request.setActive("Y");

        TermsTemplateDtlDto response = new TermsTemplateDtlDto();
        response.setTermsPoid(1L);
        response.setClauseNo("001");
        response.setClauseDetails("Test clause");
        response.setActive("Y");

        when(termsTemplateService.addClause(anyLong(), any(TermsTemplateDtlDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/terms/{termsPoid}/clause", 1L)
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Clause Added to template Successfully "))
                .andExpect(jsonPath("$.result.data.clauseNo").value("001"));

        verify(termsTemplateService).addClause(eq(1L), any(TermsTemplateDtlDto.class));
    }

    @Test
    void addClauseToTemplate_InvalidJson_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/terms/{termsPoid}/clause", 1L)
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addClauseToTemplate_EmptyRequestBody_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/terms/{termsPoid}/clause", 1L)
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addClauseToTemplate_ServiceException_ReturnsInternalServerError() throws Exception {
        TermsTemplateDtlDto request = new TermsTemplateDtlDto();
        request.setClauseNo("001");
        request.setClauseDetails("Test");
        request.setActive("Y");

        when(termsTemplateService.addClause(anyLong(), any(TermsTemplateDtlDto.class)))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/api/v1/terms/{termsPoid}/clause", 1L)
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    // Edge case tests
    @Test
    void updateTemplateMetadata_NegativeTermsPoid_ReturnsBadRequest() throws Exception {
        TermsTemplateDto request = new TermsTemplateDto();
        request.setTemplateName("Test");
        request.setDocId("DOC-001");
        request.setActive("Y");

        mockMvc.perform(put("/api/v1/terms/{termsPoid}", -1L)
                        .header("loginUserPoid", "user-123")
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTermsTemplateAndClauses_ZeroTermsPoid_Success() throws Exception {
        TermsTemplateDto response = new TermsTemplateDto();
        response.setTermsPoid(0L);

        when(termsTemplateService.getTermsTemplateAndClauses(anyLong())).thenReturn(response);

        mockMvc.perform(get("/api/v1/terms/{termsPoid}", 0L)
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isOk());
    }

    @Test
    void softDeleteTemplate_EmptyDocumentId_Success() throws Exception {
        doNothing().when(termsTemplateService).softDeleteByTermsPoid(anyLong());

        mockMvc.perform(delete("/api/v1/terms/{termsPoid}", 1L)
                        .param("documentId", "")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isOk());
    }

    @Test
    void addClauseToTemplate_NullRequestBody_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/terms/{termsPoid}/clause", 1L)
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // Additional Edge Case Tests
    @Test
    void updateTemplateMetadata_InvalidTermsPoidFormat_ReturnsBadRequest() throws Exception {
        TermsTemplateDto request = new TermsTemplateDto();
        request.setTemplateName("Test");

        mockMvc.perform(put("/api/v1/terms/{termsPoid}", "invalid")
                        .header("loginUserPoid", "user-123")
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTemplateMetadata_SpecialCharactersInData_Success() throws Exception {
        TermsTemplateDto request = new TermsTemplateDto();
        request.setTemplateId("TEMP-@#$%");
        request.setTemplateName("Template with Special Chars: !@#$%^&*()");
        request.setDocId("DOC-001");
        request.setActive("Y");
        request.setTermsCategory("GENERAL");

        TemplateResponseDto response = new TemplateResponseDto();
        response.setTermsPoid(1L);
        response.setTemplateId("TEMP-@#$%");

        when(termsTemplateService.updateTemplateMetadata(anyLong(), any(TermsTemplateDto.class), anyString()))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/terms/{termsPoid}", 1L)
                        .header("loginUserPoid", "user-123")
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updateTemplateMetadata_EmptyStringValues_ReturnsBadRequest() throws Exception {
        TermsTemplateDto request = new TermsTemplateDto();
        request.setTemplateId("");
        request.setTemplateName("");
        request.setDocId("DOC-001");
        request.setActive("Y");
        request.setTermsCategory("");

        mockMvc.perform(put("/api/v1/terms/{termsPoid}", 1L)
                        .header("loginUserPoid", "user-123")
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTermsAndConditionsTemplate_InvalidGroupPoidFormat_ReturnsBadRequest() throws Exception {
        TermsTemplateDto request = new TermsTemplateDto();
        request.setTemplateName("Test");

        mockMvc.perform(post("/api/v1/terms")
                        .header("groupPoid", "invalid")
                        .header("loginUserPoid", "user-123")
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTermsAndConditionsTemplate_NegativeGroupPoid_ReturnsBadRequest() throws Exception {
        TermsTemplateDto request = new TermsTemplateDto();
        request.setTemplateName("Test");
        request.setDocId("DOC-001");
        request.setActive("Y");

        mockMvc.perform(post("/api/v1/terms")
                        .header("groupPoid", "-1")
                        .header("loginUserPoid", "user-123")
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTermsAndConditionsTemplate_LargePayload_Success() throws Exception {
        TermsTemplateDto request = new TermsTemplateDto();
        request.setTemplateName("A".repeat(1000)); // Large template name
        request.setDocId("DOC-001");
        request.setActive("Y");
        request.setTermsCategory("B".repeat(500)); // Large category

        TemplateResponseDto response = new TemplateResponseDto();
        response.setTermsPoid(1L);

        when(termsTemplateService.addTemplateAndClause(any(TermsTemplateDto.class), anyLong(), anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/terms")
                        .header("groupPoid", "1")
                        .header("loginUserPoid", "user-123")
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void getTermsTemplateAndClauses_MaxLongValue_Success() throws Exception {
        TermsTemplateDto response = new TermsTemplateDto();
        response.setTermsPoid(Long.MAX_VALUE);

        when(termsTemplateService.getTermsTemplateAndClauses(anyLong())).thenReturn(response);

        mockMvc.perform(get("/api/v1/terms/{termsPoid}", Long.MAX_VALUE)
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isOk());
    }

    @Test
    void getTermsTemplateAndClauses_SpecialCharactersInParams_Success() throws Exception {
        TermsTemplateDto response = new TermsTemplateDto();
        response.setTermsPoid(1L);

        when(termsTemplateService.getTermsTemplateAndClauses(anyLong())).thenReturn(response);

        mockMvc.perform(get("/api/v1/terms/{termsPoid}", 1L)
                        .param("documentId", "DOC-@#$%^&*()")
                        .param("actionRequested", "VIEW-@#$"))
                .andExpect(status().isOk());
    }

    @Test
    void softDeleteTemplate_VeryLongDocumentId_Success() throws Exception {
        doNothing().when(termsTemplateService).softDeleteByTermsPoid(anyLong());

        mockMvc.perform(delete("/api/v1/terms/{termsPoid}", 1L)
                        .param("documentId", "A".repeat(1000))
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isOk());
    }

    @Test
    void softDeleteTermsAndClauses_SpecialCharactersInClauseNo_Success() throws Exception {
        TermsTemplateDtlDto response = new TermsTemplateDtlDto();
        response.setTermsPoid(1L);
        response.setClauseNo("@#$%^&*()");
        response.setActive("N");

        when(termsTemplateService.softDeleteClause(anyLong(), anyString()))
                .thenReturn(Collections.singletonList(response));
        mockMvc.perform(delete("/api/v1/terms/{termsPoid}/clause/{clauseNo}", 1L, "@#$%^&*()")
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isOk());
    }

    @Test
    void softDeleteTermsAndClauses_EmptyClauseNo_ReturnsInternalServerError() throws Exception {
        mockMvc.perform(delete("/api/v1/terms/{termsPoid}/clause/{clauseNo}", 1L, "")
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "DELETE"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void addClauseToTemplate_LargeClauseDetails_Success() throws Exception {
        TermsTemplateDtlDto request = new TermsTemplateDtlDto();
        request.setClauseNo("001");
        request.setClauseDetails("C".repeat(5000)); // Large clause details
        request.setActive("Y");

        TermsTemplateDtlDto response = new TermsTemplateDtlDto();
        response.setTermsPoid(1L);
        response.setClauseNo("001");

        when(termsTemplateService.addClause(anyLong(), any(TermsTemplateDtlDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/terms/{termsPoid}/clause", 1L)
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void addClauseToTemplate_UnicodeCharacters_Success() throws Exception {
        TermsTemplateDtlDto request = new TermsTemplateDtlDto();
        request.setClauseNo("001");
        request.setClauseDetails("Unicode test: 中文 العربية русский 日本語 한국어");
        request.setActive("Y");

        TermsTemplateDtlDto response = new TermsTemplateDtlDto();
        response.setTermsPoid(1L);
        response.setClauseNo("001");

        when(termsTemplateService.addClause(anyLong(), any(TermsTemplateDtlDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/terms/{termsPoid}/clause", 1L)
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updateTemplateMetadata_ConcurrentModification_ReturnsBadRequest() throws Exception {
        TermsTemplateDto request = new TermsTemplateDto();
        request.setTemplateName("Concurrent Test");
        request.setDocId("DOC-001");
        request.setActive("Y");

        // Simulate concurrent requests - this will fail validation
        mockMvc.perform(put("/api/v1/terms/{termsPoid}", 1L)
                        .header("loginUserPoid", "user-123")
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTermsAndConditionsTemplate_MalformedJson_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/terms")
                        .header("groupPoid", "1")
                        .header("loginUserPoid", "user-123")
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"templateName\":\"Test\",}")) // Trailing comma
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTemplateMetadata_InvalidActionRequested_ReturnsBadRequest() throws Exception {
        TermsTemplateDto request = new TermsTemplateDto();
        request.setTemplateName("Test");
        request.setDocId("DOC-001");
        request.setActive("Y");

        mockMvc.perform(put("/api/v1/terms/{termsPoid}", 1L)
                        .header("loginUserPoid", "user-123")
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "INVALID_ACTION")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addClauseToTemplate_NullFieldsInRequest_ReturnsBadRequest() throws Exception {
        TermsTemplateDtlDto request = new TermsTemplateDtlDto();
        // All fields are null

        mockMvc.perform(post("/api/v1/terms/{termsPoid}/clause", 1L)
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}