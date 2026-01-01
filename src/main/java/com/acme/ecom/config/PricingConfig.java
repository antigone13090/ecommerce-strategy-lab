package com.acme.ecom.config;

import com.acme.ecom.application.CheckoutService;
import com.acme.ecom.domain.money.Money;
import com.acme.ecom.domain.pricing.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    // ✅ stratégie cohérente avec CountryShippingPricingTest + IT
    @Bean
    public ShippingStrategy shippingStrategy() {
        return new CountryBasedShippingAfterDiscountsStrategy(
                Money.of("50.00"),
                Map.of(
                        "FR", Money.of("4.90"),
                        "EU", Money.of("7.90"),
                        "OTHER", Money.of("14.90")
                )
        );
    }

    @Bean
    public TaxStrategy taxStrategy() {
        return new VatByCountryTaxStrategy(
                Map.of(
                        "FR", new BigDecimal("0.20"),
                        "EU", new BigDecimal("0.21"),
                        "OTHER", new BigDecimal("0.00")
                ),
                new BigDecimal("0.20")
        );
    }

    @Bean
    public PaymentFeeStrategy paymentFeeStrategy() {
        return new PaymentFeeByMethodStrategy();
    }

    @Bean
    public PricingService pricingService(
            DiscountStrategy loyaltyDiscountStrategy,
            DiscountStrategy promoCodeDiscountStrategy,
            ShippingStrategy shippingStrategy,
            TaxStrategy taxStrategy,
            PaymentFeeStrategy paymentFeeStrategy
    ) {
        return new PricingService(
                List.of(loyaltyDiscountStrategy, promoCodeDiscountStrategy),
                shippingStrategy,
                taxStrategy,
                paymentFeeStrategy
        );
    }

    @Bean
    public CheckoutService checkoutService(PricingService pricingService) {
        return new CheckoutService(pricingService);
    }
}
