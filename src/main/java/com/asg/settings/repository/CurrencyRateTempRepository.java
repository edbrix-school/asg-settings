package com.asg.settings.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public class CurrencyRateTempRepository {

    private final JdbcTemplate jdbcTemplate;

    public CurrencyRateTempRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void clearTempTable() {
        jdbcTemplate.update("DELETE FROM CURRENCY_RATE_UPLOAD_TEMP");
    }

    public void insertTemp(String code, BigDecimal buy, BigDecimal sell) {
        jdbcTemplate.update(
                "INSERT INTO CURRENCY_RATE_UPLOAD_TEMP (CURRENCY_CODE, BUY_RATE, SELL_RATE) VALUES (?, ?, ?)",
                code, buy, sell
        );
    }
}