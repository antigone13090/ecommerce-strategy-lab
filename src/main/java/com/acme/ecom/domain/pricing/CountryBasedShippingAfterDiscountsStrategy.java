package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.money.Money;

import java.util.Map;

public final class CountryBasedShippingAfterDiscountsStrategy implements ShippingStrategy {

    private final Money freeThreshold;
    private final Map<String, Money> standardCosts;
    private final Map<String, Money> expressCosts;

    // Defaults compatibles avec tes tests (EU = 7.90, seuil = 50)
    public CountryBasedShippingAfterDiscountsStrategy() {
        this(Money.of("50.00"), defaultStandardCosts());
    }

    public CountryBasedShippingAfterDiscountsStrategy(Money freeThreshold) {
        this(freeThreshold, defaultStandardCosts());
    }

    // ✅ constructeur attendu par CountryShippingPricingTest
    public CountryBasedShippingAfterDiscountsStrategy(Money freeThreshold, Map<String, Money> standardCosts) {
        this.freeThreshold = (freeThreshold == null) ? Money.of("50.00") : freeThreshold;
        this.standardCosts = (standardCosts == null || standardCosts.isEmpty())
                ? defaultStandardCosts()
                : standardCosts;

        this.expressCosts = defaultExpressCosts();
    }

    @Override
    public Money compute(Money totalAfterDiscounts, Cart cart, CheckoutContext ctx) {

        // Free shipping dès que >= seuil, pour tout mode NON express
        if (ctx != null
                && ctx.deliveryMode() != DeliveryMode.EXPRESS
                && totalAfterDiscounts.asBigDecimal().compareTo(freeThreshold.asBigDecimal()) >= 0) {
            return Money.of("0.00");
        }

        String c = (ctx == null || ctx.country() == null) ? "OTHER" : ctx.country().trim().toUpperCase();

        if (ctx != null && ctx.deliveryMode() == DeliveryMode.EXPRESS) {
            return expressCosts.getOrDefault(c, expressCosts.getOrDefault("OTHER", Money.of("29.90")));
        }

        return standardCosts.getOrDefault(c, standardCosts.getOrDefault("OTHER", Money.of("14.90")));
    }

    private static Map<String, Money> defaultStandardCosts() {
        return Map.of(
                "FR", Money.of("4.90"),
                "EU", Money.of("7.90"),
                "OTHER", Money.of("14.90")
        );
    }

    private static Map<String, Money> defaultExpressCosts() {
        return Map.of(
                "FR", Money.of("12.90"),
                "EU", Money.of("19.90"),
                "OTHER", Money.of("29.90")
        );
    }

    @Override
    public String label() {
        return "COUNTRY_BASED_AFTER_DISCOUNTS";
    }
}
