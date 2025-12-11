package com.asg.settings.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.Map;


@Repository
public class ParameterRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ParameterRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    protected SimpleJdbcCall buildJdbcCall() {
        return new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("PROC_SETTINGS_UPDATE_PARAMETER")
                .declareParameters(
                        new SqlParameter("P_LOGIN_USER_POID", Types.BIGINT),
                        new SqlParameter("P_PARAMETER_POID", Types.BIGINT),
                        new SqlParameter("P_PARAMETER_KEYID_NEW", Types.VARCHAR),
                        new SqlParameter("P_PARAMETER_VALUE_NEW", Types.VARCHAR),
                        new SqlOutParameter("P_STATUS", Types.VARCHAR)
                );
    }

    public String callUpdateProcedure(Long loginUserPoid, Long parameterPoid,
                                      String parameterKeyId, String parameterValue) {

        SimpleJdbcCall jdbcCall = buildJdbcCall();

        Map<String, Object> result = jdbcCall.execute(
                new MapSqlParameterSource()
                        .addValue("P_LOGIN_USER_POID", loginUserPoid)
                        .addValue("P_PARAMETER_POID", parameterPoid)
                        .addValue("P_PARAMETER_KEYID_NEW", parameterKeyId)
                        .addValue("P_PARAMETER_VALUE_NEW", parameterValue)
        );

        return (String) result.get("P_STATUS");
    }
}
