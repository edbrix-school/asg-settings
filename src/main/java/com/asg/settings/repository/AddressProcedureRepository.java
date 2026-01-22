package com.asg.settings.repository;

import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AddressProcedureRepository {

    private final EntityManager entityManager;
    private final LoggingService loggingService;

    public String createAllTypes(Long groupPoid, Long userPoid, Long companyPoid, Long addressPoid) {
        StoredProcedureQuery query = entityManager
                .createStoredProcedureQuery("PROC_ADDRESS_TYPE_CREATE_ALL")
                .registerStoredProcedureParameter("P_LOGIN_GROUP_POID", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_LOGIN_USER_POID", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_LOGIN_COMPANY_POID", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_ADDRESS_POID", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_STATUS", String.class, ParameterMode.OUT)
                .setParameter("P_LOGIN_GROUP_POID", groupPoid)
                .setParameter("P_LOGIN_USER_POID", userPoid)
                .setParameter("P_LOGIN_COMPANY_POID", companyPoid)
                .setParameter("P_ADDRESS_POID", addressPoid);

        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), addressPoid.toString(), "Created all address types from address master...");

        query.execute();
        return (String) query.getOutputParameterValue("P_STATUS");
    }

    public String copyAllTypes(Long groupPoid, Long userPoid, Long companyPoid, Long targetPoid) {
        StoredProcedureQuery query = entityManager
                .createStoredProcedureQuery("PROC_ADDRESS_TYPE_UPDATE_ALL")
                .registerStoredProcedureParameter("P_LOGIN_GROUP_POID", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_LOGIN_USER_POID", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_LOGIN_COMPANY_POID", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_ADDRESS_POID", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_STATUS", String.class, ParameterMode.OUT)
                .setParameter("P_LOGIN_GROUP_POID", groupPoid)
                .setParameter("P_LOGIN_USER_POID", userPoid)
                .setParameter("P_LOGIN_COMPANY_POID", companyPoid)
                .setParameter("P_ADDRESS_POID", targetPoid);
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), targetPoid.toString(), "Copied all address types from first address...");
        query.execute();
        return (String) query.getOutputParameterValue("P_STATUS");
    }
}
