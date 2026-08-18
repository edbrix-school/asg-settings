package com.asg.settings.repository;

import com.asg.settings.entity.FavoriteMenuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;


@Repository
public class FavoriteMenuRepository {

    @Autowired
    private DataSource dataSource;

    public List<FavoriteMenuEntity> getUnassignedFavList(String userId, Long userPoid, String search) throws SQLException {
        String sql = "{ call PROC_GLOB_FAV_MENU_LIST_FULL(?, ?, ?, ?) }";
        List<FavoriteMenuEntity> results = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            // Postgres refcursors only live for the duration of the transaction that opened them
            conn.setAutoCommit(false);

            try (CallableStatement cs = conn.prepareCall(sql)) {
                cs.setString(1, userId);
                cs.setLong(2, userPoid);
                cs.setString(3, search != null ? search : "");
                cs.registerOutParameter(4, Types.OTHER); // REF_CURSOR

                cs.execute();

                try (ResultSet rs = (ResultSet) cs.getObject(4)) {
                    while (rs.next()) {
                        FavoriteMenuEntity menu = new FavoriteMenuEntity();
                        menu.setMenuId(rs.getString("MENU_ID"));
                        menu.setMenuName(rs.getString("MENU_NAME"));
                        menu.setMenuLevel(rs.getLong("MENU_LEVEL"));
                        menu.setMenuGroup(rs.getString("MENU_GROUP"));
                        menu.setTaskflowUrl(rs.getString("TASKFLOW_URL"));
                        menu.setDocType(rs.getString("DOC_TYPE"));
                        menu.setModuleId(rs.getString("MODULE_ID"));
                        results.add(menu);
                    }
                }
            }
            conn.commit();
        }

        return results;
    }


    public List<FavoriteMenuEntity> getFavoriteMenuList(@Param("userPoid") Long userPoid,
                                                        @Param("userId") String userId) throws SQLException {
        String sql = "{ call PROC_GLOB_FAV_MENU_LIST(?, ?, ?) }";
        List<FavoriteMenuEntity> results = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try (CallableStatement cs = conn.prepareCall(sql)) {
                cs.setString(1, userId);
                cs.setLong(2, userPoid);
                cs.registerOutParameter(3, Types.OTHER); // REF_CURSOR

                cs.execute();

                try (ResultSet rs = (ResultSet) cs.getObject(3)) {
                    while (rs.next()) {
                        FavoriteMenuEntity menu = new FavoriteMenuEntity();
                        menu.setMenuId(rs.getString("MENU_ID"));
                        menu.setMenuName(rs.getString("MENU_NAME"));
                        menu.setMenuLevel(rs.getLong("MENU_LEVEL"));
                        menu.setMenuGroup(rs.getString("MENU_GROUP"));
                        menu.setTaskflowUrl(rs.getString("TASKFLOW_URL"));
                        menu.setDocType(rs.getString("DOC_TYPE"));
                        menu.setModuleId(rs.getString("MODULE_ID"));

                        results.add(menu);
                    }
                }
            }
            conn.commit();
        }

        return results;
    }

    public String addFavoriteMenu(String userId, Long userPoid, String categoryValue, String selectedDocIds) throws SQLException {

        String sql = "{ call PROC_GLOB_FAV_MENU_LIST_ADD(?, ?, ?, ?, ?) }";
        String status;

        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, userId);
            cs.setLong(2, userPoid);
            cs.setString(3, categoryValue);
            cs.setString(4, selectedDocIds);
            cs.registerOutParameter(5, Types.VARCHAR);

            cs.execute();
            status = cs.getString(5);
        }
        return status;
    }

    public String removeFavoriteMenuList(String userId, Long userPoid, String categoryValue, String selectedDocIdList) throws SQLException {
        String sql = "{ call PROC_GLOB_FAV_MENU_LIST_REMOVE(?, ?, ?, ?, ?) }";
        String status;

        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, userId);
            cs.setLong(2, userPoid);
            cs.setString(3, categoryValue);
            cs.setString(4, selectedDocIdList);
            cs.registerOutParameter(5, Types.VARCHAR);

            cs.execute();

            status = cs.getString(5);
        }

        return status;
    }
}
