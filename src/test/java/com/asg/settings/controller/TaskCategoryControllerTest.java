package com.asg.settings.controller;

import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.exception.ValidationException;
import com.asg.settings.dto.TaskCategoryDto;
import com.asg.settings.exceptions.GlobalExceptionHandler;
import com.asg.settings.service.TaskCategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TaskCategoryControllerTest {

    @Mock
    private TaskCategoryService taskCategoryService;

    @InjectMocks
    private TaskCategoryController taskCategoryController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private TaskCategoryDto testTaskCategory;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskCategoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();

        testTaskCategory = new TaskCategoryDto();
        testTaskCategory.setCategoryPoid(1L);
        testTaskCategory.setCategoryDescription("Test Category");
        testTaskCategory.setActive("Y");
        testTaskCategory.setSeqNo(1);
        testTaskCategory.setCategoryCode("TEST001");
    }

    @Test
    void testGetTaskCategory_Success() throws Exception {
        when(taskCategoryService.getTaskCategory(1L)).thenReturn(testTaskCategory);

        mockMvc.perform(get("/api/v1/task-category/1")
                        .param("documentId", "000-019")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data.categoryPoid").value(1))
                .andExpect(jsonPath("$.result.data.categoryDescription").value("Test Category"));

        verify(taskCategoryService, times(1)).getTaskCategory(1L);
    }

    @Test
    void testGetTaskCategory_ServiceException() throws Exception {
        when(taskCategoryService.getTaskCategory(1L))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/v1/task-category/1")
                        .param("documentId", "000-019")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));

        verify(taskCategoryService, times(1)).getTaskCategory(1L);
    }

    @Test
    void testGetTaskCategory_MissingDocumentId() throws Exception {
        mockMvc.perform(get("/api/v1/task-category/1")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetTaskCategory_InvalidId() throws Exception {
        mockMvc.perform(get("/api/v1/task-category/invalid")
                        .param("documentId", "000-019")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTaskCategory_Success() throws Exception {
        when(taskCategoryService.createTaskCategory(any(TaskCategoryDto.class))).thenReturn(testTaskCategory);

        mockMvc.perform(post("/api/v1/task-category")
                        .param("documentId", "000-019")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTaskCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data.categoryDescription").value("Test Category"));

        verify(taskCategoryService, times(1)).createTaskCategory(any(TaskCategoryDto.class));
    }

    @Test
    void testUpdateTaskCategory_Success() throws Exception {
        when(taskCategoryService.updateTaskCategory(eq(1L), any(TaskCategoryDto.class))).thenReturn(testTaskCategory);

        mockMvc.perform(put("/api/v1/task-category/1")
                        .param("documentId", "000-019")
                        .param("actionRequested", "UPDATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTaskCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data.categoryDescription").value("Test Category"));

        verify(taskCategoryService, times(1)).updateTaskCategory(eq(1L), any(TaskCategoryDto.class));
    }

    @Test
    void testListTaskCategories_Success() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("content", List.of(testTaskCategory));
        data.put("totalElements", 1);

        when(taskCategoryService.listTaskCategories(anyString(), any(FilterRequestDto.class), any(Pageable.class)))
                .thenReturn(data);

        FilterDto filter = new FilterDto("CATEGORY_DESCRIPTION", "Test");
        FilterRequestDto filters = new FilterRequestDto("AND", "N", List.of(filter));

        mockMvc.perform(post("/api/v1/task-category/list")
                        .param("documentId", "000-019")
                        .param("actionRequested", "VIEW")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filters)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data.totalElements").value(1));

        verify(taskCategoryService, times(1)).listTaskCategories(anyString(), any(FilterRequestDto.class), any(Pageable.class));
    }

    @Test
    void testCreateTaskCategory_MissingDocumentId() throws Exception {
        mockMvc.perform(post("/api/v1/task-category")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTaskCategory)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testCreateTaskCategory_InvalidJson() throws Exception {
        mockMvc.perform(post("/api/v1/task-category")
                        .param("documentId", "000-019")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateTaskCategory_InvalidId() throws Exception {
        mockMvc.perform(put("/api/v1/task-category/invalid")
                        .param("documentId", "000-019")
                        .param("actionRequested", "UPDATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTaskCategory)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetTaskCategory_MissingActionRequested() throws Exception {
        mockMvc.perform(get("/api/v1/task-category/1")
                        .param("documentId", "000-019"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetTaskCategory_NegativeId() throws Exception {
        when(taskCategoryService.getTaskCategory(-1L)).thenReturn(testTaskCategory);

        mockMvc.perform(get("/api/v1/task-category/-1")
                        .param("documentId", "000-019")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetTaskCategory_ZeroId() throws Exception {
        when(taskCategoryService.getTaskCategory(0L)).thenReturn(testTaskCategory);

        mockMvc.perform(get("/api/v1/task-category/0")
                        .param("documentId", "000-019")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateTaskCategory_MissingActionRequested() throws Exception {
        mockMvc.perform(post("/api/v1/task-category")
                        .param("documentId", "000-019")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTaskCategory)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testCreateTaskCategory_EmptyRequestBody() throws Exception {
        mockMvc.perform(post("/api/v1/task-category")
                        .param("documentId", "000-019")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTaskCategory_ServiceException() throws Exception {
        when(taskCategoryService.createTaskCategory(any(TaskCategoryDto.class)))
                .thenThrow(new RuntimeException("Create failed"));

        mockMvc.perform(post("/api/v1/task-category")
                        .param("documentId", "000-019")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTaskCategory)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));

        verify(taskCategoryService, times(1)).createTaskCategory(any(TaskCategoryDto.class));
    }

    @Test
    void testUpdateTaskCategory_MissingDocumentId() throws Exception {
        mockMvc.perform(put("/api/v1/task-category/1")
                        .param("actionRequested", "UPDATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTaskCategory)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testUpdateTaskCategory_ServiceException() throws Exception {
        when(taskCategoryService.updateTaskCategory(eq(1L), any(TaskCategoryDto.class)))
                .thenThrow(new RuntimeException("Update failed"));

        mockMvc.perform(put("/api/v1/task-category/1")
                        .param("documentId", "000-019")
                        .param("actionRequested", "UPDATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTaskCategory)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));

        verify(taskCategoryService, times(1)).updateTaskCategory(eq(1L), any(TaskCategoryDto.class));
    }

    @Test
    void testListTaskCategories_MissingDocumentId() throws Exception {
        FilterDto filter = new FilterDto("CATEGORY_DESCRIPTION", "Test");
        FilterRequestDto filters = new FilterRequestDto("AND", "N", List.of(filter));

        mockMvc.perform(post("/api/v1/task-category/list")
                        .param("actionRequested", "VIEW")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filters)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testListTaskCategories_ServiceException() throws Exception {
        when(taskCategoryService.listTaskCategories(anyString(), any(FilterRequestDto.class), any(Pageable.class)))
                .thenThrow(new RuntimeException("List failed"));

        FilterDto filter = new FilterDto("CATEGORY_DESCRIPTION", "Test");
        FilterRequestDto filters = new FilterRequestDto("AND", "N", List.of(filter));

        mockMvc.perform(post("/api/v1/task-category/list")
                        .param("documentId", "000-019")
                        .param("actionRequested", "VIEW")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filters)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));

        verify(taskCategoryService, times(1)).listTaskCategories(anyString(), any(FilterRequestDto.class), any(Pageable.class));
    }

    @Test
    void testListTaskCategories_InvalidJson() throws Exception {
        mockMvc.perform(post("/api/v1/task-category/list")
                        .param("documentId", "000-019")
                        .param("actionRequested", "VIEW")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTaskCategory_UnsupportedMediaType() throws Exception {
        mockMvc.perform(post("/api/v1/task-category")
                        .param("documentId", "000-019")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("plain text"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testUpdateTaskCategory_EmptyRequestBody() throws Exception {
        mockMvc.perform(put("/api/v1/task-category/1")
                        .param("documentId", "000-019")
                        .param("actionRequested", "UPDATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testListTaskCategories_EmptyFilters() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("content", List.of());
        data.put("totalElements", 0);

        when(taskCategoryService.listTaskCategories(anyString(), any(FilterRequestDto.class), any(Pageable.class)))
                .thenReturn(data);

        FilterRequestDto filters = new FilterRequestDto("AND", "N", List.of());

        mockMvc.perform(post("/api/v1/task-category/list")
                        .param("documentId", "000-019")
                        .param("actionRequested", "VIEW")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filters)))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateTaskCategory_NullRequestBody() throws Exception {
        mockMvc.perform(post("/api/v1/task-category")
                        .param("documentId", "000-019")
                        .param("actionRequested", "CREATE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateTaskCategory_NullRequestBody() throws Exception {
        mockMvc.perform(put("/api/v1/task-category/1")
                        .param("documentId", "000-019")
                        .param("actionRequested", "UPDATE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testListTaskCategories_NullRequestBody() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("content", List.of());
        data.put("totalElements", 0);

        when(taskCategoryService.listTaskCategories(anyString(), isNull(), any(Pageable.class)))
                .thenReturn(data);

        mockMvc.perform(post("/api/v1/task-category/list")
                        .param("documentId", "000-019")
                        .param("actionRequested", "VIEW")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetTaskCategory_EmptyDocumentId() throws Exception {
        when(taskCategoryService.getTaskCategory(1L)).thenReturn(testTaskCategory);

        mockMvc.perform(get("/api/v1/task-category/1")
                        .param("documentId", "")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateTaskCategory_EmptyActionRequested() throws Exception {
        when(taskCategoryService.createTaskCategory(any(TaskCategoryDto.class))).thenReturn(testTaskCategory);

        mockMvc.perform(post("/api/v1/task-category")
                        .param("documentId", "000-019")
                        .param("actionRequested", "")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTaskCategory)))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateTaskCategory_ValidationException() throws Exception {
        when(taskCategoryService.updateTaskCategory(eq(1L), any(TaskCategoryDto.class)))
                .thenThrow(new ValidationException("Validation failed"));

        mockMvc.perform(put("/api/v1/task-category/1")
                        .param("documentId", "000-019")
                        .param("actionRequested", "UPDATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTaskCategory)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testListTaskCategories_ValidationException() throws Exception {
        when(taskCategoryService.listTaskCategories(anyString(), any(FilterRequestDto.class), any(Pageable.class)))
                .thenThrow(new ValidationException("Invalid filter"));

        FilterDto filter = new FilterDto("CATEGORY_DESCRIPTION", "Test");
        FilterRequestDto filters = new FilterRequestDto("AND", "N", List.of(filter));

        mockMvc.perform(post("/api/v1/task-category/list")
                        .param("documentId", "000-019")
                        .param("actionRequested", "VIEW")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
