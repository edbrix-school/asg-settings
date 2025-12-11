package com.asg.settings.repository;

import com.asg.settings.entity.CurrencyRateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurrencyRateRepository extends JpaRepository<CurrencyRateEntity, Long> {

    @Query("SELECT c FROM CurrencyRateEntity c " +
            "WHERE c.currencyCode = :currencyCode AND c.groupPoid = :groupPoid " +
            "ORDER BY c.rateDate DESC")
    List<CurrencyRateEntity> findAllByCurrencyCodeAndGroupPoid(
            @Param("currencyCode") String currencyCode,
            @Param("groupPoid") Long groupPoid
    );

    List<CurrencyRateEntity> findAllByCurrencyCode(String currencyCode);

    @Query("SELECT c FROM CurrencyRateEntity c " +
            "WHERE c.currencyCode = :currencyCode " +
            "AND c.rateDate <= :transactionDate " +
            "ORDER BY c.rateDate DESC")
    List<CurrencyRateEntity> findLatestRateByCurrencyCodeAndDate(
            @Param("currencyCode") String currencyCode,
            @Param("transactionDate") java.time.LocalDate transactionDate
    );
}