package com.asg.settings.repository;

import com.asg.settings.entity.UserRolesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRolesEntity, Long> {

    List<UserRolesEntity> findAllById_UserPoid(Long userPoid);

    @Query("SELECT MAX(u.id.detRowId) FROM UserRolesEntity u WHERE u.id.userPoid = :userPoid")
    Long findMaxDetRowIdByUserPoid(Long userPoid);

    void removeAllById_UserPoid(Long userPoid);

    void deleteByUserRolePoidAndId_UserPoid(Long aLong, Long userPoid);

    UserRolesEntity getUserRolesEntitiesById_UserPoidAndUserRolePoid(Long userPoid, Long aLong);
    
    @Query("SELECT ur FROM UserRolesEntity ur WHERE ur.userRolePoid = :userRolePoid")
    List<UserRolesEntity> findAllByUserRolePoid(@Param("userRolePoid") Long userRolePoid);
    
}
