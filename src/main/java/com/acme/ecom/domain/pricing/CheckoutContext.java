package com.acme.ecom.domain.pricing;

import java.util.List;
import java.util.Set;

public record CheckoutContext(
        String country,
        DeliveryMode deliveryMode,
        LoyaltyTier loyaltyTier,
        List<String> promoCodes,
        PaymentMethod paymentMethod
) {
    // EU-27 (ISO2)
    private static final Set<String> EU = Set.of(
            "AT","BE","BG","HR","CY","CZ","DK","EE","FI","FR","DE","GR","HU","IE","IT",
            "LV","LT","LU","MT","NL","PL","PT","RO","SK","SI","ES","SE"
    );

    public CheckoutContext {
        String raw = (country == null || country.isBlank()) ? "OTHER" : country.trim().toUpperCase();

        // normalisation zone
        if ("FR".equals(raw)) {
            country = "FR";
        } else if ("EU".equals(raw) || EU.contains(raw)) {
            country = "EU";
        } else {
            country = "OTHER";
        }

        deliveryMode = (deliveryMode == null) ? DeliveryMode.STANDARD : deliveryMode;
        loyaltyTier = (loyaltyTier == null) ? LoyaltyTier.NONE : loyaltyTier;

        promoCodes = (promoCodes == null)
                ? List.of()
                : promoCodes.stream().filter(s -> s != null && !s.isBlank()).toList();

        paymentMethod = (paymentMethod == null) ? PaymentMethod.CARD : paymentMethod;
    }

    // compat : ancien code (pays + mode + tier + code)
    public CheckoutContext(String country, DeliveryMode deliveryMode, LoyaltyTier loyaltyTier, String promoCode) {
        this(country,
                deliveryMode,
                loyaltyTier,
                (promoCode == null || promoCode.isBlank()) ? List.of() : List.of(promoCode),
                PaymentMethod.CARD);
    }

    // compat : tes tests (tier, promoCode, country)
    public CheckoutContext(LoyaltyTier loyaltyTier, String promoCode, String country) {
        this(country,
                DeliveryMode.STANDARD,
                loyaltyTier,
                (promoCode == null || promoCode.isBlank()) ? List.of() : List.of(promoCode),
                PaymentMethod.CARD);
    }
}
