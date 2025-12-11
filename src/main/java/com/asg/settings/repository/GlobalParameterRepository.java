package com.asg.settings.repository;

import com.asg.settings.entity.GlobalParameterEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface GlobalParameterRepository extends JpaRepository<GlobalParameterEntity, Long> {

    @Query("""
              SELECT g
              FROM GlobalParameterEntity g
              WHERE g.parameterType = :parameterType
                AND (
                  :filterValue IS NULL OR :filterValue = '' OR (
                       UPPER(g.parameterName)       LIKE CONCAT('%', UPPER(:filterValue), '%')
                    OR UPPER(g.parameterDetails)    LIKE CONCAT('%', UPPER(:filterValue), '%')
                    OR UPPER(g.parameterKeyIdType)  LIKE CONCAT('%', UPPER(:filterValue), '%')
                    OR UPPER(g.parameterValue)      LIKE CONCAT('%', UPPER(:filterValue), '%')
                  )
                )
            """)
    Page<GlobalParameterEntity> findAllByParameterType(
            @Param("parameterType") String parameterType,
            @Param("filterValue") String filterValue,
            Pageable pageable
    );


    @Query(value = """
                SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END
                FROM GLOBAL_USERS_AUTH_ROLES_DTL
                WHERE USER_ROLE_POID IN (
                    SELECT URR.USER_ROLE_POID
                    FROM GLOBAL_USER_ROLES_RIGHTS_DTL URR
                    WHERE URR.DOC_ID = '000-269' AND URR.RIGHTS LIKE '%1%'
                ) AND USER_POID = :userPoid
            """, nativeQuery = true)
    int hasSystemPrivilege(@Param("userPoid") Long userPoid);

    @Query("""
              SELECT g.parameterValue
              FROM GlobalParameterEntity g
              WHERE g.parameterType = :parameterType
                AND g.parameterName = :parameterName
            """)
    Integer findParameterValueByParameterName(
            @Param("parameterType") String parameterType,
            @Param("parameterName") String parameterName
    );

    /**
     * Find parameter value by parameter name
     */
    @Query("SELECT g.parameterValue FROM GlobalParameterEntity g WHERE g.parameterName = :parameterName")
    Optional<String> findParameterValueByName(@Param("parameterName") String parameterName);
    @Query("""
      SELECT g.parameterValue
      FROM GlobalParameterEntity g
      WHERE g.parameterType = :parameterType
        AND g.parameterName = :parameterName
  """)
    BigDecimal findParameterValueByParameterNameAsDecimal(
            @Param("parameterType") String parameterType,
            @Param("parameterName") String parameterName
    );


}
