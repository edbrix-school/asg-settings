package com.asg.settings.repository;

import com.asg.settings.entity.UserRoleRightsDetEntity;
import com.asg.settings.entity.UserRoleRightsEntity;
import com.asg.settings.entity.key.UserRoleRightsKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRightsRepository extends JpaRepository<UserRoleRightsEntity, UserRoleRightsKey> {

   @Query(name = "UserRoleRightsDetEntity.fetchByUserRolePoid",
           nativeQuery = true)
   List<UserRoleRightsDetEntity> fetchAllDocsWithRightsForRole(@Param("userRolePoid") Long userRolePoid);

    boolean existsByIdUserRolePoidAndIdDetRowIdAndDocId(Long roleId, Long detRowId, String docId);

    Optional<UserRoleRightsEntity> findByIdUserRolePoidAndIdDetRowIdAndDocId(Long roleId, Long detRowId, String docId);

    List<UserRoleRightsEntity> findAllByIdUserRolePoid(Long userRolePoid);

}
