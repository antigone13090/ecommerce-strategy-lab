package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.money.Money;

public interface DiscountStrategy {

    record DiscountApplication(Money newTotal, boolean applied, String label) {}

    DiscountApplication apply(Money currentTotal, Cart cart, CheckoutContext ctx);

    String label();
}
