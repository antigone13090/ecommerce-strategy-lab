set -euo pipefail

# Dossiers
mkdir -p src/main/java/com/acme/ecom/domain/money
mkdir -p src/main/java/com/acme/ecom/domain/model
mkdir -p src/main/java/com/acme/ecom/domain/pricing
mkdir -p src/main/java/com/acme/ecom/application
mkdir -p src/main/java/com/acme/ecom/web/dto
mkdir -p src/main/java/com/acme/ecom/web
mkdir -p src/main/java/com/acme/ecom/config
mkdir -p src/test/java/com/acme/ecom/domain/pricing

# Money
cat > src/main/java/com/acme/ecom/domain/money/Money.java <<'JAVA'
package com.acme.ecom.domain.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Money {
    private final BigDecimal value;

    private Money(BigDecimal v) {
        this.value = v.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(String s) {
        return new Money(new BigDecimal(s));
    }

    public BigDecimal asBigDecimal() { return value; }

    public Money plus(Money other) { return new Money(this.value.add(other.value)); }

    public Money minus(Money other) { return new Money(this.value.subtract(other.value)); }

    public Money percent(String pct) {
        BigDecimal p = new BigDecimal(pct).divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
        return new Money(this.value.multiply(p));
    }

    public int compareTo(Money other) { return this.value.compareTo(other.value); }

    @Override public String toString() { return value.toPlainString(); }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money)) return false;
        Money money = (Money) o;
        return Objects.equals(value, money.value);
    }

    @Override public int hashCode() { return Objects.hash(value); }
}
JAVA

# Cart
cat > src/main/java/com/acme/ecom/domain/model/CartItem.java <<'JAVA'
package com.acme.ecom.domain.model;

import com.acme.ecom.domain.money.Money;

public record CartItem(String sku, Money unitPrice, int qty) {}
JAVA

cat > src/main/java/com/acme/ecom/domain/model/Cart.java <<'JAVA'
package com.acme.ecom.domain.model;

import com.acme.ecom.domain.money.Money;

import java.math.BigDecimal;
import java.util.List;

public record Cart(List<CartItem> items) {
    public Money subtotal() {
        Money sum = Money.of("0.00");
        for (CartItem it : items) {
            BigDecimal line = it.unitPrice().asBigDecimal().multiply(new BigDecimal(it.qty()));
            sum = sum.plus(Money.of(line.toPlainString()));
        }
        return sum;
    }
}
JAVA

# Pricing context
cat > src/main/java/com/acme/ecom/domain/pricing/LoyaltyTier.java <<'JAVA'
package com.acme.ecom.domain.pricing;

public enum LoyaltyTier { NONE, SILVER, GOLD }
JAVA

cat > src/main/java/com/acme/ecom/domain/pricing/CheckoutContext.java <<'JAVA'
package com.acme.ecom.domain.pricing;

public record CheckoutContext(LoyaltyTier loyaltyTier, String promoCode, String country) {}
JAVA

# Discount Strategy
cat > src/main/java/com/acme/ecom/domain/pricing/DiscountStrategy.java <<'JAVA'
package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.money.Money;

public interface DiscountStrategy {
    Money apply(Money currentTotal, Cart cart, CheckoutContext ctx);
    String label();
}
JAVA

cat > src/main/java/com/acme/ecom/domain/pricing/LoyaltyDiscountStrategy.java <<'JAVA'
package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.money.Money;

public final class LoyaltyDiscountStrategy implements DiscountStrategy {

    @Override
    public Money apply(Money currentTotal, Cart cart, CheckoutContext ctx) {
        if (ctx.loyaltyTier() == LoyaltyTier.GOLD) {
            return currentTotal.minus(currentTotal.percent("10"));
        }
        if (ctx.loyaltyTier() == LoyaltyTier.SILVER) {
            return currentTotal.minus(currentTotal.percent("5"));
        }
        return currentTotal;
    }

    @Override
    public String label() {
        return "LOYALTY";
    }
}
JAVA

cat > src/main/java/com/acme/ecom/domain/pricing/PromoCodeDiscountStrategy.java <<'JAVA'
package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.money.Money;

public final class PromoCodeDiscountStrategy implements DiscountStrategy {

    @Override
    public Money apply(Money currentTotal, Cart cart, CheckoutContext ctx) {
        if (ctx.promoCode() == null) return currentTotal;
        if ("NOEL10".equalsIgnoreCase(ctx.promoCode())) {
            // 5% appliqué après fidélité (si appelé en 2ème)
            return currentTotal.minus(currentTotal.percent("5"));
        }
        return currentTotal;
    }

    @Override
    public String label() {
        return "PROMO_CODE";
    }
}
JAVA

# Shipping Strategy (après remises)
cat > src/main/java/com/acme/ecom/domain/pricing/ShippingStrategy.java <<'JAVA'
package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.money.Money;

public interface ShippingStrategy {
    Money compute(Money totalAfterDiscounts, Cart cart, CheckoutContext ctx);
    String label();
}
JAVA

cat > src/main/java/com/acme/ecom/domain/pricing/FixedShippingAfterDiscountsStrategy.java <<'JAVA'
package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.money.Money;

public final class FixedShippingAfterDiscountsStrategy implements ShippingStrategy {

    private final Money shippingFee;
    private final Money freeThreshold;

    public FixedShippingAfterDiscountsStrategy(Money shippingFee, Money freeThreshold) {
        this.shippingFee = shippingFee;
        this.freeThreshold = freeThreshold;
    }

    @Override
    public Money compute(Money totalAfterDiscounts, Cart cart, CheckoutContext ctx) {
        return (totalAfterDiscounts.compareTo(freeThreshold) < 0) ? shippingFee : Money.of("0.00");
    }

    @Override
    public String label() {
        return "FIXED_AFTER_DISCOUNTS";
    }
}
JAVA

# Result + PricingService
cat > src/main/java/com/acme/ecom/domain/pricing/PricingResult.java <<'JAVA'
package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.money.Money;

import java.util.List;

public record PricingResult(
        Money subtotal,
        Money totalAfterDiscounts,
        Money shippingCost,
        Money finalTotal,
        List<String> appliedDiscounts,
        String shippingRule
) {}
JAVA

cat > src/main/java/com/acme/ecom/domain/pricing/PricingService.java <<'JAVA'
package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.money.Money;

import java.util.ArrayList;
import java.util.List;

public final class PricingService {

    private final List<DiscountStrategy> discountStrategies;
    private final ShippingStrategy shippingStrategy;

    public PricingService(List<DiscountStrategy> discountStrategies, ShippingStrategy shippingStrategy) {
        this.discountStrategies = discountStrategies;
        this.shippingStrategy = shippingStrategy;
    }

    public PricingResult quote(Cart cart, CheckoutContext ctx) {
        Money subtotal = cart.subtotal();

        Money current = subtotal;
        List<String> applied = new ArrayList<>();

        for (DiscountStrategy ds : discountStrategies) {
            Money next = ds.apply(current, cart, ctx);
            if (!next.equals(current)) applied.add(ds.label());
            current = next;
        }

        Money totalAfterDiscounts = current;
        Money shipping = shippingStrategy.compute(totalAfterDiscounts, cart, ctx);
        Money finalTotal = totalAfterDiscounts.plus(shipping);

        return new PricingResult(subtotal, totalAfterDiscounts, shipping, finalTotal, applied, shippingStrategy.label());
    }
}
JAVA

# Application layer
cat > src/main/java/com/acme/ecom/application/CheckoutService.java <<'JAVA'
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
JAVA

# Spring config (ordre des remises)
cat > src/main/java/com/acme/ecom/config/PricingConfig.java <<'JAVA'
package com.acme.ecom.config;

import com.acme.ecom.application.CheckoutService;
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
        return new FixedShippingAfterDiscountsStrategy(Money.of("4.90"), Money.of("50.00"));
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
JAVA

# Web DTO + Controller
cat > src/main/java/com/acme/ecom/web/dto/QuoteRequest.java <<'JAVA'
package com.acme.ecom.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;

public class QuoteRequest {

    @NotEmpty
    public List<Item> items;

    public String loyaltyTier; // NONE/SILVER/GOLD
    public String promoCode;

    @NotBlank
    public String country;

    public static class Item {
        @NotBlank public String sku;
        @NotBlank public String unitPrice;
        @Positive public int qty;
    }
}
JAVA

cat > src/main/java/com/acme/ecom/web/CheckoutController.java <<'JAVA'
package com.acme.ecom.web;

import com.acme.ecom.application.CheckoutService;
import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.model.CartItem;
import com.acme.ecom.domain.money.Money;
import com.acme.ecom.domain.pricing.CheckoutContext;
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
                .map(i -> new CartItem(i.sku, Money.of(i.unitPrice), i.qty))
                .toList();

        Cart cart = new Cart(items);

        LoyaltyTier tier = (req.loyaltyTier == null) ? LoyaltyTier.NONE : LoyaltyTier.valueOf(req.loyaltyTier);
        CheckoutContext ctx = new CheckoutContext(tier, req.promoCode, req.country);

        return checkoutService.quote(cart, ctx);
    }
}
JAVA

# Tests (3 scénarios)
cat > src/test/java/com/acme/ecom/domain/pricing/PricingServiceTest.java <<'JAVA'
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
JAVA

echo "OK: cas B installé."
