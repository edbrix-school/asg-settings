package com.asg.settings.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CurrencyUpdateRequest(
     Long groupPOID,
     String currencyCode,
     LocalDate rateChangeDate,
     BigDecimal buyRate,
     BigDecimal sellRate

){}