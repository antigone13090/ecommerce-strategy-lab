package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.money.Money;

public interface ShippingStrategy {
    Money compute(Money totalAfterDiscounts, Cart cart, CheckoutContext ctx);
    String label();
}
