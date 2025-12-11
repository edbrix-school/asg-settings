package com.asg.settings.repository;

import com.asg.settings.entity.DocMasterAuthDtlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentAuthDtlRepository extends JpaRepository<DocMasterAuthDtlEntity, String> {

    List<DocMasterAuthDtlEntity> findAllById_DocId(String docId);
}

