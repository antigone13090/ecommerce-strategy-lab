package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.money.Money;

import java.math.BigDecimal;

public final class LoyaltyDiscountStrategy implements DiscountStrategy {

    @Override
    public DiscountApplication apply(Money currentTotal, Cart cart, CheckoutContext ctx) {
        LoyaltyTier tier = (ctx == null || ctx.loyaltyTier() == null) ? LoyaltyTier.NONE : ctx.loyaltyTier();

        BigDecimal rate = switch (tier) {
            case GOLD -> new BigDecimal("0.05");   // -5%
            case SILVER -> new BigDecimal("0.03"); // -3%
            default -> BigDecimal.ZERO;
        };

        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            return new DiscountApplication(currentTotal, false, label());
        }

        BigDecimal out = currentTotal.asBigDecimal().multiply(BigDecimal.ONE.subtract(rate));
        return new DiscountApplication(Money.of(out.toPlainString()), true, label());
    }

    @Override
    public String label() {
        return "LOYALTY";
    }
}
