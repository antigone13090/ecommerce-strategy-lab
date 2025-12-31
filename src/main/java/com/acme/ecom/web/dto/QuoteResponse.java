package com.acme.ecom.web.dto;

import com.acme.ecom.domain.pricing.PricingResult;

import java.util.List;

public record QuoteResponse(
        String subtotal,
        String totalAfterDiscounts,
        String shippingCost,
        String finalTotal,
        List<String> appliedDiscounts,
        String shippingRule
) {
    public static QuoteResponse from(PricingResult r) {
        return new QuoteResponse(
                r.subtotal().toString(),
                r.totalAfterDiscounts().toString(),
                r.shippingCost().toString(),
                r.finalTotal().toString(),
                r.appliedDiscounts(),
                r.shippingRule()
        );
    }
}
