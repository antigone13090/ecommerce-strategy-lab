package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.money.Money;

public final class LoyaltyDiscountStrategy implements DiscountStrategy {

    @Override
    public Money apply(Money currentTotal, Cart cart, CheckoutContext ctx) {
        if (ctx.loyaltyTier() == LoyaltyTier.GOLD) {
            return currentTotal.minus(currentTotal.percent("10"));
        }
        if (ctx.loyaltyTier() == LoyaltyTier.SILVER) {
            return currentTotal.minus(currentTotal.percent("5"));
        }
        return currentTotal;
    }

    @Override
    public String label() {
        return "LOYALTY";
    }
}
