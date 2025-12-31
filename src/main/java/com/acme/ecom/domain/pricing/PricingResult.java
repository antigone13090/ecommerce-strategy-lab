package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.money.Money;

import java.util.List;

public record PricingResult(
        Money subtotal,
        Money totalAfterDiscounts,
        Money shippingCost,
        Money finalTotal,
        List<String> appliedDiscounts,
        String shippingRule
) {}
