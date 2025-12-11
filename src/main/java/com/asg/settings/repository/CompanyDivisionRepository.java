package com.asg.settings.repository;

import com.asg.settings.entity.CompanyDivisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CompanyDivisionRepository extends JpaRepository<CompanyDivisionEntity, Long> {

    @Query("SELECT MAX(c.id.detRowId) FROM CompanyDivisionEntity c WHERE c.id.companyPoid = :companyPoid")
    Long findMaxDetRowIdByCompanyPoid(Long companyPoid);

    List<CompanyDivisionEntity> findById_CompanyPoid(Long companyPoid);

    // Find by companyPoid and divPoid
    CompanyDivisionEntity findById_CompanyPoidAndDivPoid(Long companyPoid, Long divPoid);

    //  Delete by companyPoid and divPoid
    void deleteById_CompanyPoidAndDivPoid(Long companyPoid, Long divPoid);

    //  Delete all divisions by companyPoid
    void deleteById_CompanyPoid(Long companyPoid);

}


