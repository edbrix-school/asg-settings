package com.asg.settings.repository;

import com.asg.settings.entity.GlobalKpiMastersLineDtlEntity;
import com.asg.settings.entity.key.GlobalKpiMastersDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GlobalKpiMastersLineDtlRepository extends JpaRepository<GlobalKpiMastersLineDtlEntity, GlobalKpiMastersDtlId> {
    List<GlobalKpiMastersLineDtlEntity> findByTransactionPoid(Long globalKpiMastersPoid);

    @Query("""
       SELECT COALESCE(MAX(e.id.detRowId), 0)
       FROM GlobalKpiMastersLineDtlEntity e
       WHERE e.id.transactionPoid = :poid
       """)
    Long findMaxDetRowId(@Param("poid") Long poid);

    Optional<GlobalKpiMastersLineDtlEntity> findByTransactionPoidAndDetRowId(Long globalKpiMastersPoid, Long detRowId);
}
