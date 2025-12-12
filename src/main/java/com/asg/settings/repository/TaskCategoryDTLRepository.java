package com.asg.settings.repository;

import com.asg.settings.entity.TaskCategoryDTLEntity;
import com.asg.settings.entity.key.TaskCategoryDTLId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskCategoryDTLRepository extends JpaRepository<TaskCategoryDTLEntity, TaskCategoryDTLId> {
    List<TaskCategoryDTLEntity> findByCategoryPoid(Long categoryPoid);

    Optional<TaskCategoryDTLEntity> findByCategoryPoidAndDetRowId(Long categoryPoid, Long detRowId);

    boolean existsBySubCategoryDescriptionIgnoreCase(String subCategoryDescription);
}