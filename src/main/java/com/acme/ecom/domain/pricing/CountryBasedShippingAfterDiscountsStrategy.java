package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.money.Money;

import java.util.Map;

public final class CountryBasedShippingAfterDiscountsStrategy implements ShippingStrategy {

    private final Money freeThreshold;
    private final Map<String, Money> feesByCountry; // "FR" -> 4.90, "EU" -> 7.90, "OTHER" -> 12.90

    public CountryBasedShippingAfterDiscountsStrategy(Money freeThreshold, Map<String, Money> feesByCountry) {
        this.freeThreshold = freeThreshold;
        this.feesByCountry = feesByCountry;
    }

    @Override
    public Money compute(Money totalAfterDiscounts, Cart cart, CheckoutContext ctx) {
        if (totalAfterDiscounts.compareTo(freeThreshold) >= 0) return Money.of("0.00");

        String key = ctx.country();
        Money fee = feesByCountry.get(key);
        if (fee != null) return fee;

        // fallback
        return feesByCountry.getOrDefault("OTHER", Money.of("12.90"));
    }

    @Override
    public String label() {
        return "COUNTRY_AFTER_DISCOUNTS";
    }
}
