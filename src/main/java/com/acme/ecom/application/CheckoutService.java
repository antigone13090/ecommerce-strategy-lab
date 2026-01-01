package com.acme.ecom.application;

import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.pricing.CheckoutContext;
import com.acme.ecom.domain.pricing.PricingResult;
import com.acme.ecom.domain.pricing.PricingService;

public final class CheckoutService {

    private final PricingService pricingService;

    public CheckoutService(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    public PricingResult quote(Cart cart, CheckoutContext ctx) {
        return pricingService.quote(cart, ctx);
    }
}
