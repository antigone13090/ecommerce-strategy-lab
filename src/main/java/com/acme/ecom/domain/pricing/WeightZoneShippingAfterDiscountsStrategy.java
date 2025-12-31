package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.money.Money;

public final class WeightZoneShippingAfterDiscountsStrategy implements ShippingStrategy {

    private final Money freeThreshold;

    public WeightZoneShippingAfterDiscountsStrategy(Money freeThreshold) {
        this.freeThreshold = freeThreshold;
    }

    @Override
    public Money compute(Money totalAfterDiscounts, Cart cart, CheckoutContext ctx) {

        // Gratuit seulement en STANDARD
        if (ctx.deliveryMode() == DeliveryMode.STANDARD
                && totalAfterDiscounts.compareTo(freeThreshold) >= 0) {
            return Money.of("0.00");
        }

        int w = cart.totalWeightGrams();

        // Barèmes exemple (réalistes)
        Money base = switch (ctx.country()) {
            case "FR" -> feeFR(w);
            case "EU" -> feeEU(w);
            default   -> feeOTHER(w);
        };

        if (ctx.deliveryMode() == DeliveryMode.EXPRESS) {
            return base.times("1.70");
        }
        return base;
    }

    private Money feeFR(int w) {
        if (w <= 500)  return Money.of("4.90");
        if (w <= 2000) return Money.of("6.90");
        return Money.of("9.90");
    }

    private Money feeEU(int w) {
        if (w <= 500)  return Money.of("7.90");
        if (w <= 2000) return Money.of("10.90");
        return Money.of("14.90");
    }

    private Money feeOTHER(int w) {
        if (w <= 500)  return Money.of("12.90");
        if (w <= 2000) return Money.of("18.90");
        return Money.of("24.90");
    }

    @Override
    public String label() {
        return "WEIGHT_ZONE_AFTER_DISCOUNTS";
    }
}
