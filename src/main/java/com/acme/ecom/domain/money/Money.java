package com.acme.ecom.domain.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Money {

    private static final int SCALE = 2;
    private static final RoundingMode RM = RoundingMode.HALF_EVEN;

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount;
    }

    public static Money of(String value) {
        if (value == null || value.isBlank()) return new Money(BigDecimal.ZERO.setScale(SCALE, RM));
        BigDecimal bd = new BigDecimal(value.trim());
        return new Money(bd.setScale(SCALE, RM));
    }

    public BigDecimal asBigDecimal() {
        return amount;
    }

    public Money plus(Money other) {
        BigDecimal o = (other == null) ? BigDecimal.ZERO : other.amount;
        return new Money(this.amount.add(o).setScale(SCALE, RM));
    }

    public Money times(String factor) {
        BigDecimal f = new BigDecimal(factor);
        return new Money(this.amount.multiply(f).setScale(SCALE, RM));
    }

    @Override
    public String toString() {
        return amount.setScale(SCALE, RM).toPlainString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros());
    }
}
