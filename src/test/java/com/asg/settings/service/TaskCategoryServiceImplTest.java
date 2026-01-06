package com.asg.settings.service;

import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.exception.ResourceAlreadyExistsException;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.settings.dto.TaskCategoryDtlDto;
import com.asg.settings.dto.TaskCategoryDto;
import com.asg.settings.entity.RoleEntity;
import com.asg.settings.entity.TaskCategoryDTLEntity;
import com.asg.settings.entity.TaskCategoryEntity;
import com.asg.settings.repository.RoleRepository;
import com.asg.settings.repository.TaskCategoryDTLRepository;
import com.asg.settings.repository.TaskCategoryRepository;
import com.asg.settings.service.impl.TaskCategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaskCategoryServiceImplTest {

    @Mock
    private TaskCategoryRepository taskCategoryRepository;

    @Mock
    private TaskCategoryDTLRepository taskCategoryDTLRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private DocumentSearchService documentService;

    @Mock
    private com.asg.common.lib.service.LoggingService loggingService;

    @InjectMocks
    private TaskCategoryServiceImpl taskCategoryService;

    private TaskCategoryEntity testCategory;
    private TaskCategoryDTLEntity testCategoryDtl;
    private RoleEntity testRole;
    private final Long categoryPoid = 1L;

    @BeforeEach
    void setUp() {
        // Mock LoggingService to prevent NullPointerException
        org.springframework.test.util.ReflectionTestUtils.setField(taskCategoryService, "loggingService", loggingService);
        
        testCategory = new TaskCategoryEntity();
        testCategory.setCategoryPoid(categoryPoid);
        testCategory.setCategoryDescription("Test Category");
        testCategory.setUserRolePoid("1;2;3");
        testCategory.setActive("Y");
        testCategory.setSeqNo(1);
        testCategory.setCategoryCode("TEST");
        testCategory.setDeleted("N");

        testCategoryDtl = new TaskCategoryDTLEntity();
        testCategoryDtl.setDetRowId(1L);
        testCategoryDtl.setCategoryPoid(categoryPoid);
        testCategoryDtl.setSubCategoryDescription("Test Subcategory");

        testRole = new RoleEntity();
        testRole.setUserRolePoid(1L);
        testRole.setUserRoleId("ROLE1");
        testRole.setUserRoleName("Test Role");
        testRole.setActive("Y");
    }

    @Test
    void getTaskCategory_ShouldReturnTaskCategory_WhenFound() {
        when(taskCategoryRepository.findByCategoryPoid(categoryPoid)).thenReturn(testCategory);
        when(taskCategoryDTLRepository.findByCategoryPoid(categoryPoid))
                .thenReturn(Collections.singletonList(testCategoryDtl));
        when(roleRepository.findByUserRolePoidIn(anyList()))
                .thenReturn(Collections.singletonList(testRole));

        TaskCategoryDto result = taskCategoryService.getTaskCategory(categoryPoid);

        assertNotNull(result);
        assertEquals(categoryPoid, result.getCategoryPoid());
        assertEquals("Test Category", result.getCategoryDescription());
        assertEquals("Y", result.getActive());
        assertEquals(1, result.getSeqNo());
        assertEquals("TEST", result.getCategoryCode());
        assertNotNull(result.getSubCategories());
        assertEquals(1, result.getSubCategories().size());
        assertNotNull(result.getUserRolePoidDet());
        assertEquals(1, result.getUserRolePoidDet().size());
    }

    @Test
    void getTaskCategory_ShouldThrowResourceNotFoundException_WhenCategoryNotFound() {
        when(taskCategoryRepository.findByCategoryPoid(anyLong())).thenReturn(null);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> taskCategoryService.getTaskCategory(categoryPoid)
        );

        assertEquals("Task Category not found with categoryPoid : '" + categoryPoid + "'", exception.getMessage());
    }

    // Edge Cases for getTaskCategory
    @Test
    void getTaskCategory_ShouldHandleEmptyUserRolePoid() {
        testCategory.setUserRolePoid("");
        when(taskCategoryRepository.findByCategoryPoid(categoryPoid)).thenReturn(testCategory);
        when(taskCategoryDTLRepository.findByCategoryPoid(categoryPoid)).thenReturn(Collections.emptyList());
        when(roleRepository.findByUserRolePoidIn(anyList())).thenReturn(Collections.emptyList());

        TaskCategoryDto result = taskCategoryService.getTaskCategory(categoryPoid);

        assertNotNull(result);
        assertTrue(result.getUserRolePoidDet().isEmpty());
    }

    @Test
    void getTaskCategory_ShouldHandleNullUserRolePoid() {
        testCategory.setUserRolePoid(null);
        when(taskCategoryRepository.findByCategoryPoid(categoryPoid)).thenReturn(testCategory);
        when(taskCategoryDTLRepository.findByCategoryPoid(categoryPoid)).thenReturn(Collections.emptyList());
        when(roleRepository.findByUserRolePoidIn(anyList())).thenReturn(Collections.emptyList());

        TaskCategoryDto result = taskCategoryService.getTaskCategory(categoryPoid);

        assertNotNull(result);
        assertTrue(result.getUserRolePoidDet().isEmpty());
    }

    @Test
    void getTaskCategory_ShouldHandleNullRoleEntities() {
        when(taskCategoryRepository.findByCategoryPoid(categoryPoid)).thenReturn(testCategory);
        when(taskCategoryDTLRepository.findByCategoryPoid(categoryPoid)).thenReturn(Collections.emptyList());
        when(roleRepository.findByUserRolePoidIn(anyList())).thenReturn(Arrays.asList(testRole, null));

        TaskCategoryDto result = taskCategoryService.getTaskCategory(categoryPoid);

        assertNotNull(result);
        assertEquals(1, result.getUserRolePoidDet().size());
    }

    @Test
    void softDeleteTaskCategory_ShouldReturnSuccess_WhenCategoryExists() {
        when(taskCategoryRepository.findByCategoryPoid(categoryPoid)).thenReturn(testCategory);
        when(taskCategoryRepository.save(any(TaskCategoryEntity.class))).thenReturn(testCategory);

        ResponseEntity<?> result = taskCategoryService.softDeleteTaskCategory(categoryPoid, "testUser");

        assertEquals(200, result.getStatusCodeValue());
        verify(taskCategoryRepository).save(testCategory);
        assertEquals("N", testCategory.getActive());
        assertEquals("Y", testCategory.getDeleted());
    }

    @Test
    void softDeleteTaskCategory_ShouldReturnNotFound_WhenCategoryNotExists() {
        when(taskCategoryRepository.findByCategoryPoid(categoryPoid)).thenReturn(null);

        ResponseEntity<?> result = taskCategoryService.softDeleteTaskCategory(categoryPoid, "testUser");

        assertEquals(404, result.getStatusCodeValue());
    }

    @Test
    void softDeleteTaskCategory_ShouldReturnSuccess_WhenAlreadyDeleted() {
        testCategory.setActive("N");
        when(taskCategoryRepository.findByCategoryPoid(categoryPoid)).thenReturn(testCategory);

        ResponseEntity<?> result = taskCategoryService.softDeleteTaskCategory(categoryPoid, "testUser");

        assertEquals(200, result.getStatusCodeValue());
    }

    // Edge Cases for softDeleteTaskCategory
    @Test
    void softDeleteTaskCategory_ShouldHandleNullUpdatedBy() {
        when(taskCategoryRepository.findByCategoryPoid(categoryPoid)).thenReturn(testCategory);
        when(taskCategoryRepository.save(any(TaskCategoryEntity.class))).thenReturn(testCategory);

        ResponseEntity<?> result = taskCategoryService.softDeleteTaskCategory(categoryPoid, null);

        assertEquals(200, result.getStatusCodeValue());
        assertNull(testCategory.getLastModifiedBy());
    }

    @Test
    void softDeleteTaskCategory_ShouldHandleEmptyUpdatedBy() {
        when(taskCategoryRepository.findByCategoryPoid(categoryPoid)).thenReturn(testCategory);
        when(taskCategoryRepository.save(any(TaskCategoryEntity.class))).thenReturn(testCategory);

        ResponseEntity<?> result = taskCategoryService.softDeleteTaskCategory(categoryPoid, "");

        assertEquals(200, result.getStatusCodeValue());
        assertEquals("", testCategory.getLastModifiedBy());
    }

//    @Test
//    void getAllTaskCategories_ShouldReturnPagedResults() {
//        List<TaskCategoryEntity> entities = Arrays.asList(testCategory);
//        Page<TaskCategoryEntity> page = new PageImpl<>(entities, PageRequest.of(0, 10), 1);
//        List<FilterDto> filters = Arrays.asList(new FilterDto("categoryDescription", "Test"));
//
//        when(taskCategoryRepository.getAllTaskcategories(filters, PageRequest.of(0, 10))).thenReturn(page);
//        when(taskCategoryDTLRepository.findByCategoryPoid(categoryPoid)).thenReturn(Arrays.asList(testCategoryDtl));
//        when(roleRepository.findByUserRolePoidIn(anyList())).thenReturn(Arrays.asList(testRole));
//
//        Page<TaskCategoryDto> result = taskCategoryService.getAllTaskCategories(filters, PageRequest.of(0, 10));
//
//        assertNotNull(result);
//        assertEquals(1, result.getTotalElements());
//    }

    // Edge Cases for getAllTaskCategories
//    @Test
//    void getAllTaskCategories_ShouldHandleEmptyResults() {
//        Page<TaskCategoryEntity> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
//        when(taskCategoryRepository.getAllTaskcategories(anyList(), any(Pageable.class))).thenReturn(emptyPage);
//
//        Page<TaskCategoryDto> result = taskCategoryService.getAllTaskCategories(Collections.emptyList(), PageRequest.of(0, 10));
//
//        assertNotNull(result);
//        assertEquals(0, result.getTotalElements());
//        assertTrue(result.getContent().isEmpty());
//    }

//    @Test
//    void getAllTaskCategories_ShouldHandleNullFilters() {
//        List<TaskCategoryEntity> entities = Arrays.asList(testCategory);
//        Page<TaskCategoryEntity> page = new PageImpl<>(entities, PageRequest.of(0, 10), 1);
//
//        when(taskCategoryRepository.getAllTaskcategories(null, PageRequest.of(0, 10))).thenReturn(page);
//        when(taskCategoryDTLRepository.findByCategoryPoid(categoryPoid)).thenReturn(Collections.emptyList());
//        when(roleRepository.findByUserRolePoidIn(anyList())).thenReturn(Collections.emptyList());
//
//        Page<TaskCategoryDto> result = taskCategoryService.getAllTaskCategories(null, PageRequest.of(0, 10));
//
//        assertNotNull(result);
//        assertEquals(1, result.getTotalElements());
//    }

    @Test
    void listTaskCategories_ShouldReturnMappedResults() {
        FilterRequestDto request = new FilterRequestDto("AND", "N", Collections.emptyList());
        Pageable pageable = PageRequest.of(0, 10);
        RawSearchResult rawResult = new RawSearchResult(
                Arrays.asList(Map.of("CATEGORY_DESCRIPTION", "Test", "CATEGORY_POID", 1L)),
                Map.of("CATEGORY_DESCRIPTION", "Category Description", "CATEGORY_POID", "Category ID"),
                1L
        );

        when(documentService.resolveOperator(request)).thenReturn("AND");
        when(documentService.resolveIsDeleted(request)).thenReturn("N");
        when(documentService.resolveFilters(request)).thenReturn(Collections.emptyList());

        when(documentService.search(anyString(), anyList(), anyString(), any(Pageable.class), anyString(), anyString(), anyString()))
                .thenReturn(rawResult);

        Map<String, Object> result = taskCategoryService.listTaskCategories("DOC001", request, pageable);

        assertNotNull(result);
        assertTrue(result.containsKey("content"));
    }

    @Test
    void createTaskCategory_ShouldReturnCreatedCategory_WhenValidInput() {
        TaskCategoryDto taskCategoryDto = new TaskCategoryDto();
        taskCategoryDto.setCategoryDescription("New Category");
        taskCategoryDto.setUserRolePoid(Arrays.asList("1", "2"));
        taskCategoryDto.setSeqNo(1);
        taskCategoryDto.setCategoryCode("NEW");

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn("testUser");

            when(taskCategoryRepository.existsByCategoryDescriptionIgnoreCase(anyString())).thenReturn(false);
            when(taskCategoryRepository.save(any(TaskCategoryEntity.class))).thenReturn(testCategory);
            when(taskCategoryRepository.findByCategoryPoid(categoryPoid)).thenReturn(testCategory);
            when(taskCategoryDTLRepository.findByCategoryPoid(categoryPoid)).thenReturn(Collections.emptyList());
            when(roleRepository.findByUserRolePoidIn(anyList())).thenReturn(Collections.emptyList());

            TaskCategoryDto result = taskCategoryService.createTaskCategory(taskCategoryDto);

            assertNotNull(result);
            verify(taskCategoryRepository).save(any(TaskCategoryEntity.class));
        }
    }

    @Test
    void createTaskCategory_ShouldThrowException_WhenCategoryAlreadyExists() {
        TaskCategoryDto taskCategoryDto = new TaskCategoryDto();
        taskCategoryDto.setCategoryDescription("Existing Category");

        when(taskCategoryRepository.existsByCategoryDescriptionIgnoreCase(anyString())).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class,
                () -> taskCategoryService.createTaskCategory(taskCategoryDto));
    }

    // Edge Cases for createTaskCategory
    @Test
    void createTaskCategory_ShouldHandleNullUserContext() {
        TaskCategoryDto taskCategoryDto = new TaskCategoryDto();
        taskCategoryDto.setCategoryDescription("New Category");

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(null);

            when(taskCategoryRepository.existsByCategoryDescriptionIgnoreCase(anyString())).thenReturn(false);
            when(taskCategoryRepository.save(any(TaskCategoryEntity.class))).thenReturn(testCategory);
            when(taskCategoryRepository.findByCategoryPoid(categoryPoid)).thenReturn(testCategory);
            when(taskCategoryDTLRepository.findByCategoryPoid(categoryPoid)).thenReturn(Collections.emptyList());
            when(roleRepository.findByUserRolePoidIn(anyList())).thenReturn(Collections.emptyList());

            TaskCategoryDto result = taskCategoryService.createTaskCategory(taskCategoryDto);

            assertNotNull(result);
        }
    }

    @Test
    void createTaskCategory_ShouldHandleNullActiveAndDeleted() {
        TaskCategoryDto taskCategoryDto = new TaskCategoryDto();
        taskCategoryDto.setCategoryDescription("New Category");
        taskCategoryDto.setActive(null);
        taskCategoryDto.setDeleted(null);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn("testUser");

            when(taskCategoryRepository.existsByCategoryDescriptionIgnoreCase(anyString())).thenReturn(false);
            when(taskCategoryRepository.save(any(TaskCategoryEntity.class))).thenReturn(testCategory);
            when(taskCategoryRepository.findByCategoryPoid(categoryPoid)).thenReturn(testCategory);
            when(taskCategoryDTLRepository.findByCategoryPoid(categoryPoid)).thenReturn(Collections.emptyList());
            when(roleRepository.findByUserRolePoidIn(anyList())).thenReturn(Collections.emptyList());

            TaskCategoryDto result = taskCategoryService.createTaskCategory(taskCategoryDto);

            assertNotNull(result);
        }
    }

    @Test
    void createTaskCategory_ShouldCreateWithSubCategories() {
        TaskCategoryDto taskCategoryDto = new TaskCategoryDto();
        taskCategoryDto.setCategoryDescription("New Category");
        TaskCategoryDtlDto subCategory = new TaskCategoryDtlDto();
        subCategory.setSubCategoryDescription("New Sub");
        taskCategoryDto.setSubCategories(Arrays.asList(subCategory));

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn("testUser");

            when(taskCategoryRepository.existsByCategoryDescriptionIgnoreCase(anyString())).thenReturn(false);
            when(taskCategoryDTLRepository.existsBySubCategoryDescriptionIgnoreCase(anyString())).thenReturn(false);
            when(taskCategoryRepository.save(any(TaskCategoryEntity.class))).thenReturn(testCategory);
            when(taskCategoryRepository.findByCategoryPoid(categoryPoid)).thenReturn(testCategory);
            when(taskCategoryDTLRepository.findByCategoryPoid(categoryPoid)).thenReturn(Arrays.asList(testCategoryDtl));
            when(roleRepository.findByUserRolePoidIn(anyList())).thenReturn(Collections.emptyList());

            TaskCategoryDto result = taskCategoryService.createTaskCategory(taskCategoryDto);

            assertNotNull(result);
            verify(taskCategoryDTLRepository).saveAll(anyList());
        }
    }

    @Test
    void createTaskCategory_ShouldThrowException_WhenSubCategoryAlreadyExists() {
        TaskCategoryDto taskCategoryDto = new TaskCategoryDto();
        taskCategoryDto.setCategoryDescription("New Category");
        TaskCategoryDtlDto subCategory = new TaskCategoryDtlDto();
        subCategory.setSubCategoryDescription("Existing Sub");
        taskCategoryDto.setSubCategories(Arrays.asList(subCategory));

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn("testUser");

            when(taskCategoryRepository.existsByCategoryDescriptionIgnoreCase(anyString())).thenReturn(false);
            when(taskCategoryDTLRepository.existsBySubCategoryDescriptionIgnoreCase(anyString())).thenReturn(true);
            when(taskCategoryRepository.save(any(TaskCategoryEntity.class))).thenReturn(testCategory);

            assertThrows(ResourceAlreadyExistsException.class,
                    () -> taskCategoryService.createTaskCategory(taskCategoryDto));
        }
    }

    @Test
    void updateTaskCategory_ShouldReturnUpdatedCategory_WhenValidInput() {
        TaskCategoryDto taskCategoryDto = new TaskCategoryDto();
        taskCategoryDto.setCategoryDescription("Updated Category");

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn("testUser");

            when(taskCategoryRepository.findByCategoryPoid(categoryPoid)).thenReturn(testCategory);
            when(taskCategoryRepository.save(any(TaskCategoryEntity.class))).thenReturn(testCategory);
            when(taskCategoryDTLRepository.findByCategoryPoid(categoryPoid)).thenReturn(Collections.emptyList());
            when(roleRepository.findByUserRolePoidIn(anyList())).thenReturn(Collections.emptyList());

            TaskCategoryDto result = taskCategoryService.updateTaskCategory(categoryPoid, taskCategoryDto);

            assertNotNull(result);
            verify(taskCategoryRepository).save(testCategory);
        }
    }

    @Test
    void updateTaskCategory_ShouldThrowException_WhenCategoryNotFound() {
        TaskCategoryDto taskCategoryDto = new TaskCategoryDto();
        when(taskCategoryRepository.findByCategoryPoid(categoryPoid)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> taskCategoryService.updateTaskCategory(categoryPoid, taskCategoryDto));
    }

    @Test
    void updateTaskCategory_ShouldHandleDeleteAction() {
        TaskCategoryDto taskCategoryDto = new TaskCategoryDto();
        TaskCategoryDtlDto subCategoryDto = new TaskCategoryDtlDto();
        subCategoryDto.setDetRowId(1L);
        subCategoryDto.setActionType("isDeleted");
        taskCategoryDto.setSubCategories(Arrays.asList(subCategoryDto));

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn("testUser");

            when(taskCategoryRepository.findByCategoryPoid(categoryPoid)).thenReturn(testCategory);
            when(taskCategoryRepository.save(any(TaskCategoryEntity.class))).thenReturn(testCategory);
            when(taskCategoryDTLRepository.findByCategoryPoidAndDetRowId(categoryPoid, 1L))
                    .thenReturn(Optional.of(testCategoryDtl));
            when(taskCategoryDTLRepository.findByCategoryPoid(categoryPoid)).thenReturn(Collections.emptyList());
            when(roleRepository.findByUserRolePoidIn(anyList())).thenReturn(Collections.emptyList());

            TaskCategoryDto result = taskCategoryService.updateTaskCategory(categoryPoid, taskCategoryDto);

            assertNotNull(result);
            verify(taskCategoryDTLRepository).deleteAll(anyList());
        }
    }

    // Edge Cases for updateTaskCategory delete action
    @Test
    void updateTaskCategory_ShouldHandleDeleteSubCategoryNotFound() {
        TaskCategoryDto taskCategoryDto = new TaskCategoryDto();
        TaskCategoryDtlDto subCategoryDto = new TaskCategoryDtlDto();
        subCategoryDto.setDetRowId(999L);
        subCategoryDto.setActionType("isDeleted");
        taskCategoryDto.setSubCategories(Arrays.asList(subCategoryDto));

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn("testUser");

            when(taskCategoryRepository.findByCategoryPoid(categoryPoid)).thenReturn(testCategory);
            when(taskCategoryRepository.save(any(TaskCategoryEntity.class))).thenReturn(testCategory);
            when(taskCategoryDTLRepository.findByCategoryPoidAndDetRowId(categoryPoid, 999L))
                    .thenReturn(Optional.empty());
            when(taskCategoryDTLRepository.findByCategoryPoid(categoryPoid)).thenReturn(Collections.emptyList());
            when(roleRepository.findByUserRolePoidIn(anyList())).thenReturn(Collections.emptyList());

            TaskCategoryDto result = taskCategoryService.updateTaskCategory(categoryPoid, taskCategoryDto);

            assertNotNull(result);
            verify(taskCategoryDTLRepository, never()).deleteAll(anyList());
        }
    }

    @Test
    void updateTaskCategory_ShouldHandleCreateAction() {
        TaskCategoryDto taskCategoryDto = new TaskCategoryDto();
        TaskCategoryDtlDto subCategoryDto = new TaskCategoryDtlDto();
        subCategoryDto.setSubCategoryDescription("New Sub");
        subCategoryDto.setActionType("isCreated");
        taskCategoryDto.setSubCategories(Arrays.asList(subCategoryDto));

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn("testUser");

            when(taskCategoryRepository.findByCategoryPoid(categoryPoid)).thenReturn(testCategory);
            when(taskCategoryRepository.save(any(TaskCategoryEntity.class))).thenReturn(testCategory);
            when(taskCategoryDTLRepository.existsBySubCategoryDescriptionIgnoreCase(anyString())).thenReturn(false);
            when(taskCategoryDTLRepository.findByCategoryPoid(categoryPoid)).thenReturn(Collections.emptyList());
            when(roleRepository.findByUserRolePoidIn(anyList())).thenReturn(Collections.emptyList());

            TaskCategoryDto result = taskCategoryService.updateTaskCategory(categoryPoid, taskCategoryDto);

            assertNotNull(result);
            verify(taskCategoryDTLRepository).saveAll(anyList());
        }
    }

    @Test
    void updateTaskCategory_ShouldHandleCreateActionWithDetRowId() {
        TaskCategoryDto taskCategoryDto = new TaskCategoryDto();
        TaskCategoryDtlDto subCategoryDto = new TaskCategoryDtlDto();
        subCategoryDto.setDetRowId(1L);
        subCategoryDto.setSubCategoryDescription("Updated Sub");
        subCategoryDto.setActionType("isCreated");
        taskCategoryDto.setSubCategories(Arrays.asList(subCategoryDto));

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn("testUser");

            when(taskCategoryRepository.findByCategoryPoid(categoryPoid)).thenReturn(testCategory);
            when(taskCategoryRepository.save(any(TaskCategoryEntity.class))).thenReturn(testCategory);
            when(taskCategoryDTLRepository.findByCategoryPoidAndDetRowId(categoryPoid, 1L))
                    .thenReturn(Optional.of(testCategoryDtl));
            when(taskCategoryDTLRepository.findByCategoryPoid(categoryPoid)).thenReturn(Collections.emptyList());
            when(roleRepository.findByUserRolePoidIn(anyList())).thenReturn(Collections.emptyList());

            TaskCategoryDto result = taskCategoryService.updateTaskCategory(categoryPoid, taskCategoryDto);

            assertNotNull(result);
            verify(taskCategoryDTLRepository).saveAll(anyList());
        }
    }

    @Test
    void updateTaskCategory_ShouldHandleUpdateAction() {
        TaskCategoryDto taskCategoryDto = new TaskCategoryDto();
        TaskCategoryDtlDto subCategoryDto = new TaskCategoryDtlDto();
        subCategoryDto.setDetRowId(1L);
        subCategoryDto.setSubCategoryDescription("Updated Sub");
        subCategoryDto.setActionType("isUpdated");
        taskCategoryDto.setSubCategories(Arrays.asList(subCategoryDto));

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn("testUser");

            when(taskCategoryRepository.findByCategoryPoid(categoryPoid)).thenReturn(testCategory);
            when(taskCategoryRepository.save(any(TaskCategoryEntity.class))).thenReturn(testCategory);
            when(taskCategoryDTLRepository.findByCategoryPoidAndDetRowId(categoryPoid, 1L))
                    .thenReturn(Optional.of(testCategoryDtl));
            when(taskCategoryDTLRepository.findByCategoryPoid(categoryPoid)).thenReturn(Collections.emptyList());
            when(roleRepository.findByUserRolePoidIn(anyList())).thenReturn(Collections.emptyList());

            TaskCategoryDto result = taskCategoryService.updateTaskCategory(categoryPoid, taskCategoryDto);

            assertNotNull(result);
            verify(taskCategoryDTLRepository).saveAll(anyList());
        }
    }

    // Edge Cases for updateTaskCategory update action
    @Test
    void updateTaskCategory_ShouldHandleUpdateSubCategoryNotFound() {
        TaskCategoryDto taskCategoryDto = new TaskCategoryDto();
        TaskCategoryDtlDto subCategoryDto = new TaskCategoryDtlDto();
        subCategoryDto.setDetRowId(999L);
        subCategoryDto.setSubCategoryDescription("Updated Sub");
        subCategoryDto.setActionType("isUpdated");
        taskCategoryDto.setSubCategories(Arrays.asList(subCategoryDto));

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn("testUser");

            when(taskCategoryRepository.findByCategoryPoid(categoryPoid)).thenReturn(testCategory);
            when(taskCategoryRepository.save(any(TaskCategoryEntity.class))).thenReturn(testCategory);
            when(taskCategoryDTLRepository.findByCategoryPoidAndDetRowId(categoryPoid, 999L))
                    .thenReturn(Optional.empty());
            when(taskCategoryDTLRepository.existsBySubCategoryDescriptionIgnoreCase(anyString())).thenReturn(false);
            when(taskCategoryDTLRepository.findByCategoryPoid(categoryPoid)).thenReturn(Collections.emptyList());
            when(roleRepository.findByUserRolePoidIn(anyList())).thenReturn(Collections.emptyList());

            TaskCategoryDto result = taskCategoryService.updateTaskCategory(categoryPoid, taskCategoryDto);

            assertNotNull(result);
            verify(taskCategoryDTLRepository).saveAll(anyList()); // Creates new entity when not found
        }
    }

    @Test
    void updateTaskCategory_ShouldHandleUnknownActionType() {
        TaskCategoryDto taskCategoryDto = new TaskCategoryDto();
        TaskCategoryDtlDto subCategoryDto = new TaskCategoryDtlDto();
        subCategoryDto.setDetRowId(1L);
        subCategoryDto.setSubCategoryDescription("Test Sub");
        subCategoryDto.setActionType("unknown");
        taskCategoryDto.setSubCategories(Arrays.asList(subCategoryDto));

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn("testUser");

            when(taskCategoryRepository.findByCategoryPoid(categoryPoid)).thenReturn(testCategory);
            when(taskCategoryRepository.save(any(TaskCategoryEntity.class))).thenReturn(testCategory);
            when(taskCategoryDTLRepository.findByCategoryPoid(categoryPoid)).thenReturn(Collections.emptyList());
            when(roleRepository.findByUserRolePoidIn(anyList())).thenReturn(Collections.emptyList());

            TaskCategoryDto result = taskCategoryService.updateTaskCategory(categoryPoid, taskCategoryDto);

            assertNotNull(result);
            verify(taskCategoryDTLRepository, never()).deleteAll(anyList());
            verify(taskCategoryDTLRepository, never()).saveAll(anyList());
        }
    }

    @Test
    void updateTaskCategory_ShouldHandleNullSubCategories() {
        TaskCategoryDto taskCategoryDto = new TaskCategoryDto();
        taskCategoryDto.setCategoryDescription("Updated Category");
        taskCategoryDto.setSubCategories(null);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn("testUser");

            when(taskCategoryRepository.findByCategoryPoid(categoryPoid)).thenReturn(testCategory);
            when(taskCategoryRepository.save(any(TaskCategoryEntity.class))).thenReturn(testCategory);
            when(taskCategoryDTLRepository.findByCategoryPoid(categoryPoid)).thenReturn(Collections.emptyList());
            when(roleRepository.findByUserRolePoidIn(anyList())).thenReturn(Collections.emptyList());

            TaskCategoryDto result = taskCategoryService.updateTaskCategory(categoryPoid, taskCategoryDto);

            assertNotNull(result);
            verify(taskCategoryDTLRepository, never()).deleteAll(anyList());
            verify(taskCategoryDTLRepository, never()).saveAll(anyList());
        }
    }

    @Test
    void updateTaskCategory_ShouldHandleEmptySubCategories() {
        TaskCategoryDto taskCategoryDto = new TaskCategoryDto();
        taskCategoryDto.setCategoryDescription("Updated Category");
        taskCategoryDto.setSubCategories(Collections.emptyList());

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn("testUser");

            when(taskCategoryRepository.findByCategoryPoid(categoryPoid)).thenReturn(testCategory);
            when(taskCategoryRepository.save(any(TaskCategoryEntity.class))).thenReturn(testCategory);
            when(taskCategoryDTLRepository.findByCategoryPoid(categoryPoid)).thenReturn(Collections.emptyList());
            when(roleRepository.findByUserRolePoidIn(anyList())).thenReturn(Collections.emptyList());

            TaskCategoryDto result = taskCategoryService.updateTaskCategory(categoryPoid, taskCategoryDto);

            assertNotNull(result);
            verify(taskCategoryDTLRepository, never()).deleteAll(anyList());
            verify(taskCategoryDTLRepository, never()).saveAll(anyList());
        }
    }

    @Test
    void updateTaskCategory_ShouldThrowException_WhenCreateSubCategoryAlreadyExists() {
        TaskCategoryDto taskCategoryDto = new TaskCategoryDto();
        TaskCategoryDtlDto subCategoryDto = new TaskCategoryDtlDto();
        subCategoryDto.setSubCategoryDescription("Existing Sub");
        subCategoryDto.setActionType("isCreated");
        taskCategoryDto.setSubCategories(Arrays.asList(subCategoryDto));

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn("testUser");

            when(taskCategoryRepository.findByCategoryPoid(categoryPoid)).thenReturn(testCategory);
            when(taskCategoryRepository.save(any(TaskCategoryEntity.class))).thenReturn(testCategory);
            when(taskCategoryDTLRepository.existsBySubCategoryDescriptionIgnoreCase(anyString())).thenReturn(true);

            assertThrows(ResourceAlreadyExistsException.class,
                    () -> taskCategoryService.updateTaskCategory(categoryPoid, taskCategoryDto));
        }
    }
}
