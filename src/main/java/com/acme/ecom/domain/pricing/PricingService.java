package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.model.CartItem;
import com.acme.ecom.domain.money.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public final class PricingService {

    private final List<DiscountStrategy> discounts;
    private final ShippingStrategy shippingStrategy;
    private final TaxStrategy taxStrategy;
    private final PaymentFeeStrategy paymentFeeStrategy;

    public PricingService(
            List<DiscountStrategy> discounts,
            ShippingStrategy shippingStrategy,
            TaxStrategy taxStrategy,
            PaymentFeeStrategy paymentFeeStrategy
    ) {
        this.discounts = (discounts == null) ? List.of() : discounts;
        this.shippingStrategy = shippingStrategy;
        this.taxStrategy = taxStrategy;
        this.paymentFeeStrategy = paymentFeeStrategy;
    }

    // compat : anciens tests
    public PricingService(List<DiscountStrategy> discounts, ShippingStrategy shippingStrategy) {
        this(discounts, shippingStrategy, null, null);
    }

    public PricingResult quote(Cart cart, CheckoutContext ctx) {

        Money subtotal = cartSubtotal(cart);

        // 1) Discounts séquentiels
        Money afterDiscounts = subtotal;
        List<String> applied = new ArrayList<>();

        for (DiscountStrategy d : discounts) {
            if (d == null) continue;
            DiscountStrategy.DiscountApplication app = d.apply(afterDiscounts, cart, ctx);
            if (app == null) continue;

            afterDiscounts = normalize(app.newTotal());

            if (app.applied()) applied.add(app.label());
        }

        // 2) Shipping (quote => coût + délais + transporteur interne)
        ShippingStrategy.ShippingQuote ship = shippingStrategy.quote(afterDiscounts, cart, ctx);
        Money shippingCost = normalize(ship.cost());
        applied.add("SHIPPING:" + ship.label());
Money deliveredBase = add(afterDiscounts, shippingCost);

        // 3) Tax (TVA)
        Money taxAmount = Money.of("0.00");
        BigDecimal vatRate = BigDecimal.ZERO;

        if (taxStrategy != null) {
            TaxStrategy.TaxQuote tq = taxStrategy.quote(deliveredBase, cart, ctx);
            taxAmount = normalize(tq.amount());
            vatRate = (tq.rate() == null) ? BigDecimal.ZERO : tq.rate();

            if (taxAmount.asBigDecimal().compareTo(BigDecimal.ZERO) > 0) {
                applied.add("TAX:" + tq.label());
            }
        }

        Money totalBeforeFee = add(deliveredBase, taxAmount);

        // 4) Frais de paiement (CB / PayPal)
        Money paymentFee = Money.of("0.00");
        if (paymentFeeStrategy != null) {
            PaymentFeeStrategy.PaymentFeeQuote fq = paymentFeeStrategy.quote(totalBeforeFee, ctx);
            paymentFee = normalize(fq.fee());

            if (paymentFee.asBigDecimal().compareTo(BigDecimal.ZERO) > 0) {
                applied.add(fq.label());
            }
        }

        Money finalTotal = add(totalBeforeFee, paymentFee);

        // IMPORTANT : respecte l’ordre exact demandé par TON PricingResult (vu dans l’erreur)
        return new PricingResult(
                subtotal,
                afterDiscounts,
                shippingCost,
                ship.etaDays(),
                ship.label(),
                taxAmount,
                vatRate,
                paymentFee,
                ctx.paymentMethod(),
                finalTotal,
                applied
        );
    }

    private static Money cartSubtotal(Cart cart) {
        if (cart == null || cart.items() == null) return Money.of("0.00");
        BigDecimal sum = BigDecimal.ZERO;

        for (CartItem it : cart.items()) {
            if (it == null || it.unitPrice() == null) continue;
            BigDecimal line = it.unitPrice().asBigDecimal().multiply(BigDecimal.valueOf(it.qty()));
            sum = sum.add(line);
        }
        return Money.of(sum.setScale(2, RoundingMode.HALF_EVEN).toPlainString());
    }

    private static Money add(Money a, Money b) {
        BigDecimal x = (a == null) ? BigDecimal.ZERO : a.asBigDecimal();
        BigDecimal y = (b == null) ? BigDecimal.ZERO : b.asBigDecimal();
        return Money.of(x.add(y).setScale(2, RoundingMode.HALF_EVEN).toPlainString());
    }

    private static Money normalize(Money m) {
        if (m == null) return Money.of("0.00");
        return Money.of(m.asBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toPlainString());
    }
}
