package com.asg.settings.repository;

import com.asg.common.lib.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long>, JpaSpecificationExecutor<Company> {

    Company findByCompanyPoid(Long companyPoid);

    @Query("SELECT c.countryId FROM Company c WHERE c.companyPoid = :companyPoid")
    String findCountryIdByCompanyPoid(@Param("companyPoid") Long companyPoid);

    @Query("SELECT c FROM Company c WHERE c.companyPoid = :companyPoid AND (c.deleted IS NULL OR c.deleted = '' OR c.deleted = 'N')")
    Company findActiveByCompanyPoid(@Param("companyPoid") Long companyPoid);

    boolean existsByCompanyCodeIgnoreCase(String companyCode);

    boolean existsByCompanyPoid(Long companyPoid);

    boolean existsByCompanyCodeIgnoreCaseAndCompanyPoidNot(String companyCode, Long companyPoid);

    boolean existsByCompanyNameIgnoreCase(String companyName);

    boolean existsByCompanyNameIgnoreCaseAndCompanyPoidNot(String companyName, Long companyPoid);

    boolean existsByTinNumberIgnoreCase(String tinNumber);

}
