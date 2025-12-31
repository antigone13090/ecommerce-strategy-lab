package com.acme.ecom.web;

import com.acme.ecom.application.CheckoutService;
import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.model.CartItem;
import com.acme.ecom.domain.money.Money;
import com.acme.ecom.domain.pricing.CheckoutContext;
import com.acme.ecom.domain.pricing.DeliveryMode;
import com.acme.ecom.domain.pricing.LoyaltyTier;
import com.acme.ecom.domain.pricing.PricingResult;
import com.acme.ecom.web.dto.QuoteRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/quote")
    public PricingResult quote(@Valid @RequestBody QuoteRequest req) {
        List<CartItem> items = req.items.stream()
                .map(i -> new CartItem(i.sku, Money.of(i.unitPrice), i.qty, (i.weightGrams==null?0:i.weightGrams)))
                .toList();

        Cart cart = new Cart(items);

        LoyaltyTier tier = LoyaltyTier.NONE;
        if (req.loyaltyTier != null && !req.loyaltyTier.isBlank()) {
            tier = LoyaltyTier.valueOf(req.loyaltyTier.trim().toUpperCase());
        }

        String promo = (req.promoCode == null) ? "" : req.promoCode.trim();
        String country = (req.country == null || req.country.isBlank()) ? "FR" : req.country.trim().toUpperCase();

        DeliveryMode mode = DeliveryMode.STANDARD;
        if (req.deliveryMode != null && !req.deliveryMode.isBlank()) {
            mode = DeliveryMode.valueOf(req.deliveryMode.trim().toUpperCase());
        }

        CheckoutContext ctx = new CheckoutContext(tier, promo, country, mode);

return checkoutService.quote(cart, ctx);
    }
}
