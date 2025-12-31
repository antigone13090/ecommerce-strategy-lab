package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.model.CartItem;
import com.acme.ecom.domain.money.Money;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CountryShippingPricingTest {

    private PricingService serviceWithCountryShipping() {
        return new PricingService(
                List.of(new LoyaltyDiscountStrategy(), new PromoCodeDiscountStrategy()),
                new CountryBasedShippingAfterDiscountsStrategy(
                        Money.of("50.00"),
                        Map.of(
                                "FR", Money.of("4.90"),
                                "EU", Money.of("7.90"),
                                "OTHER", Money.of("12.90")
                        )
                )
        );
    }

    @Test
    void subtotal55_eu_shipping_790() {
        PricingService svc = serviceWithCountryShipping();
        Cart cart = new Cart(List.of(new CartItem("A", Money.of("55.00"), 1)));

        PricingResult inv = svc.quote(cart, new CheckoutContext(LoyaltyTier.GOLD, "NOEL10", "EU"));

        // 55 -> -10% = 49.50 ; -5% de 49.50 (=2.48) => 47.02
        assertEquals("47.02", inv.totalAfterDiscounts().toString());
        assertEquals("7.90", inv.shippingCost().toString());
        assertEquals("54.92", inv.finalTotal().toString());
    }

    @Test
    void subtotal60_other_free_shipping() {
        PricingService svc = serviceWithCountryShipping();
        Cart cart = new Cart(List.of(new CartItem("A", Money.of("60.00"), 1)));

        PricingResult inv = svc.quote(cart, new CheckoutContext(LoyaltyTier.GOLD, "NOEL10", "OTHER"));

        // 60 -> -10% = 54.00 ; -5% de 54 (=2.70) => 51.30 (>= 50 => shipping 0)
        assertEquals("51.30", inv.totalAfterDiscounts().toString());
        assertEquals("0.00", inv.shippingCost().toString());
        assertEquals("51.30", inv.finalTotal().toString());
    }
}
