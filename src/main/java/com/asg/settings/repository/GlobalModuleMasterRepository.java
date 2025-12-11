package com.asg.settings.repository;

import com.asg.settings.entity.GlobalModuleMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalModuleMasterRepository extends JpaRepository<GlobalModuleMasterEntity, String> {
    GlobalModuleMasterEntity findByModuleId(String moduleId);
}

