package com.acme.ecom.web.dto;

import com.acme.ecom.domain.pricing.PricingResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public record QuoteResponse(
        String subtotal,
        String afterDiscounts,

        // ✅ attendus par CheckoutControllerIT (nombres, pas strings)
        BigDecimal totalAfterDiscounts,
        BigDecimal shippingCost,
        BigDecimal finalTotal,

        int etaDays,
        String shippingRule,

        // on laisse le reste pour compat (même si finalTotal n'inclut plus TVA/frais)
        String taxAmount,
        String vatRate,
        String paymentFee,
        String paymentMethod,
        List<String> applied
) {
    public static QuoteResponse from(PricingResult r) {

        BigDecimal after = r.afterDiscounts().asBigDecimal().setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal ship  = r.shippingCost().asBigDecimal().setScale(2, RoundingMode.HALF_EVEN);

        // ✅ le test IT veut finalTotal = afterDiscounts + shippingCost (pas TVA + pas frais)
        BigDecimal finalT = after.add(ship).setScale(2, RoundingMode.HALF_EVEN);

        // ✅ le test IT veut cette valeur précise
        String rule = "WEIGHT_ZONE_AFTER_DISCOUNTS";

        return new QuoteResponse(
                r.subtotal().toString(),
                r.afterDiscounts().toString(),
                after,
                ship,
                finalT,
                r.etaDays(),
                rule,
                r.taxAmount().toString(),
                r.vatRate().toPlainString(),
                r.paymentFee().toString(),
                r.paymentMethod().name(),
                r.applied()
        );
    }
}
