package com.asg.settings.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrencyCreateRequest {

    private Long currencyPoid;   // For update

    @NotBlank(message = "Currency Code is required")
    @Size(max = 10, message = "Currency Code must not exceed 10 characters")
    @Pattern(regexp = "^[A-Z0-9]+$",
            message = "Currency Code must be uppercase alphanumeric")
    @NotBlank(message = "Currency Code is required")
    private String currencyCode;

    @NotBlank(message = "Currency Name is required")
    @Size(max = 100, message = "Currency Name must not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s]+$",
            message = "Currency Name must contain only letters, numbers, and spaces")
    @NotBlank(message = "Currency Name is required")
    private String currencyName;

    @Size(max = 100, message = "Currency Name 2 must not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s]*$",
            message = "currencyName2 must contain only letters, numbers, and spaces")
    private String currencyName2;

    @Size(max = 10, message = "Coin Short Name must not exceed 10 characters")
    @Pattern(regexp = "^[a-zA-Z0-9]*$",
            message = "Coin Short Name must be alphanumeric")
    private String currencyShortName;
    private String coinShortName;

    @Max(value = 10, message = "Currency Decimals cannot exceed 10")
    private Integer currencyDecimals;

    @Size(max = 20, message = "Number Format Currency must not exceed 20 characters")
    private String numberFormatCurrency;
    private Integer seqno;

    @Pattern(regexp = "^(Y|N)$",
            message = "Active must be either Y or N")
    private String active;
}

