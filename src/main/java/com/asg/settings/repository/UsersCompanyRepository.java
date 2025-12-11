package com.asg.settings.repository;


import com.asg.settings.entity.UsersCompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsersCompanyRepository extends JpaRepository<UsersCompanyEntity, Long> {

    @Query("SELECT  c " +
            "FROM UsersCompanyEntity c WHERE c.id.userPoid = :userPoid")
    List<UsersCompanyEntity> findCompanyAccess(@Param("userPoid") Long userPoid);

    @Query("SELECT MAX(u.id.detRowId) FROM UsersCompanyEntity u WHERE u.id.userPoid = :userPoid")
    Long findMaxDetRowIdByUserPoid(Long userPoid);

    void removeAllById_UserPoid(Long userPoid);

    UsersCompanyEntity findById_UserPoidAndId_CompanyPoid(Long userPoid, Long aLong);

    void deleteById_UserPoidAndId_CompanyPoid(Long userPoid, Long aLong);

    List<UsersCompanyEntity> findAllById_UserPoid(Long userPoid);
}
