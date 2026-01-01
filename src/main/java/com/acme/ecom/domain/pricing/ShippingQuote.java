package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.money.Money;

public record ShippingQuote(
        Money cost,
        int etaDays,
        String ruleLabel
) {}
