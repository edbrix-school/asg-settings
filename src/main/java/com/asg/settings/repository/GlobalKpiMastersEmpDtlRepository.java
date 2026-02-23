package com.asg.settings.repository;

import com.asg.settings.entity.GlobalKpiMastersEmpDtlEntity;
import com.asg.settings.entity.key.GlobalKpiMastersDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GlobalKpiMastersEmpDtlRepository extends JpaRepository<GlobalKpiMastersEmpDtlEntity, GlobalKpiMastersDtlId> {
    List<GlobalKpiMastersEmpDtlEntity> findByTransactionPoid(Long globalKpiMastersPoid);

    @Query("""
       SELECT COALESCE(MAX(e.id.detRowId), 0)
       FROM GlobalKpiMastersEmpDtlEntity e
       WHERE e.id.transactionPoid = :globalKpiMastersPoid
       """)
    long findMaxDetRowId(@Param("globalKpiMastersPoid")Long globalKpiMastersPoid);

    Optional<GlobalKpiMastersEmpDtlEntity> findByTransactionPoidAndDetRowId(Long globalKpiMastersPoid, Long detRowId);
}
