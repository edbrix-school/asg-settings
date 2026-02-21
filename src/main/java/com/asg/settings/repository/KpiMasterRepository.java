package com.asg.settings.repository;

import com.asg.common.lib.exception.ValidationException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.settings.dto.response.KpiCompanyMasterResponseDto;
import com.asg.settings.dto.response.KpiEmployeeMasterResponseDto;
import com.asg.settings.dto.response.KpiLineMasterResponseDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class KpiMasterRepository {

    private final EntityManager entityManager;

    public List<KpiLineMasterResponseDto> getAllLines() {

        StoredProcedureQuery query =
                entityManager.createStoredProcedureQuery("PROC_GET_KPI_MASTER_ALL_LINES");

        query.registerStoredProcedureParameter("P_LOGIN_GROUP_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_LOGIN_COMPANY_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_LOGIN_USER_POID", Long.class, ParameterMode.IN);

        query.registerStoredProcedureParameter("OUTDATA", void.class, ParameterMode.REF_CURSOR);
        query.registerStoredProcedureParameter("P_RESULT", String.class, ParameterMode.OUT);

        query.setParameter("P_LOGIN_GROUP_POID", UserContext.getGroupPoid());
        query.setParameter("P_LOGIN_COMPANY_POID", UserContext.getCompanyPoid());
        query.setParameter("P_LOGIN_USER_POID", UserContext.getUserPoid());

        query.execute();

        String status = (String) query.getOutputParameterValue("P_RESULT");

        if (status != null && status.startsWith("ERROR")) {
            log.error("Error from PROC_GET_KPI_MASTER_ALL_LINES: {}", status);
            throw new ValidationException(status);
        }

        ResultSet rs = (ResultSet) query.getOutputParameterValue("OUTDATA");

        List<KpiLineMasterResponseDto> result = new ArrayList<>();

        if (rs == null) {
            return result;
        }

        try {
            while (rs.next()) {
                result.add(
                        KpiLineMasterResponseDto.builder()
                                .linePoid(rs.getLong("LINE_POID"))
                                .lineCode(rs.getString("LINE_CODE"))
                                .lineName(rs.getString("LINE_NAME"))
                                .build()
                );
            }
        } catch (Exception e) {
            log.error("Error reading KPI Line cursor", e);
            throw new ValidationException("Error loading KPI Line Master: " + e.getMessage());
        }

        return result;
    }

    public List<KpiCompanyMasterResponseDto> getAllCompanies() {

        StoredProcedureQuery query =
                entityManager.createStoredProcedureQuery("PROC_GET_KPI_MASTER_ALL_COMPANIES");

        query.registerStoredProcedureParameter("P_LOGIN_GROUP_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_LOGIN_COMPANY_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_LOGIN_USER_POID", Long.class, ParameterMode.IN);

        query.registerStoredProcedureParameter("OUTDATA", void.class, ParameterMode.REF_CURSOR);
        query.registerStoredProcedureParameter("P_RESULT", String.class, ParameterMode.OUT);

        query.setParameter("P_LOGIN_GROUP_POID", UserContext.getGroupPoid());
        query.setParameter("P_LOGIN_COMPANY_POID", UserContext.getCompanyPoid());
        query.setParameter("P_LOGIN_USER_POID", UserContext.getUserPoid());

        query.execute();

        String status = (String) query.getOutputParameterValue("P_RESULT");

        if (status != null && status.startsWith("ERROR")) {
            log.error("Error from PROC_GET_KPI_MASTER_ALL_COMPANIES: {}", status);
            throw new ValidationException(status);
        }

        ResultSet rs = (ResultSet) query.getOutputParameterValue("OUTDATA");

        List<KpiCompanyMasterResponseDto> result = new ArrayList<>();

        if (rs == null) {
            return result;
        }

        try {
            while (rs.next()) {
                result.add(
                        KpiCompanyMasterResponseDto.builder()
                                .companyPoid(rs.getLong("COMPANY_POID"))
                                .companyCode(rs.getString("COMPANY_CODE"))
                                .companyName(rs.getString("COMPANY_NAME"))
                                .build()
                );
            }
        } catch (Exception e) {
            log.error("Error reading KPI Company cursor", e);
            throw new ValidationException("Error loading KPI Company Master: " + e.getMessage());
        }

        return result;
    }

    public List<KpiEmployeeMasterResponseDto> getAllEmployees() {

        StoredProcedureQuery query =
                entityManager.createStoredProcedureQuery("PROC_GET_KPI_MASTER_ALL_EMPLOYEES");

        query.registerStoredProcedureParameter("P_LOGIN_GROUP_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_LOGIN_COMPANY_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_LOGIN_USER_POID", Long.class, ParameterMode.IN);

        query.registerStoredProcedureParameter("OUTDATA", void.class, ParameterMode.REF_CURSOR);
        query.registerStoredProcedureParameter("P_RESULT", String.class, ParameterMode.OUT);

        query.setParameter("P_LOGIN_GROUP_POID", UserContext.getGroupPoid());
        query.setParameter("P_LOGIN_COMPANY_POID", UserContext.getCompanyPoid());
        query.setParameter("P_LOGIN_USER_POID", UserContext.getUserPoid());

        query.execute();

        String status = (String) query.getOutputParameterValue("P_RESULT");

        if (status != null && status.startsWith("ERROR")) {
            log.error("Error from PROC_GET_KPI_MASTER_ALL_EMPLOYEES: {}", status);
            throw new ValidationException(status);
        }

        ResultSet rs = (ResultSet) query.getOutputParameterValue("OUTDATA");

        List<KpiEmployeeMasterResponseDto> result = new ArrayList<>();

        if (rs == null) {
            return result;
        }

        try {
            while (rs.next()) {
                result.add(
                        KpiEmployeeMasterResponseDto.builder()
                                .employeePoid(rs.getLong("EMPLOYEE_POID"))
                                .employeeCode(rs.getString("EMPLOYEE_CODE"))
                                .employeeName(rs.getString("EMPLOYEE_NAME"))
                                .build()
                );
            }
        } catch (Exception e) {
            log.error("Error reading KPI Employee cursor", e);
            throw new ValidationException("Error loading KPI Employee Master: " + e.getMessage());
        }

        return result;
    }


}
