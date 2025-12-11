package com.asg.settings.repository;

import com.asg.settings.entity.AlertConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertConfigRepository extends JpaRepository<AlertConfigEntity, Long>, JpaSpecificationExecutor<AlertConfigEntity> {

    AlertConfigEntity findByConfigPoid(Long configId);

    List<AlertConfigEntity> findAllByActiveAndDeleted(String active, String deleted);
}
