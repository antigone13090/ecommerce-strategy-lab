package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.model.CartItem;
import com.acme.ecom.domain.money.Money;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PricingServiceTest {

    private PricingService service() {
        return new PricingService(
                List.of(new LoyaltyDiscountStrategy(), new PromoCodeDiscountStrategy()),
                new FixedShippingAfterDiscountsStrategy(Money.of("4.90"), Money.of("50.00"))
        );
    }

    @Test
    void scenario_subtotal100_gold_plus_noel10() {
        Cart cart = new Cart(List.of(new CartItem("A", Money.of("100.00"), 1)));
        PricingResult inv = service().quote(cart, new CheckoutContext(LoyaltyTier.GOLD, "NOEL10", "FR"));

        assertEquals("100.00", inv.subtotal().toString());
        assertEquals("85.50", inv.totalAfterDiscounts().toString());
    }

    @Test
    void scenario_subtotal55_shipping_paid() {
        Cart cart = new Cart(List.of(new CartItem("A", Money.of("55.00"), 1)));
        PricingResult inv = service().quote(cart, new CheckoutContext(LoyaltyTier.GOLD, "NOEL10", "FR"));

        assertEquals("47.02", inv.totalAfterDiscounts().toString());
        assertEquals("4.90", inv.shippingCost().toString());
        assertEquals("51.92", inv.finalTotal().toString());
    }

    @Test
    void scenario_subtotal60_free_shipping() {
        Cart cart = new Cart(List.of(new CartItem("A", Money.of("60.00"), 1)));
        PricingResult inv = service().quote(cart, new CheckoutContext(LoyaltyTier.GOLD, "NOEL10", "FR"));

        assertEquals("51.30", inv.totalAfterDiscounts().toString());
        assertEquals("0.00", inv.shippingCost().toString());
        assertEquals("51.30", inv.finalTotal().toString());
    }
}
