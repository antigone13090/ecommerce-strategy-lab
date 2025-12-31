package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.money.Money;

import java.util.ArrayList;
import java.util.List;

public final class PricingService {

    private final List<DiscountStrategy> discountStrategies;
    private final ShippingStrategy shippingStrategy;

    public PricingService(List<DiscountStrategy> discountStrategies, ShippingStrategy shippingStrategy) {
        this.discountStrategies = discountStrategies;
        this.shippingStrategy = shippingStrategy;
    }

    public PricingResult quote(Cart cart, CheckoutContext ctx) {
        Money subtotal = cart.subtotal();

        Money current = subtotal;
        List<String> applied = new ArrayList<>();

        for (DiscountStrategy ds : discountStrategies) {
            Money next = ds.apply(current, cart, ctx);
            if (!next.equals(current)) applied.add(ds.label());
            current = next;
        }

        Money totalAfterDiscounts = current;
        Money shipping = shippingStrategy.compute(totalAfterDiscounts, cart, ctx);
        Money finalTotal = totalAfterDiscounts.plus(shipping);

        return new PricingResult(subtotal, totalAfterDiscounts, shipping, finalTotal, applied, shippingStrategy.label());
    }
}
