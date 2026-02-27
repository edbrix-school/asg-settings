package com.asg.settings.repository;

import com.asg.settings.entity.GlobalKpiMastersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalKpiMastersRepository extends JpaRepository<GlobalKpiMastersEntity, Long> {


    boolean existsByKpiNameIgnoreCase(String kpiName);

    boolean existsByKpiNameIgnoreCaseAndGlobalKpiMastersPoidNot(
            String kpiName,
            Long globalKpiMastersPoid
    );
}
