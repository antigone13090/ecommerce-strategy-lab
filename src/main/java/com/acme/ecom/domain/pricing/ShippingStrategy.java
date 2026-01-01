package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.money.Money;

public interface ShippingStrategy {

    record ShippingQuote(Money cost, String carrier, int etaDays, String label) {}

    // ancien contrat (encore présent dans tes anciennes stratégies)
    Money compute(Money totalAfterDiscounts, Cart cart, CheckoutContext ctx);

    // nouveau contrat (par défaut => compatible sans toucher tes anciennes classes)
    default ShippingQuote quote(Money totalAfterDiscounts, Cart cart, CheckoutContext ctx) {
        Money cost = compute(totalAfterDiscounts, cart, ctx);

        String carrier = switch (ctx.country()) {
            case "FR" -> (ctx.deliveryMode() == DeliveryMode.EXPRESS) ? "Chronopost" : "Colissimo";
            case "EU" -> (ctx.deliveryMode() == DeliveryMode.EXPRESS) ? "UPS" : "DPD";
            default   -> (ctx.deliveryMode() == DeliveryMode.EXPRESS) ? "DHL Express" : "DHL";
        };

        int etaDays = switch (ctx.country()) {
            case "FR" -> (ctx.deliveryMode() == DeliveryMode.EXPRESS) ? 1 : 2;
            case "EU" -> (ctx.deliveryMode() == DeliveryMode.EXPRESS) ? 2 : 4;
            default   -> (ctx.deliveryMode() == DeliveryMode.EXPRESS) ? 3 : 8;
        };

        return new ShippingQuote(cost, carrier, etaDays, label());
    }

    String label();
}
