package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.money.Money;

import java.math.BigDecimal;
import java.util.List;

public record PricingResult(
        Money subtotal,
        Money afterDiscounts,
        Money shippingCost,
        int etaDays,
        String shippingRule,
        Money taxAmount,
        BigDecimal vatRate,
        Money paymentFee,
        PaymentMethod paymentMethod,
        Money finalTotal,
        List<String> applied
) {
    // compat : anciens tests / anciens noms
    public Money totalAfterDiscounts() {
        return afterDiscounts;
    }
}
