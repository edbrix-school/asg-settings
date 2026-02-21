package com.asg.settings.repository;

import com.asg.settings.entity.GlobalKpiMastersCompanyDtlEntity;
import com.asg.settings.entity.key.GlobalKpiMastersDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GlobalKpiMastersCompanyDtlRepository extends JpaRepository<GlobalKpiMastersCompanyDtlEntity, GlobalKpiMastersDtlId> {
    List<GlobalKpiMastersCompanyDtlEntity> findByIdTransactionPoid(Long globalKpiMastersPoid);

    @Query("""
       SELECT COALESCE(MAX(e.id.detRowId), 0)
       FROM GlobalKpiMastersCompanyDtlEntity e
       WHERE e.id.transactionPoid = :globalKpiMastersPoid
       """)
    Long findMaxDetRowId(@Param("globalKpiMastersPoid") Long globalKpiMastersPoid);

    void deleteByIdTransactionPoidAndIdDetRowId(Long globalKpiMastersPoid, Long detRowId);

    Optional<GlobalKpiMastersCompanyDtlEntity> findByIdTransactionPoidAndIdDetRowId(Long globalKpiMastersPoid, Long detRowId);
}
