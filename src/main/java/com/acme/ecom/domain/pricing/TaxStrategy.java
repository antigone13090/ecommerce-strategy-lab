package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.money.Money;

import java.math.BigDecimal;

public interface TaxStrategy {

    record TaxQuote(Money amount, BigDecimal rate, String label) {}

    TaxQuote quote(Money taxableBase, Cart cart, CheckoutContext ctx);

    String label();
}
