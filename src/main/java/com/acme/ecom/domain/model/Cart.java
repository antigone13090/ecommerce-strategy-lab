package com.acme.ecom.domain.model;

import com.acme.ecom.domain.money.Money;

import java.math.BigDecimal;
import java.util.List;

public record Cart(List<CartItem> items) {
    public Money subtotal() {
        Money sum = Money.of("0.00");
        for (CartItem it : items) {
            BigDecimal line = it.unitPrice().asBigDecimal().multiply(new BigDecimal(it.qty()));
            sum = sum.plus(Money.of(line.toPlainString()));
        }
        return sum;
    }

    public int totalWeightGrams() {
        return items.stream().mapToInt(i -> i.weightGrams() * i.qty()).sum();
    }
}
