package com.asg.settings.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;

@Repository
public class CurrencyUploadRepository {

    private final DataSource dataSource;
    private static final Logger log = LoggerFactory.getLogger(CurrencyUploadRepository.class);

    public CurrencyUploadRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String callCurrencyUploadProc(Long groupPoid, Long userPoid, Long companyPoid) throws SQLException {
        String sql = "{ call PROC_CURRENCY_RATE_UPLOAD(?, ?, ?, ?) }";
        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setLong(1, groupPoid);
            cs.setLong(2, userPoid);
            cs.setLong(3, companyPoid);
            cs.registerOutParameter(4, Types.VARCHAR);

            cs.execute();
            String result = cs.getString(4);
            log.debug("Stored procedure PROC_CURRENCY_RATE_UPLOAD returned: {}", result);
            return result;

        }
        catch (SQLException e) {
            log.error("Error while calling stored procedure PROC_CURRENCY_RATE_UPLOAD", e);
            return "ERROR: " + e.getMessage();
        }
    }

    public String callCurrencyUpdateProc(Long groupPoid, String currencyCode, LocalDate rateChangeDate, BigDecimal buyRate, BigDecimal sellRate) throws SQLException {
        String sql = "{ call PROC_GLOB_CURRENCY_UPDATE(?, ?, ?, ?, ?, ?) }";
        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setLong(1, groupPoid);
            cs.setString(2, currencyCode);
            
            // Convert LocalDate to java.sql.Date for database
            cs.setDate(3, java.sql.Date.valueOf(rateChangeDate));
            
            cs.setBigDecimal(4, buyRate);
            cs.setBigDecimal(5, sellRate);
            cs.registerOutParameter(6, Types.VARCHAR);

            cs.execute();
            String result = cs.getString(6);
            log.debug("Stored procedure PROC_GLOB_CURRENCY_UPDATE returned: {}", result);
            return result;

        }
        catch (SQLException e) {
            log.error("Error while calling stored procedure PROC_GLOB_CURRENCY_UPDATE", e);
            return "ERROR: " + e.getMessage();
        }
    }
}