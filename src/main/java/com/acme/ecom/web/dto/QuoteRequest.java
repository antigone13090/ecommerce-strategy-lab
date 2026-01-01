package com.acme.ecom.web.dto;

import com.acme.ecom.domain.pricing.DeliveryMode;
import com.acme.ecom.domain.pricing.LoyaltyTier;
import com.acme.ecom.domain.pricing.PaymentMethod;

import java.util.List;

public record QuoteRequest(
        List<Item> items,
        LoyaltyTier loyaltyTier,
        String promoCode,          // compat ancienne API
        List<String> promoCodes,   // nouvelle API
        String country,
        DeliveryMode deliveryMode,
        PaymentMethod paymentMethod
) {
    public record Item(
            String sku,
            String unitPrice,
            int qty,
            Integer weightGrams
    ) {}
}
