package com.acme.ecom.domain.pricing;

public record CheckoutContext(
        LoyaltyTier loyaltyTier,
        String promoCode,
        String country,
        DeliveryMode deliveryMode
) {
    // compat : ancien appel => STANDARD
    public CheckoutContext(LoyaltyTier loyaltyTier, String promoCode, String country) {
        this(loyaltyTier, promoCode, country, DeliveryMode.STANDARD);
    }
}
