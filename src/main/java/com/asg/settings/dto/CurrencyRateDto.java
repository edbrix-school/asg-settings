package com.asg.settings.dto;

import com.asg.common.lib.entity.CurrencyRateEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CurrencyRateDto {

    private Long currencyPoid;
    private String currencyCode;
    private String currencyName;
    private String label;
    private Long value;
    private Integer seqno;
    private String currencyShortName;
    private String currencyName2;
    private String coinShortName;
    private String active;
    private Integer decimals;
    private BigDecimal buyRate;
    private BigDecimal sellRate;
    private LocalDate rateDate;
    private List<CurrencyRateEntity> rateHistory;
    
    // Audit fields
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
}