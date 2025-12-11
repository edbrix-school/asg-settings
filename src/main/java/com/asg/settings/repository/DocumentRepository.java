package com.asg.settings.repository;

import com.asg.common.lib.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, String>, JpaSpecificationExecutor<DocumentEntity> {

    DocumentEntity findByDocId(String docId);

    boolean existsByDocId(String docId);

    boolean existsByDocPoid(Long docPoid);

    List<DocumentEntity> findAllByActiveAndDeleted(String active, String deleted);

    DocumentEntity findByDocPoid(Long docPoid);

}

