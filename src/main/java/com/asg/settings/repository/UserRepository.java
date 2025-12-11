package com.asg.settings.repository;

import com.asg.settings.entity.User;
import com.asg.settings.entity.UserEntity;
import com.asg.settings.repository.projection.UserRoleProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    String USER_ROLE_BY_ROLE_POID = """
            SELECT um.user_poid,
                   um.user_id,
                   um.user_name,
                   um.user_email
            FROM GLOBAL_USERS um
                JOIN global_users_auth_roles_dtl ur ON um.user_poid = ur.user_poid
                JOIN global_user_roles gur ON ur.user_role_poid = gur.user_role_poid
            WHERE um.active = 'Y'
              AND gur.active = 'Y'
              AND gur.user_role_poid = :userRolePoid
            ORDER BY um.user_id
            """;

    @Query(value = USER_ROLE_BY_ROLE_POID, nativeQuery = true)
    List<UserEntity> fetchUserRoleByRolePoid(Long userRolePoid);
    
    @Query(value = USER_ROLE_BY_ROLE_POID, nativeQuery = true)
    List<UserRoleProjection> fetchUserRoleProjectionByRolePoid(Long userRolePoid);


    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email) AND u.active = 'Y'")
    Optional<User> findByActiveEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE LOWER(u.userId) = LOWER(:userId)AND u.active = 'Y' AND u.deleted is NULL")
    Optional<User> findByActiveUserId(@Param("userId") String userId);

    @Query("SELECT u FROM User u WHERE LOWER(u.userName) = LOWER(:userName)AND u.active = 'Y' AND u.deleted is NULL")
    Optional<User> findByActiveUserName(@Param("userName") String userId);

    @Query("SELECT u.userId FROM User u WHERE u.userPoid = :userPoid")
    String findUserIdByUserPoid(Long userPoid);

    @Query("SELECT u.userName FROM User u WHERE u.userPoid = :userPoid")
    String findUserNameByUserPoid(Long userPoid);

    Optional<User> findByUserPoid(Long userPoid);

    Boolean existsByUserIdIgnoreCase(String userId);

    Boolean existsByUserIdIgnoreCaseAndUserPoidNot(String userId, Long userPoid);

    Boolean existsByUserNameIgnoreCase(String userName);

    Boolean existsByUserNameIgnoreCaseAndUserPoidNot(String userName, Long userPoid);

    Boolean existsByEmailIgnoreCaseAndAuthenticationMethod(String userEmail, String authenticationMethod);

    Boolean existsByEmailIgnoreCaseAndAuthenticationMethodAndUserPoidNot(String userEmail, String authenticationMethod, Long userPoid);

    boolean existsUserByUserPoid(Long userPoid);

    @Query("SELECT u.userPoid FROM User u WHERE LOWER(u.userId) = LOWER(:userId)")
    Long findUserPoidByUserId(@Param("userId") String userId);

}

