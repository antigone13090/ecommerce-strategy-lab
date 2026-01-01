package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.money.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class WeightZoneShippingAfterDiscountsStrategy implements ShippingStrategy {

    private final Money freeThreshold;

    public WeightZoneShippingAfterDiscountsStrategy(Money freeThreshold) {
        this.freeThreshold = freeThreshold;
    }

    @Override
    public Money compute(Money totalAfterDiscounts, Cart cart, CheckoutContext ctx) {

        // Free shipping uniquement en STANDARD
        if (ctx.deliveryMode() != DeliveryMode.EXPRESS
                && totalAfterDiscounts.asBigDecimal().compareTo(freeThreshold.asBigDecimal()) >= 0) {
            return Money.of("0.00");
        }

        int grams = cart.items().stream().mapToInt(i -> i.weightGrams()).sum();
        int kg = (int) Math.ceil(grams / 1000.0);

        // coût base selon zone + poids (exemple réaliste)
        BigDecimal base;
        switch (ctx.country()) {
            case "FR" -> base = tier(kg, "4.90", "6.90", "9.90");
            case "EU" -> base = tier(kg, "9.90", "14.90", "19.90");
            default   -> base = tier(kg, "19.90", "29.90", "39.90");
        }

        // surcharge express
        if (ctx.deliveryMode() == DeliveryMode.EXPRESS) {
            BigDecimal extra = switch (ctx.country()) {
                case "FR" -> new BigDecimal("8.00");
                case "EU" -> new BigDecimal("12.00");
                default   -> new BigDecimal("20.00");
            };
            base = base.add(extra);
        }

        base = base.setScale(2, RoundingMode.HALF_UP);
        return Money.of(base.toPlainString());
    }

    private static BigDecimal tier(int kg, String t1, String t2, String t3) {
        if (kg <= 1) return new BigDecimal(t1);
        if (kg <= 5) return new BigDecimal(t2);
        return new BigDecimal(t3);
    }

    @Override
    public String label() {
        return "WEIGHT_ZONE_AFTER_DISCOUNTS";
    }
}
