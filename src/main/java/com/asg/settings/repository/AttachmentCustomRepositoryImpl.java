package com.asg.settings.repository;

import com.asg.common.lib.enums.AttachmentFilterType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AttachmentCustomRepositoryImpl implements AttachmentCustomRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Object[]> fetchActiveAttachments(Long groupPoid, Long companyPoid, String docId, Long docKeyPoid) {
        if (docKeyPoid == null || docKeyPoid <= 0) return List.of();

        StoredProcedureQuery query = em.createStoredProcedureQuery("PROC_ATTACHMENTS_LOADLIST");
        query.registerStoredProcedureParameter("P_GROUP_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_COMPANY_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_DOC_ID", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_DOC_KEY_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("OUTDATA", void.class, ParameterMode.REF_CURSOR);

        query.setParameter("P_GROUP_POID", groupPoid);
        query.setParameter("P_COMPANY_POID", companyPoid);
        query.setParameter("P_DOC_ID", docId);
        query.setParameter("P_DOC_KEY_POID", docKeyPoid);

        return query.getResultList();
    }

    @Override
    public List<Object[]> fetchAllAttachments(Long groupPoid, Long companyPoid, String docId, Long docKeyPoid) {
        if (docKeyPoid == null || docKeyPoid <= 0) return List.of();

        // Use native query to get ACTIVE and DELETED columns since stored procedure doesn't return them
        return em.createNativeQuery(
            "SELECT GROUP_POID, COMPANY_POID, DOC_ID, DOC_KEY_POID, SEQNO, FILE_NAME, " +
            "FILE_REMARKS, CHECKLIST_NAME, CREATED_BY, CREATED_DATE, FILE_NAME_MAPPED, ACTIVE, DELETED " +
            "FROM GLOBAL_ATTACHMENTS " +
            "WHERE DOC_ID = ?1 AND DOC_KEY_POID > 0 AND DOC_KEY_POID = ?2 " +
            "ORDER BY DOC_KEY_POID, SEQNO")
            .setParameter(1, docId)
            .setParameter(2, docKeyPoid)
            .getResultList();
    }

    @Override
    public List<Object[]> fetchDeletedAttachments(Long groupPoid, Long companyPoid, String docId, Long docKeyPoid) {
        if (docKeyPoid == null || docKeyPoid <= 0) return List.of();

        // Use PROC_ATTACHMENTS_LOADLIST_ALL and filter deleted in application layer
        // since there's no specific stored procedure for deleted attachments
        return fetchAllAttachments(groupPoid, companyPoid, docId, docKeyPoid);
    }

    @Override
    public List<Object[]> fetchAttachmentsByFilter(Long groupPoid, Long companyPoid, String docId, Long docKeyPoid, AttachmentFilterType filterType) {
        return switch (filterType) {
            case ACTIVE -> fetchActiveAttachments(groupPoid, companyPoid, docId, docKeyPoid);
            case DELETED -> fetchDeletedAttachments(groupPoid, companyPoid, docId, docKeyPoid);
            case ALL -> fetchAllAttachments(groupPoid, companyPoid, docId, docKeyPoid);
        };
    }
}
