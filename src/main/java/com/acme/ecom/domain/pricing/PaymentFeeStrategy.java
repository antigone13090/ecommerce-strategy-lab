package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.money.Money;

public interface PaymentFeeStrategy {

    record PaymentFeeQuote(Money fee, String label) {}

    PaymentFeeQuote quote(Money totalBeforeFee, CheckoutContext ctx);

    String label();
}
