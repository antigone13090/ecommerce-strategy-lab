package com.acme.ecom.domain.model;

import com.acme.ecom.domain.money.Money;

public record CartItem(
        String sku,
        Money unitPrice,
        int qty,
        int weightGrams
) {
    // compat : ancien code/tests qui faisaient new CartItem(sku, price, qty)
    public CartItem(String sku, Money unitPrice, int qty) {
        this(sku, unitPrice, qty, 0);
    }
}
