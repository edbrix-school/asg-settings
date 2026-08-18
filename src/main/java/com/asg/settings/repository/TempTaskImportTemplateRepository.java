package com.asg.settings.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

@Repository
public class TempTaskImportTemplateRepository {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private static final Logger log = LoggerFactory.getLogger(TempTaskImportTemplateRepository.class);

    public TempTaskImportTemplateRepository(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    public void clearTempTable() {
        jdbcTemplate.update("DELETE FROM TEMP_TASK_IMPORT_TEMPLATE");
    }

    public void insertTemp(String category, String subCategory, String description, String priority, String reportedBy, String allocatedTo, String startDate, String dueDate, String taskType) {
        jdbcTemplate.update(
                "INSERT INTO TEMP_TASK_IMPORT_TEMPLATE (CATEGORY, SUBCATEGORY, TASKDESCRIPTION, PRIORITY, REPORTEDBY, ALLOCATEDTO, STARTDATE, DUEDATE, TASK_TYPE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                 category,  subCategory,  description,  priority,  reportedBy,  allocatedTo, startDate,  dueDate, taskType
        );
    }

    // Need this as second step to get data from temp table and put into actual and then delete temp table
    public String importTasksFromExcel(Long userPoid, Long companyPoid) throws SQLException {
        String sql = "{ call PROC_TASK_IMPORT_FROM_EXCEL(?, ?, ?) }";
        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setLong(1, userPoid);
            cs.setLong(2, companyPoid);
            cs.registerOutParameter(3, Types.VARCHAR);

            cs.execute();
            return cs.getString(3); // Return the result message from the procedure
        }
        catch (SQLException e) {
            log.error("Error while calling stored procedure PROC_TASK_IMPORT_FROM_EXCEL", e);
            return "ERROR: " + e.getMessage();
        }
    }

}
