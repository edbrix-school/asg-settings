package com.asg.settings.repository;

import com.asg.settings.dto.request.DocAcquireLockRequestDto;
import com.asg.settings.dto.request.DocReleaseLockRequestDto;
import com.asg.settings.dto.request.DocUpdateLockRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

@Repository
@Slf4j
@RequiredArgsConstructor
public class DocumentLockRepository {

    @Autowired
    private final DataSource dataSource;

    public String releaseLock(DocReleaseLockRequestDto request) {
        log.info("Releasing lock -> loginGroupPoid: {}, loginCompanyPoid: {}, loginUserPoid: {}, docId: {}, docPoidValue: {}, userId: {}",
                request.getLoginGroupPoid(), request.getLoginCompanyPoid(), request.getLoginUserPoid(), request.getDocId(),
                request.getDocPoidValue(), request.getUserId());
        String status = null;
        String sql = "{ call PROC_GLOB_DOC_RELEASE_LOCK(?, ?, ?, ?, ?, ?, ?)}";

        try (Connection connection = DataSourceUtils.getConnection(dataSource);
             CallableStatement cs = connection.prepareCall(sql)) {

            cs.setLong(1, request.getLoginGroupPoid());
            cs.setLong(2, request.getLoginCompanyPoid());
            cs.setLong(3, Long.parseLong(request.getLoginUserPoid()));
            cs.setString(4, request.getDocId());
            cs.setLong(5, request.getDocPoidValue());
            cs.setString(6, request.getUserId());
            cs.registerOutParameter(7, java.sql.Types.VARCHAR);

            cs.execute();
            status= cs.getString(7);
            log.info("status from release lock : {}", status);
            return status;
        } catch (SQLException e) {
            log.error("Error while releasing lock: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to release lock");
        }
    }


    public String acquireLock(DocAcquireLockRequestDto request) {

        log.info("Acquiring lock -> userId: {}, sessionDetails: {}, docId: {}, docName: {}, docKeyPoid: {}",
                request.getUserId(), request.getSessionDetails(), request.getDocId(), request.getDocName(),
                request.getDocKeyPoid());

        String status = null;
        String sql = "{ ? = call RTN_GLOBAL_USER_RECORD_LOCK(?, ?, ?, ?, ?, ?) }";

        try (Connection connection = DataSourceUtils.getConnection(dataSource);
             CallableStatement cs = connection.prepareCall(sql)) {

            cs.registerOutParameter(1, java.sql.Types.VARCHAR); // RETURN value

            cs.setString(2, request.getUserId());
            cs.setString(3, request.getSessionDetails());
            cs.setString(4, request.getDocId());
            cs.setString(5, request.getDocName());
            cs.setLong(6, request.getDocKeyPoid());
            cs.setString(7, "E");

            cs.execute();

             status = cs.getString(1); // RETURN value from function
            log.info("Lock function returned status: {}", status);
            return status;

        } catch (SQLException e) {
            log.error("Error while acquiring lock: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to acquire lock");
        }

    }


    public String updateLock(DocUpdateLockRequestDto request) {

        String sql = "{ call PROC_GLOB_USER_LOGIN_UPDATE(?, ?, ?, ?, ?, ?) }";
        String status = null;
        String pStatus = (request.getStatus() == null || request.getStatus().isBlank())
                ? "UPDATE" : request.getStatus().trim().toUpperCase();
        log.info("Session update -> status: {}, userId: {}, sessionId: {}, ip: {}, browser: {}, lastPage: {}",
                pStatus, request.getUserId(), request.getSessionId(), request.getSessionIp(), request.getSessionBrowser(), request.getLastPageVisited());
        try (Connection connection = DataSourceUtils.getConnection(dataSource);
             CallableStatement cs = connection.prepareCall(sql)) {

            cs.setString(1, request.getUserId());
            cs.setString(2, request.getSessionId());
            cs.setString(3, request.getSessionIp());
            cs.setString(4, request.getSessionBrowser());
            cs.setString(5, request.getLastPageVisited());
            cs.setString(6, pStatus); // LOGIN / LOGOUT

            cs.execute();

            status = "OK";
            return status;
        } catch (SQLException e) {
            log.error("Error while updating session/lock: {}", e.getMessage(), e);
        }
        return status;
    }

}
