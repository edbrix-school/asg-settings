package com.asg.settings.repository;

import com.asg.settings.entity.DocMasterApprovalDtlEntity;
import com.asg.settings.entity.key.GlobalDocMasterApprovalDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentApprovalDtlRepository extends JpaRepository<DocMasterApprovalDtlEntity, GlobalDocMasterApprovalDtlId> {
    List<DocMasterApprovalDtlEntity> findAllById_DocId(String docId);
}

