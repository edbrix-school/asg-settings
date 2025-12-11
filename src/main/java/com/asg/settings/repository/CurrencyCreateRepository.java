package com.asg.settings.repository;

import com.asg.settings.dto.request.CurrencyCreateRequest;
import com.asg.settings.entity.CurrencyEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.OffsetDateTime;

@Repository
public class CurrencyCreateRepository {

    private static final Logger log = LoggerFactory.getLogger(CurrencyCreateRepository.class);

    private final CurrencyRepository currencyRepository;
    private final JdbcTemplate jdbcTemplate;
    
    @PersistenceContext
    private EntityManager entityManager;

    public CurrencyCreateRepository(CurrencyRepository currencyRepository, JdbcTemplate jdbcTemplate) {
        this.currencyRepository = currencyRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Handles CREATE or UPDATE using currencyCode for updates.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public CurrencyEntity createOrUpdateCurrency(CurrencyCreateRequest request, Long groupPoid, Long userPoid) {
        CurrencyEntity entity;

        if (request.getCurrencyPoid() == null) {
            // ---------- CREATE ----------
            if (request.getCurrencyName() == null || request.getCurrencyName().trim().isEmpty()) {
                throw new IllegalArgumentException("Currency Name is required");
            }

            if (currencyRepository.existsByCurrencyNameIgnoreCase(request.getCurrencyName())) {
                throw new IllegalArgumentException("Currency Name already exists");
            }

            // Use currencyCode from request payload
            if (StringUtils.isBlank(request.getCurrencyCode())) {
                throw new IllegalArgumentException("Currency Code is required");
            }
            String currencyCode = request.getCurrencyCode().trim();

            if (currencyRepository.existsByCurrencyCodeIgnoreCase(currencyCode)) {
                throw new IllegalArgumentException("Currency Code already exists");
            }

            entity = new CurrencyEntity();
            entity.setGroupPoid(groupPoid);
            entity.setCurrencyCode(currencyCode);
            entity.setCurrencyName(request.getCurrencyName());
            entity.setCurrencyName2(request.getCurrencyName2());
            entity.setCreatedBy(userPoid != null ? userPoid.toString() : "SYSTEM");
            entity.setCreatedDate(OffsetDateTime.now());
            entity.setActive("Y");
            entity.setDeleted(null);

            log.info("CURRENCY_CREATE success for code={} name={} by userPoid={}",
                    currencyCode, request.getCurrencyName(), userPoid);

        } else {
            // ---------- UPDATE ----------
            if (request.getCurrencyCode() == null || request.getCurrencyCode().trim().isEmpty()) {
                throw new IllegalArgumentException("Currency Code is required for update");
            }

            if (currencyRepository.existsByCurrencyCodeIgnoreCaseAndCurrencyPoidNot(request.getCurrencyCode(),request.getCurrencyPoid())){
                throw new IllegalArgumentException("Currency Code already exists.");
            }
            if(currencyRepository.existsByCurrencyNameIgnoreCaseAndCurrencyPoidNot(request.getCurrencyName(),request.getCurrencyPoid())) {
                throw new IllegalArgumentException("Currency Name already exists.");
            }

            entity = currencyRepository.findByCurrencyCodeIgnoreCase(request.getCurrencyCode())
                    .orElseThrow(() -> new IllegalArgumentException("Currency not found with code " + request.getCurrencyCode()));

            entity.setCurrencyName(request.getCurrencyName());
            entity.setCurrencyName2(request.getCurrencyName2());
            entity.setCurrencyShortName(request.getCurrencyShortName());
            entity.setCoinShortName(request.getCoinShortName());
            entity.setDecimals(request.getCurrencyDecimals());
            entity.setNumberFormatCurrency(request.getNumberFormatCurrency());
            entity.setSeqno(request.getSeqno());
            entity.setActive(request.getActive());
            entity.setLastModifiedBy(userPoid != null ? userPoid.toString() : "SYSTEM");
            entity.setLastModifiedDate(OffsetDateTime.now());

            log.info("CURRENCY_UPDATE success for code={} name={} by userPoid={}",
                    entity.getCurrencyCode(), entity.getCurrencyName(), userPoid);
        }

        log.info("Transaction active: {}", TransactionSynchronizationManager.isActualTransactionActive());
        
        if (request.getCurrencyPoid() == null) {
            // CREATE - Use direct JDBC insert
            try {
                String sql = "INSERT INTO GLOBAL_CURRENCY_MASTER " +
                            "(CURRENCY_POID, GROUP_POID, CURRENCY_CODE, CURRENCY_NAME, CURRENCY_NAME2, " +
                            "CURRENCY_SHORT_NAME, COIN_SHORT_NAME, CURRENCY_DECIMALS, SEQNO, ACTIVE, " +
                            "CREATED_BY, CREATED_DATE, DELETED, NUMBER_FORMAT_CURRENCY) " +
                            "VALUES (GLOBAL_CURRENCY_MASTER_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?, ?)";
                
                int rowsAffected = jdbcTemplate.update(sql, 
                    entity.getGroupPoid(),
                    entity.getCurrencyCode(), 
                    entity.getCurrencyName(),
                    request.getCurrencyName2(),
                    request.getCurrencyShortName(),
                    request.getCoinShortName(),
                    request.getCurrencyDecimals(),
                    request.getSeqno(),
                    entity.getActive(),
                    entity.getCreatedBy(),
                    entity.getDeleted(),
                    request.getNumberFormatCurrency());
                    
                log.info("Direct JDBC insert affected {} rows", rowsAffected);
                
                // Get the generated ID
                Long generatedId = jdbcTemplate.queryForObject(
                    "SELECT GLOBAL_CURRENCY_MASTER_SEQ.CURRVAL FROM DUAL", Long.class);
                entity.setCurrencyPoid(generatedId);
                
                log.info("Entity created with ID: {}", generatedId);
                return entity;
                
            } catch (Exception e) {
                log.error("Direct JDBC insert failed: {}", e.getMessage());
                throw e;
            }
        } else {
            // UPDATE - Use JPA save
            return currencyRepository.save(entity);
        }
    }
}
