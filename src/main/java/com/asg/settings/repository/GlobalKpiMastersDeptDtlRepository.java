package com.asg.settings.repository;

import com.asg.settings.entity.GlobalKpiMastersDeptDtlEntity;
import com.asg.settings.entity.key.GlobalKpiMastersDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GlobalKpiMastersDeptDtlRepository extends JpaRepository<GlobalKpiMastersDeptDtlEntity, GlobalKpiMastersDtlId> {
    List<GlobalKpiMastersDeptDtlEntity> findByIdTransactionPoid(Long globalKpiMastersPoid);

    @Query("""
       SELECT COALESCE(MAX(e.id.detRowId), 0)
       FROM GlobalKpiMastersDeptDtlEntity e
       WHERE e.id.transactionPoid = :globalKpiMastersPoid
       """)
    long findMaxDetRowId(@Param("globalKpiMastersPoid")Long globalKpiMastersPoid);

    void deleteByIdTransactionPoidAndIdDetRowId(Long globalKpiMastersPoid, Long detRowId);

    Optional<GlobalKpiMastersDeptDtlEntity> findByIdTransactionPoidAndIdDetRowId(Long globalKpiMastersPoid, Long detRowId);
}
