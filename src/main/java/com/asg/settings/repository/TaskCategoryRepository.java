package com.asg.settings.repository;

import com.asg.settings.entity.TaskCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskCategoryRepository extends JpaRepository<TaskCategoryEntity, Integer>, JpaSpecificationExecutor<TaskCategoryEntity> {
    TaskCategoryEntity findByCategoryPoid(Long categoryPoid);

    @Query("SELECT t FROM TaskCategoryEntity t WHERE t.categoryPoid = :categoryPoid AND t.active != 'N' AND t.deleted = 'N'")
    TaskCategoryEntity findActiveByCategoryPoid(@Param("categoryPoid") Long categoryPoid);

    boolean existsByCategoryDescriptionIgnoreCase(String categoryDescription);

    boolean existsByCategoryDescriptionIgnoreCaseAndCategoryPoidNot(String categoryDescription, Long categoryPoid);

}

