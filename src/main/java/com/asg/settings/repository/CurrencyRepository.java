package com.asg.settings.repository;

import com.asg.common.lib.entity.CurrencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<CurrencyEntity, Long>, JpaSpecificationExecutor<CurrencyEntity> {

    @Query(value="SELECT c.CURRENCY_CODE FROM GLOBAL_CURRENCY_MASTER c", nativeQuery = true)
    List<String> getAllCurrencyCodes();

    boolean existsByCurrencyCodeIgnoreCase(String currencyCode);

    boolean existsByCurrencyCodeIgnoreCaseAndCurrencyPoidNot(String currencyCode,Long currencyPoid);

    boolean existsByCurrencyNameIgnoreCase(String currencyName);

    boolean existsByCurrencyNameIgnoreCaseAndCurrencyPoidNot(String currencyName,Long currencyPoid);

    Optional<CurrencyEntity> findByCurrencyCodeIgnoreCase(String currencyCode);

    CurrencyEntity getByCurrencyPoid(Long currencyPoid);

}