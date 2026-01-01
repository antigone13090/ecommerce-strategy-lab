package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.money.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PaymentFeeByMethodStrategy implements PaymentFeeStrategy {

    @Override
    public PaymentFeeQuote quote(Money totalBeforeFee, CheckoutContext ctx) {

        PaymentMethod pm = (ctx == null || ctx.paymentMethod() == null)
                ? PaymentMethod.CARD
                : ctx.paymentMethod();

        BigDecimal base = totalBeforeFee.asBigDecimal();

        // Exemples “réalistes” (à ajuster)
        BigDecimal rate;
        BigDecimal fixed;

        switch (pm) {
            case PAYPAL -> {
                rate = new BigDecimal("0.029");
                fixed = new BigDecimal("0.35");
            }
            default -> { // CARD
                rate = new BigDecimal("0.014");
                fixed = new BigDecimal("0.25");
            }
        }

        BigDecimal fee = base.multiply(rate).add(fixed).setScale(2, RoundingMode.HALF_UP);

        return new PaymentFeeQuote(Money.of(fee.toPlainString()), "PAYMENT_FEE:" + pm.name());
    }

    @Override
    public String label() {
        return "FEE_BY_METHOD";
    }
}
