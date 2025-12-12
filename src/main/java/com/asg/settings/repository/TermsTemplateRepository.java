package com.asg.settings.repository;

import com.asg.settings.entity.TermsTemplateEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TermsTemplateRepository extends JpaRepository<TermsTemplateEntity, Long>, JpaSpecificationExecutor<TermsTemplateEntity> {


    TermsTemplateEntity findByTermsPoidAndActive(Long termsPoid,String active);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE TermsTemplateEntity t " +
            "SET t.active = 'N', t.deleted = 'Y', t.lastModifiedDate = CURRENT_TIMESTAMP " +
            "WHERE t.termsPoid = :termsPoid")
    int softDeleteByTermsPoid(@Param("termsPoid") Long termsPoid);

    Optional<TermsTemplateEntity> findByTermsPoid(Long termsPoid);


}
