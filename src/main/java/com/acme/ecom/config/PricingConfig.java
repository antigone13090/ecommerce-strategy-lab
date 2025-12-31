package com.acme.ecom.config;

import com.acme.ecom.application.CheckoutService;
import java.util.Map;
import com.acme.ecom.domain.money.Money;
import com.acme.ecom.domain.pricing.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PricingConfig {

    @Bean
    public DiscountStrategy loyaltyDiscountStrategy() {
        return new LoyaltyDiscountStrategy();
    }

    @Bean
    public DiscountStrategy promoCodeDiscountStrategy() {
        return new PromoCodeDiscountStrategy();
    }

    @Bean
    public ShippingStrategy shippingStrategy() {
        return new WeightZoneShippingAfterDiscountsStrategy(Money.of("50.00"));
    }



    @Bean
    public PricingService pricingService(
            DiscountStrategy loyaltyDiscountStrategy,
            DiscountStrategy promoCodeDiscountStrategy,
            ShippingStrategy shippingStrategy
    ) {
        return new PricingService(List.of(loyaltyDiscountStrategy, promoCodeDiscountStrategy), shippingStrategy);
    }

    @Bean
    public CheckoutService checkoutService(PricingService pricingService) {
        return new CheckoutService(pricingService);
    }
}
