package com.asg.settings.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

@Repository
@RequiredArgsConstructor
public class RoleRightsCustomRepository {

    private final DataSource dataSource;

    public String callLoadDefaultRights(Long loginUserPoid, Long userRolePoid) {
        String status;
        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall("{ call PROC_GLOB_USRRLS_RIGHTS_DEF(?,?,?) }")) {

            cs.setLong(1, loginUserPoid);
            cs.setLong(2, userRolePoid);
            cs.registerOutParameter(3, Types.VARCHAR);

            cs.execute();
            status = cs.getString(3);

        } catch (SQLException e) {
            status = "ERROR : " + e.getMessage();
        }
        return status;
    }
}
