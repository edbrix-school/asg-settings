package com.asg.settings.repository;

import com.asg.settings.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    RoleEntity findByUserRolePoid(Long userRolePoid);

    List<RoleEntity> findByUserRolePoidIn(List<Long> userRolePoids);

    boolean existsByUserRoleName(String userRoleName);

    boolean existsByUserRoleId(String userRoleId);

    boolean existsByUserRolePoid(Long userRolePoid);

    boolean existsByUserRoleIdAndUserRolePoidNot(String userRoleId, Long userRolePoid);

    boolean existsByUserRoleNameAndUserRolePoidNot(String roleName, Long userRolePoid);


}


