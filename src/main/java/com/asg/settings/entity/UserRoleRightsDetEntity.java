package com.asg.settings.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;


@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@SqlResultSetMapping(
        name = "UserRoleRightsDetMapping",
        entities = @EntityResult(
                entityClass = UserRoleRightsDetEntity.class,
                fields = {
                        @FieldResult(name = "userRolePoid", column = "USER_ROLE_POID"),
                        @FieldResult(name = "userRoleId", column = "USER_ROLE_ID"),
                        @FieldResult(name = "userRoleName", column = "USER_ROLE_NAME"),
                        @FieldResult(name = "userRoleName2", column = "USER_ROLE_NAME2"),
                        @FieldResult(name = "moduleShortName", column = "MODULE_SHORT_NAME"),
                        @FieldResult(name = "docId", column = "DOC_ID"),
                        @FieldResult(name = "docShortName", column = "DOC_SHORT_NAME"),
                        @FieldResult(name = "docType", column = "DOC_TYPE"),
                        @FieldResult(name = "rights", column = "RIGHTS")
                }
        )
)
// Performance Note: For ~1400 records, ensure these indexes exist:
// - GLOBAL_USER_ROLES(USER_ROLE_POID, ACTIVE, DELETED)
// - GLOBAL_DOC_MASTER(MODULE_ID, ACTIVE, DELETED)
// - GLOBAL_USER_ROLES_RIGHTS_DTL(USER_ROLE_POID, DOC_ID)
@NamedNativeQuery(
        name = "UserRoleRightsDetEntity.fetchByUserRolePoid",
        query = """
                SELECT DISTINCT
                  gur.USER_ROLE_POID,
                  gur.USER_ROLE_ID,
                  gur.USER_ROLE_NAME,
                  gur.USER_ROLE_NAME2,
                  m.MODULE_SHORT_NAME,
                  gdm.DOC_ID,
                  gdm.DOC_SHORT_NAME,
                  gdm.DOC_TYPE,
                  gurrd.RIGHTS
                FROM GLOBAL_USER_ROLES gur
                -- base: all docs in active modules
                CROSS JOIN GLOBAL_DOC_MASTER gdm
                JOIN GLOBAL_MODULE_MASTER m 
                  ON gdm.MODULE_ID = m.MODULE_ID
                -- left join rights for this role
                LEFT JOIN GLOBAL_USER_ROLES_RIGHTS_DTL gurrd
                  ON gurrd.USER_ROLE_POID = gur.USER_ROLE_POID
                 AND gurrd.DOC_ID = gdm.DOC_ID
                WHERE gur.ACTIVE = 'Y'
                  AND (gur.DELETED = 'N' OR gur.DELETED IS NULL)
                  AND gdm.ACTIVE = 'Y'
                  AND (gdm.DELETED = 'N' OR gdm.DELETED IS NULL)
                  AND m.ACTIVE = 'Y'
                  AND (m.DELETED = 'N' OR m.DELETED IS NULL)
                  AND gur.USER_ROLE_POID = :userRolePoid
                ORDER BY m.MODULE_SHORT_NAME, gdm.DOC_SHORT_NAME
                """,
        resultSetMapping = "UserRoleRightsDetMapping"
)
public class UserRoleRightsDetEntity {

    @Id
    @Column(name = "USER_ROLE_POID")
    private BigDecimal userRolePoid;
    
    @Id
    @Column(name = "DOC_ID")
    private String docId;

    @Column(name = "USER_ROLE_ID")
    private String userRoleId;

    @Column(name = "USER_ROLE_NAME")
    private String userRoleName;

    @Column(name = "USER_ROLE_NAME2")
    private String userRoleName2;

    @Column(name = "MODULE_SHORT_NAME")
    private String moduleShortName;

    @Column(name = "DOC_TYPE")
    private String docType;

    @Column(name = "DOC_SHORT_NAME")
    private String docShortName;

    @Column(name = "RIGHTS")
    private String rights;
}
