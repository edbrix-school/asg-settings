package com.asg.settings.dto;

import java.math.BigDecimal;
import java.sql.Date;

public record CurrencyUpdateRequest(
     Long groupPOID,
     String currencyCode,
     Date rateChangeDate,
     BigDecimal buyRate,
     BigDecimal sellRate

){}