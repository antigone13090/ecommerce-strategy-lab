package com.acme.ecom.web;

import com.acme.ecom.application.CheckoutService;
import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.model.CartItem;
import com.acme.ecom.domain.money.Money;
import com.acme.ecom.domain.pricing.*;
import com.acme.ecom.web.dto.QuoteRequest;
import com.acme.ecom.web.dto.QuoteResponse;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/quote")
    public QuoteResponse quote(@RequestBody QuoteRequest req) {

        // items -> Cart
        List<CartItem> items = new ArrayList<>();
        if (req.items() != null) {
            for (QuoteRequest.Item it : req.items()) {
                if (it == null) continue;
                int w = (it.weightGrams() == null) ? 0 : it.weightGrams();
                items.add(new CartItem(it.sku(), Money.of(it.unitPrice()), it.qty(), w));
            }
        }
        Cart cart = new Cart(items);

        // champs request + defaults
        String country = (req.country() == null || req.country().isBlank()) ? "OTHER" : req.country().trim().toUpperCase();
        DeliveryMode mode = (req.deliveryMode() == null) ? DeliveryMode.STANDARD : req.deliveryMode();
        LoyaltyTier tier = (req.loyaltyTier() == null) ? LoyaltyTier.NONE : req.loyaltyTier();
        PaymentMethod pm = (req.paymentMethod() == null) ? PaymentMethod.CARD : req.paymentMethod();

        // promoCodes : nouvelle liste sinon fallback promoCode
        List<String> codes = (req.promoCodes() != null) ? req.promoCodes() : List.of();
        if (codes.isEmpty() && req.promoCode() != null && !req.promoCode().isBlank()) {
            codes = List.of(req.promoCode());
        }

        CheckoutContext ctx = new CheckoutContext(country, mode, tier, codes, pm);

        PricingResult result = checkoutService.quote(cart, ctx);
        return QuoteResponse.from(result);
    }
}
