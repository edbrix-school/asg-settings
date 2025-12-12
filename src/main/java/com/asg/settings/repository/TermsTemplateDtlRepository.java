package com.asg.settings.repository;

import com.asg.settings.entity.TermsTemplateDtlEntity;
import com.asg.settings.entity.key.TermsTemplateDtlKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TermsTemplateDtlRepository extends JpaRepository<TermsTemplateDtlEntity, TermsTemplateDtlKey> {

     List<TermsTemplateDtlEntity> findAllById_TermsPoidAndActive(Long termsPoid, String active);

    TermsTemplateDtlEntity findById_TermsPoidAndClauseNo(Long termsPoid, String clauseNo);

    @Query("SELECT COALESCE(MAX(d.id.detRowId), 0) + 1 " +
            "FROM TermsTemplateDtlEntity d " +
            "WHERE d.id.termsPoid = :termsPoid")
    Long getNextDetRowId(@Param("termsPoid") Long termsPoid);

    List<TermsTemplateDtlEntity> findAllById_TermsPoid(Long termsPoid);

    @Query("SELECT DISTINCT d FROM TermsTemplateDtlEntity d " +
            "WHERE d.id.termsPoid = :termsPoid " +
            "AND d.clauseNo = :clauseNo " +
            "AND d.active = :active")
    List<TermsTemplateDtlEntity> findDistinctById_TermsPoidAndClauseNoAndActive(
            @Param("termsPoid") Long termsPoid,
            @Param("clauseNo") String clauseNo,
            @Param("active") String active
    );

}
