package com.asg.settings.entity;

import com.asg.common.lib.entity.BaseEntity;
import com.asg.settings.entity.key.TaskCategoryDTLId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@IdClass(TaskCategoryDTLId.class)
@Table(name = "GLOBAL_TASK_CATEGORY_DTL")
public class TaskCategoryDTLEntity extends BaseEntity {

    @Id
    @Column(name = "CATEGORY_POID", nullable = false)
    private Long categoryPoid;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_sub_category_seq")
    @SequenceGenerator(name = "task_sub_category_seq", sequenceName = "GLOBAL_TASK_SUB_CATEGORY_SEQ", allocationSize = 1)
    @Column(name = "DET_ROW_ID", nullable = false)
    private Long detRowId;

    @Column(name = "SUB_CATEGORY_DESCRIPTION", length = 100)
    private String subCategoryDescription;

}
