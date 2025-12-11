package com.asg.settings.repository;

import com.asg.settings.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> ,JpaSpecificationExecutor<Country> {

    Country findByCountryPoid(Long countryPoid);

    @Query("SELECT c.countryCode FROM Country c WHERE c.countryPoid = :countryPoid")
    String findCountryCodeByCountryPoid(@Param("countryPoid") Long countryPoid);

    boolean existsByCountryPoid(Long countryPoid);

    boolean existsByCountryCode(String countryCode);

    boolean existsByCountryCodeIgnoreCaseAndCountryPoidNot(String countryCode, Long countryPoid);

    boolean existsByCountryName(String countryName);

    boolean existsByCountryNameIgnoreCaseAndCountryPoidNot(String countryName, Long countryPoid);

}
