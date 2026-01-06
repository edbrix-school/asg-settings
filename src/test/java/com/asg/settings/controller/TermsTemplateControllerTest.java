package com.asg.settings.controller;

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

    // PUT /v1/terms/{termsPoid} - updateTemplateMetadata tests
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

        mockMvc.perform(put("/v1/terms/{termsPoid}", 1L)
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

        mockMvc.perform(put("/v1/terms/{termsPoid}", 1L)
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

        mockMvc.perform(put("/v1/terms/{termsPoid}", 1L)
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

        mockMvc.perform(put("/v1/terms/{termsPoid}", 1L)
                        .header("loginUserPoid", "user-123")
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "EDIT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    // POST /v1/terms - createTermsAndConditionsTemplate tests
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

        mockMvc.perform(post("/v1/terms")
                        .header("groupPoid", "1")
                        .header("loginUserPoid", "user-123")
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Template created successfully"))
                .andExpect(jsonPath("$.result.data.templateId").value("TEMP-001"));

        verify(termsTemplateService).addTemplateAndClause(any(TermsTemplateDto.class), eq(1L), eq("user-123"));
    }

    @Test
    void createTermsAndConditionsTemplate_MissingGroupPoid_ReturnsBadRequest() throws Exception {
        TermsTemplateDto request = new TermsTemplateDto();
        request.setTemplateName("Test");

        mockMvc.perform(post("/v1/terms")
                        .header("loginUserPoid", "user-123")
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTermsAndConditionsTemplate_InvalidJson_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/v1/terms")
                        .header("groupPoid", "1")
                        .header("loginUserPoid", "user-123")
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    // GET /v1/terms/{termsPoid} - getTermsTemplateAndClauses tests
    @Test
    void getTermsTemplateAndClauses_Success() throws Exception {
        TermsTemplateDto response = new TermsTemplateDto();
        response.setTermsPoid(1L);
        response.setTemplateName("Test Template");

        when(termsTemplateService.getTermsTemplateAndClauses(anyLong())).thenReturn(response);

        mockMvc.perform(get("/v1/terms/{termsPoid}", 1L)
                        .param("documentId", "DOC-001")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Terms & clauses fetched successFully "))
                .andExpect(jsonPath("$.result.data.termsPoid").value(1));

        verify(termsTemplateService).getTermsTemplateAndClauses(1L);
    }
}