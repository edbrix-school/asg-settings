package com.asg.settings.repository;

import com.asg.settings.entity.DivisionMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DivisionRepository extends JpaRepository<DivisionMasterEntity, Long>, JpaSpecificationExecutor<DivisionMasterEntity> {

    boolean existsByDivisionCodeAndDeleted(String divisionCode, Integer deleted);

    java.util.Optional<DivisionMasterEntity> findByDivisionIdAndDeleted(Long divisionId, Integer deleted);

}
