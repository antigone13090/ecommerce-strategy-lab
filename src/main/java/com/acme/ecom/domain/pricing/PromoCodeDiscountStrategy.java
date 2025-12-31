package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.money.Money;

public final class PromoCodeDiscountStrategy implements DiscountStrategy {

    @Override
    public Money apply(Money currentTotal, Cart cart, CheckoutContext ctx) {
        if (ctx.promoCode() == null) return currentTotal;
        if ("NOEL10".equalsIgnoreCase(ctx.promoCode())) {
            // 5% appliqué après fidélité (si appelé en 2ème)
            return currentTotal.minus(currentTotal.percent("5"));
        }
        return currentTotal;
    }

    @Override
    public String label() {
        return "PROMO_CODE";
    }
}
