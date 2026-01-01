package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.money.Money;

public record PaymentFeeQuote(
        Money fee,
        String ruleLabel
) {}
