package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.money.Money;

public final class FixedShippingAfterDiscountsStrategy implements ShippingStrategy {

    private final Money shippingFee;
    private final Money freeThreshold;

    public FixedShippingAfterDiscountsStrategy(Money shippingFee, Money freeThreshold) {
        this.shippingFee = shippingFee;
        this.freeThreshold = freeThreshold;
    }

    @Override
    public Money compute(Money totalAfterDiscounts, Cart cart, CheckoutContext ctx) {
        return (totalAfterDiscounts.compareTo(freeThreshold) < 0) ? shippingFee : Money.of("0.00");
    }

    @Override
    public String label() {
        return "FIXED_AFTER_DISCOUNTS";
    }
}
