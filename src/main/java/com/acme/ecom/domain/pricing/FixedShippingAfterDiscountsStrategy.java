package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.money.Money;

public final class FixedShippingAfterDiscountsStrategy implements ShippingStrategy {

    private final Money standardCost;
    private final Money freeThreshold;

    public FixedShippingAfterDiscountsStrategy() {
        this(Money.of("7.90"), Money.of("50.00"));
    }

    public FixedShippingAfterDiscountsStrategy(Money standardCost) {
        this(standardCost, Money.of("50.00"));
    }

    public FixedShippingAfterDiscountsStrategy(Money standardCost, Money freeThreshold) {
        this.standardCost = (standardCost == null) ? Money.of("7.90") : standardCost;
        this.freeThreshold = (freeThreshold == null) ? Money.of("50.00") : freeThreshold;
    }

    @Override
    public Money compute(Money totalAfterDiscounts, Cart cart, CheckoutContext ctx) {

        // Free shipping dès que >= seuil, pour tout mode NON express
        if (ctx != null
                && ctx.deliveryMode() != DeliveryMode.EXPRESS
                && totalAfterDiscounts.asBigDecimal().compareTo(freeThreshold.asBigDecimal()) >= 0) {
            return Money.of("0.00");
        }

        // Express : surcharge simple (si jamais)
        if (ctx != null && ctx.deliveryMode() == DeliveryMode.EXPRESS) {
            return Money.of("15.90");
        }

        return standardCost;
    }

    @Override
    public String label() {
        return "FIXED_AFTER_DISCOUNTS";
    }
}
