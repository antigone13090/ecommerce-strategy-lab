package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.money.Money;

public final class ZeroPaymentFeeStrategy implements PaymentFeeStrategy {

    @Override
    public PaymentFeeQuote quote(Money totalBeforeFee, CheckoutContext ctx) {
        return new PaymentFeeQuote(Money.of("0.00"), label());
    }

    @Override
    public String label() {
        return "PAYMENT_FEE_NONE";
    }
}
