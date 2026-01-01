package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.money.Money;

import java.math.BigDecimal;

public record TaxQuote(
        Money taxAmount,
        BigDecimal vatRate,
        Money taxableBase
) {}
