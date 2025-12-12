package com.asg.settings.entity.key;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class TaskCategoryDTLId implements Serializable {

    private Long categoryPoid;
    private Long detRowId;


    public TaskCategoryDTLId(Long categoryPoid, Long detRowId) {
        this.categoryPoid = categoryPoid;
        this.detRowId = detRowId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskCategoryDTLId)) return false;
        TaskCategoryDTLId that = (TaskCategoryDTLId) o;
        return Objects.equals(categoryPoid, that.categoryPoid) &&
                Objects.equals(detRowId, that.detRowId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryPoid, detRowId);
    }

}

