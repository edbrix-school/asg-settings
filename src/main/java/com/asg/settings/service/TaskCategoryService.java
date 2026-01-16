package com.asg.settings.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.settings.dto.TaskCategoryDto;
import com.asg.settings.dto.TaskSubCategoryDto;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface TaskCategoryService {

    TaskCategoryDto getTaskCategory(Long categoryPoid);

    ResponseEntity<?> softDeleteTaskCategory(Long categoryPoid, DeleteReasonDto reasonDto);

    Map<String, Object> listTaskCategories(String docId, FilterRequestDto request, Pageable pageable);

    TaskCategoryDto createTaskCategory(TaskCategoryDto taskCategoryDto);

    TaskCategoryDto updateTaskCategory(Long categoryPoid, TaskCategoryDto taskCategoryDto);

    List<TaskSubCategoryDto> getSubCategoriesByCategoryPoid(Long categoryPoid);

    Boolean isCategoryExistsByDescriptionAndCategoryPoid(String categoryDescription, Long categoryPoid);
}

