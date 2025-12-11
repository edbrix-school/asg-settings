package com.asg.settings.repository;

import com.asg.settings.entity.DocMasterApprovalDtlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentApprovalDtlRepository extends JpaRepository<DocMasterApprovalDtlEntity, String> {

    List<DocMasterApprovalDtlEntity> findAllById_DocId(String docId);
}

