package com.asg.settings.repository;

import com.asg.settings.entity.FavoriteMenuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
        // PROC_GLOB_FAV_MENU_LIST's refcursor OUT param is 3rd of 3, not first — pgjdbc's
        // CallableStatement.registerOutParameter only binds a REF_CURSOR correctly in the first
        // position, so it was silently dropped. A plain CALL query sidesteps that entirely:
        // Postgres returns the cursor's name as an ordinary one-row ResultSet, then a separate
        // FETCH ALL FROM "<name>" reads the actual rows.
        List<FavoriteMenuEntity> results = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String cursorName;
                try (PreparedStatement ps = conn.prepareStatement("CALL PROC_GLOB_FAV_MENU_LIST(?, ?, NULL)")) {
                    ps.setString(1, userId);
                    ps.setLong(2, userPoid);
                    try (ResultSet crs = ps.executeQuery()) {
                        cursorName = crs.next() ? crs.getString(1) : null;
                    }
                }

                if (cursorName != null) {
                    try (Statement fetchStmt = conn.createStatement();
                         ResultSet rs = fetchStmt.executeQuery("FETCH ALL FROM \"" + cursorName + "\"")) {
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
            } finally {
                conn.setAutoCommit(true);
            }
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
