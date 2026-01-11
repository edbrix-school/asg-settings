package com.asg.settings.service.impl;

import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.LovGetListDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.exception.ResourceAlreadyExistsException;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.utility.ASGHelperUtils;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.settings.dto.TaskCategoryDtlDto;
import com.asg.settings.dto.TaskCategoryDto;
import com.asg.settings.dto.TaskSubCategoryDto;
import com.asg.settings.entity.RoleEntity;
import com.asg.settings.entity.TaskCategoryDTLEntity;
import com.asg.settings.entity.TaskCategoryEntity;
import com.asg.settings.repository.RoleRepository;
import com.asg.settings.repository.TaskCategoryDTLRepository;
import com.asg.settings.repository.TaskCategoryRepository;
import com.asg.settings.service.TaskCategoryService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.asg.common.lib.dto.response.ApiResponse.notFound;
import static com.asg.common.lib.dto.response.ApiResponse.success;

@Slf4j
@Service
public class TaskCategoryServiceImpl implements TaskCategoryService {

    @Autowired
    private TaskCategoryRepository taskCategoryRepository;
    @Autowired
    private TaskCategoryDTLRepository taskCategoryDTLRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private DocumentSearchService documentService;

    @Autowired
    private LoggingService loggingService;

    @Override
    public TaskCategoryDto getTaskCategory(Long categoryPoid) {
        TaskCategoryEntity taskCategoryEntity = taskCategoryRepository.findActiveByCategoryPoid(categoryPoid);

        if (taskCategoryEntity == null) {
            throw new ResourceNotFoundException("Task Category", "categoryPoid", categoryPoid);
        }

        List<TaskCategoryDTLEntity> taskCategoryDTLEntityList = taskCategoryDTLRepository.findByCategoryPoid(categoryPoid);

        TaskCategoryDto taskCategoryDto = entityToDto(taskCategoryEntity);

        List<TaskCategoryDtlDto> taskCategoryDtlDtoList = new ArrayList<>();
        if (!taskCategoryDTLEntityList.isEmpty()) {
            taskCategoryDTLEntityList.forEach(taskCategoryDTLEntity -> {
                TaskCategoryDtlDto taskCategoryDtlDto = new TaskCategoryDtlDto();
                taskCategoryDtlDto.setCategoryPoid(taskCategoryDTLEntity.getCategoryPoid());
                taskCategoryDtlDto.setSubCategoryDescription(taskCategoryDTLEntity.getSubCategoryDescription());
                taskCategoryDtlDto.setDetRowId(taskCategoryDTLEntity.getDetRowId());
                taskCategoryDtlDtoList.add(taskCategoryDtlDto);
            });
        }
        taskCategoryDto.setSubCategories(taskCategoryDtlDtoList);
        return taskCategoryDto;
    }

    private TaskCategoryDto entityToDto(TaskCategoryEntity taskCategoryEntity) {
        TaskCategoryDto taskCategoryDto = new TaskCategoryDto();
        taskCategoryDto.setCategoryPoid(taskCategoryEntity.getCategoryPoid());
        taskCategoryDto.setCategoryDescription(taskCategoryEntity.getCategoryDescription());
        taskCategoryDto.setUserRolePoid(ASGHelperUtils.convertFromStringToList(taskCategoryEntity.getUserRolePoid()));

        List<Long> userRolePoidList = ASGHelperUtils.convertFromStringToLongList(taskCategoryEntity.getUserRolePoid());

        List<LovGetListDto> userRolePoidDet = new ArrayList<>();

        if (userRolePoidList != null && !userRolePoidList.isEmpty()) {
            List<RoleEntity> userRoleEntities = roleRepository.findByUserRolePoidIn(userRolePoidList);

            for (RoleEntity userRoleEntity : userRoleEntities) {
                if (userRoleEntity != null) {
                    LovGetListDto lovDto = new LovGetListDto();
                    lovDto.setPoid(userRoleEntity.getUserRolePoid());
                    lovDto.setCode(userRoleEntity.getUserRoleId());
                    lovDto.setLabel(userRoleEntity.getUserRoleName());
                    lovDto.setValue(userRoleEntity.getUserRolePoid());
                    lovDto.setDescription(userRoleEntity.getUserRoleName());
                    lovDto.setSeqNo(0);
                    userRolePoidDet.add(lovDto);
                }
            }
        }
        taskCategoryDto.setUserRolePoidDet(userRolePoidDet);

        // Set categoryDet dropdown object
        LovGetListDto categoryDet = new LovGetListDto();
        categoryDet.setPoid(taskCategoryEntity.getCategoryPoid());
        categoryDet.setCode(taskCategoryEntity.getCategoryCode());
        categoryDet.setLabel(taskCategoryEntity.getCategoryDescription());
        categoryDet.setValue(taskCategoryEntity.getCategoryPoid());
        categoryDet.setDescription(taskCategoryEntity.getCategoryDescription());
        categoryDet.setSeqNo(taskCategoryEntity.getSeqNo());
        taskCategoryDto.setCategoryDet(categoryDet);

        taskCategoryDto.setActive(taskCategoryEntity.getActive());
        taskCategoryDto.setSeqNo(taskCategoryEntity.getSeqNo());
        taskCategoryDto.setDeleted(taskCategoryEntity.getDeleted());
        taskCategoryDto.setDeleted(taskCategoryEntity.getDeleted());
        taskCategoryDto.setCategoryCode(taskCategoryEntity.getCategoryCode());

        return taskCategoryDto;
    }

    @Override
    @Transactional
    public ResponseEntity<?> softDeleteTaskCategory(Long categoryPoid, String updatedBy) {

        // Fetch the header category
        TaskCategoryEntity category = taskCategoryRepository.findByCategoryPoid(categoryPoid);
        if (category == null) {
            return notFound("Task Category not found for id: " + categoryPoid);
        }
        LocalDateTime now = LocalDateTime.now();

        // Idempotency check
        if ("N".equalsIgnoreCase(category.getActive())) {
            return success("Task Category already soft-deleted");
        }
        // Save old values for logging
        String oldActive = category.getActive();
        String oldDeleted = category.getDeleted();

        // Soft-delete header only
        category.setActive("N");
        category.setDeleted("Y");
        category.setLastModifiedBy(updatedBy);
        category.setLastModifiedDate(now);
        taskCategoryRepository.save(category);

        loggingService.createLogSummaryEntry(LogDetailsEnum.DELETED, UserContext.getDocumentId(), categoryPoid.toString());
        loggingService.logSimpleFieldChange(TaskCategoryEntity.class, UserContext.getDocumentId(), categoryPoid.toString(), "active", oldActive, "N", "Task Category soft-deleted");
        loggingService.logSimpleFieldChange(TaskCategoryEntity.class, UserContext.getDocumentId(), categoryPoid.toString(), "deleted", oldDeleted, "Y", "Task Category soft-deleted");

        return success("Task Category soft-deleted successfully");
    }

    @Override
    public Map<String, Object> listTaskCategories(String docId, FilterRequestDto request, Pageable pageable) {
        String operator = documentService.resolveOperator(request);
        String isDeleted = documentService.resolveIsDeleted(request);
        List<FilterDto> filters = documentService.resolveFilters(request);

        RawSearchResult raw = documentService.search(docId, filters, operator, pageable, isDeleted,
                "CATEGORY_DESCRIPTION",   // label
                "CATEGORY_POID");         // value

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    @Override
    @Transactional
    public TaskCategoryDto createTaskCategory(TaskCategoryDto taskCategoryDto) {
        if (taskCategoryRepository.existsByCategoryDescriptionIgnoreCase(taskCategoryDto.getCategoryDescription())) {
            throw new ResourceAlreadyExistsException("Category Description", taskCategoryDto.getCategoryDescription());
        }
        TaskCategoryEntity taskCategoryEntity = new TaskCategoryEntity();
        taskCategoryEntity.setCategoryDescription(taskCategoryDto.getCategoryDescription());
        taskCategoryEntity.setUserRolePoid(ASGHelperUtils.convertListToString(taskCategoryDto.getUserRolePoid()));
        taskCategoryEntity.setSeqNo(taskCategoryDto.getSeqNo());
        taskCategoryEntity.setActive(StringUtils.isBlank(taskCategoryDto.getActive()) ? "Y" : taskCategoryDto.getActive());
        taskCategoryEntity.setDeleted(StringUtils.isBlank(taskCategoryDto.getDeleted()) ? "N" : taskCategoryDto.getDeleted());
        taskCategoryEntity.setCategoryCode(taskCategoryDto.getCategoryCode());
        taskCategoryEntity.setCreatedBy(getCurrentUser());
        taskCategoryEntity.setCreatedDate(LocalDateTime.now());
        taskCategoryEntity.setLastModifiedBy(getCurrentUser());
        taskCategoryEntity.setLastModifiedDate(LocalDateTime.now());

        TaskCategoryEntity savedEntity = taskCategoryRepository.save(taskCategoryEntity);
        Long categoryPoid = savedEntity.getCategoryPoid();

        if (taskCategoryDto.getSubCategories() != null && !taskCategoryDto.getSubCategories().isEmpty()) {
            List<TaskCategoryDTLEntity> subCategoriesToSave = new ArrayList<>();

            for (TaskCategoryDtlDto subCategoryDto : taskCategoryDto.getSubCategories()) {
                if (taskCategoryDTLRepository.existsBySubCategoryDescriptionIgnoreCase(subCategoryDto.getSubCategoryDescription())) {
                    throw new ResourceAlreadyExistsException("Sub Category Description", subCategoryDto.getSubCategoryDescription());
                }
                TaskCategoryDTLEntity subCategoryEntity = new TaskCategoryDTLEntity();
                subCategoryEntity.setCategoryPoid(categoryPoid);
                subCategoryEntity.setSubCategoryDescription(subCategoryDto.getSubCategoryDescription());
                subCategoryEntity.setCreatedBy(getCurrentUser());
                subCategoryEntity.setCreatedDate(LocalDateTime.now());
                subCategoryEntity.setLastModifiedBy(getCurrentUser());
                subCategoryEntity.setLastModifiedDate(LocalDateTime.now());
                subCategoriesToSave.add(subCategoryEntity);
            }
            taskCategoryDTLRepository.saveAll(subCategoriesToSave);
        }
        String docId = UserContext.getDocumentId();
        String key = savedEntity.getCategoryPoid().toString();
        loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, docId, key);

        return getTaskCategory(categoryPoid);
    }

    @Override
    @Transactional
    public TaskCategoryDto updateTaskCategory(Long categoryPoid, TaskCategoryDto taskCategoryDto) {
        if (taskCategoryRepository.existsByCategoryDescriptionIgnoreCaseAndCategoryPoidNot(
                taskCategoryDto.getCategoryDescription(), categoryPoid)) {
            throw new ResourceAlreadyExistsException("Category Description", taskCategoryDto.getCategoryDescription());
        }

        TaskCategoryEntity taskCategoryEntity = taskCategoryRepository.findByCategoryPoid(categoryPoid);
        if (taskCategoryEntity == null) {
            throw new ResourceNotFoundException("Task Category", "categoryPoid", categoryPoid);
        }

        TaskCategoryEntity oldEntity = new TaskCategoryEntity();
        BeanUtils.copyProperties(taskCategoryEntity, oldEntity);

        taskCategoryEntity.setCategoryDescription(taskCategoryDto.getCategoryDescription());
        taskCategoryEntity.setUserRolePoid(ASGHelperUtils.convertListToString(taskCategoryDto.getUserRolePoid()));
        taskCategoryEntity.setSeqNo(taskCategoryDto.getSeqNo());
        taskCategoryEntity.setActive(StringUtils.isBlank(taskCategoryDto.getActive()) ? "Y" : taskCategoryDto.getActive());
        taskCategoryEntity.setDeleted(StringUtils.isBlank(taskCategoryDto.getDeleted()) ? "N" : taskCategoryDto.getDeleted());
        taskCategoryEntity.setCategoryCode(taskCategoryDto.getCategoryCode());
        taskCategoryEntity.setLastModifiedBy(getCurrentUser());
        taskCategoryEntity.setLastModifiedDate(LocalDateTime.now());

        taskCategoryRepository.save(taskCategoryEntity);
        if (taskCategoryDto.getSubCategories() != null && !taskCategoryDto.getSubCategories().isEmpty()) {
            processSubCategories(categoryPoid, taskCategoryDto.getSubCategories());
        }
        loggingService.logChanges(oldEntity, taskCategoryEntity, TaskCategoryEntity.class, UserContext.getDocumentId(), categoryPoid.toString(), LogDetailsEnum.MODIFIED, "CATEGORY_POID");
        return getTaskCategory(categoryPoid);
    }

    private void processSubCategories(Long categoryPoid, List<TaskCategoryDtlDto> subCategories) {
        List<TaskCategoryDTLEntity> entitiesToDelete = new ArrayList<>();
        List<TaskCategoryDTLEntity> entitiesToSave = new ArrayList<>();

        for (TaskCategoryDtlDto dto : subCategories) {
            String action = StringUtils.isBlank(dto.getActionType()) ? "" : dto.getActionType().toLowerCase();

            switch (action) {
                case "isdeleted" -> handleDeleteAction(categoryPoid, dto, entitiesToDelete);
                case "iscreated", "isupdated" -> handleCreateOrUpdateAction(categoryPoid, dto, entitiesToSave);
                default -> log.warn("Unknown actionType '{}' for detRowId={}", dto.getActionType(), dto.getDetRowId());
            }
        }
        if (!entitiesToDelete.isEmpty()) {
            taskCategoryDTLRepository.deleteAll(entitiesToDelete);
        }
        if (!entitiesToSave.isEmpty()) {
            taskCategoryDTLRepository.saveAll(entitiesToSave);
        }
    }

    private void handleDeleteAction(Long categoryPoid, TaskCategoryDtlDto subCategoryDto, List<TaskCategoryDTLEntity> entitiesToDelete) {
        if (subCategoryDto.getDetRowId() != null) {
            taskCategoryDTLRepository.findByCategoryPoidAndDetRowId(categoryPoid, subCategoryDto.getDetRowId())
                    .ifPresentOrElse(
                            entitiesToDelete::add,
                            () -> log.warn("No TaskCategoryDTLEntity found for categoryPoid={} and detRowId={}, skipping delete.",
                                    categoryPoid, subCategoryDto.getDetRowId())
                    );
        } else {
            log.warn("detRowId is null for categoryPoid={}, skipping delete.", categoryPoid);
        }
    }

    private void handleCreateOrUpdateAction(Long categoryPoid, TaskCategoryDtlDto subCategoryDto, List<TaskCategoryDTLEntity> entitiesToSave) {
        if (subCategoryDto.getDetRowId() != null) {
            taskCategoryDTLRepository.findByCategoryPoidAndDetRowId(categoryPoid, subCategoryDto.getDetRowId())
                    .ifPresentOrElse(
                            existingEntity -> updateExistingEntity(existingEntity, subCategoryDto, entitiesToSave),
                            () -> createNewEntity(categoryPoid, subCategoryDto, entitiesToSave)
                    );
        } else {
            createNewEntity(categoryPoid, subCategoryDto, entitiesToSave);
        }
    }

    private void updateExistingEntity(TaskCategoryDTLEntity entity, TaskCategoryDtlDto dto, List<TaskCategoryDTLEntity> entitiesToSave) {
        entity.setSubCategoryDescription(dto.getSubCategoryDescription());
        entity.setLastModifiedBy(getCurrentUser());
        entity.setLastModifiedDate(LocalDateTime.now());
        entitiesToSave.add(entity);
    }

    private void createNewEntity(Long categoryPoid, TaskCategoryDtlDto dto, List<TaskCategoryDTLEntity> entitiesToSave) {
        if (taskCategoryDTLRepository.existsBySubCategoryDescriptionIgnoreCase(dto.getSubCategoryDescription())) {
            throw new ResourceAlreadyExistsException("Sub Category Description", dto.getSubCategoryDescription());
        }
        TaskCategoryDTLEntity newEntity = new TaskCategoryDTLEntity();
        newEntity.setCategoryPoid(categoryPoid);
        newEntity.setSubCategoryDescription(dto.getSubCategoryDescription());
        newEntity.setCreatedBy(getCurrentUser());
        newEntity.setCreatedDate(LocalDateTime.now());
        newEntity.setLastModifiedBy(getCurrentUser());
        newEntity.setLastModifiedDate(LocalDateTime.now());
        entitiesToSave.add(newEntity);
    }

    private String getCurrentUser() {
        return UserContext.getUserId() != null ? String.valueOf(UserContext.getUserId()) : "SYSTEM";
    }

    @Override
    public List<TaskSubCategoryDto> getSubCategoriesByCategoryPoid(Long categoryPoid) {
        List<TaskCategoryDTLEntity> taskCategoryDTLEntityList = taskCategoryDTLRepository.findByCategoryPoid(categoryPoid);

        List<TaskSubCategoryDto> taskCategoryDtlDtoList = new ArrayList<>();

        if (!taskCategoryDTLEntityList.isEmpty()) {
            taskCategoryDTLEntityList.forEach(taskCategoryDTLEntity -> {
                TaskSubCategoryDto taskSubCategoryDto = new TaskSubCategoryDto();
                Long combinedPoid = taskCategoryDTLEntity.getCategoryPoid() + taskCategoryDTLEntity.getDetRowId();
                taskSubCategoryDto.setDescription(taskCategoryDTLEntity.getSubCategoryDescription());
                taskSubCategoryDto.setCode(taskCategoryDTLEntity.getSubCategoryDescription());
                taskSubCategoryDto.setPoid(combinedPoid);
                taskSubCategoryDto.setLabel(taskCategoryDTLEntity.getSubCategoryDescription());
                taskSubCategoryDto.setValue(combinedPoid);
                taskCategoryDtlDtoList.add(taskSubCategoryDto);
            });
        }
        return taskCategoryDtlDtoList;
    }

    @Override
    public Boolean isCategoryExistsByDescriptionAndCategoryPoid(String categoryDescription, Long categoryPoid) {
        try {
            if (categoryPoid != null) {
                // For update scenarios - exclude the current category
                return taskCategoryRepository.existsByCategoryDescriptionIgnoreCaseAndCategoryPoidNot(categoryDescription, categoryPoid);
            } else {
                // For create scenarios - check if any category exists with this description
                return taskCategoryRepository.existsByCategoryDescriptionIgnoreCase(categoryDescription);
            }
        } catch (Exception e) {
            log.error("Error checking if category exists by description: {}", e.getMessage());
            return false;
        }
    }
}
