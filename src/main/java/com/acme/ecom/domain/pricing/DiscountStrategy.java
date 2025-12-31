package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.money.Money;

public interface DiscountStrategy {
    Money apply(Money currentTotal, Cart cart, CheckoutContext ctx);
    String label();
}
