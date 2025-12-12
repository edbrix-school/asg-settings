package com.asg.settings.repository;

import com.asg.settings.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;


public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    Task findByTransactionPoid(Long TaskPoid);

    @Query(value = """
            SELECT COALESCE(MAX(TO_NUMBER(REGEXP_SUBSTR(DOC_REF, '[0-9]+'))), 0)
            FROM GLOBAL_TASK_HDR
            WHERE TASK_CATEGORY = :category
            """, nativeQuery = true)
    Long findMaxDocRefNumberByCategory(@Param("category") String category);

    //  New for Get Task by docRefId
    @Query("SELECT t FROM Task t WHERE t.transactionPoid = :transactionPoid")
    Task findByTransactionPoid(@Param("transactionPoid") String transactionPoid);

    @Query("SELECT t FROM Task t WHERE t.transactionPoid = :taskPoid")
    Task findByTaskPoid(@Param("taskPoid") Long taskPoid);

    @Modifying
    @Query("UPDATE Task t SET t.deleted = 'Y', t.lastModifiedBy = :updatedBy, t.lastModifiedDate = :updatedDate WHERE t.transactionPoid = :taskPoid")
    int softDeleteTask(@Param("taskPoid") Long taskPoid,
                       @Param("updatedBy") String updatedBy,
                       @Param("updatedDate") LocalDateTime updatedDate);

}
