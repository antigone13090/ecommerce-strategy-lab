package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.money.Money;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PromoCodeDiscountStrategy implements DiscountStrategy {

    // code -> (rate, stackable)
    private final Map<String, Promo> catalog = new LinkedHashMap<>();

    public PromoCodeDiscountStrategy() {
        catalog.put("NOEL10", new Promo(new BigDecimal("0.10"), false));
        catalog.put("VIP5",   new Promo(new BigDecimal("0.05"), true));
        catalog.put("FLASH3", new Promo(new BigDecimal("0.03"), true));
    }

    private record Promo(BigDecimal rate, boolean stackable) {}

    @Override
    public DiscountApplication apply(Money currentTotal, Cart cart, CheckoutContext ctx) {

        List<String> codes = ctx.promoCodes();
        if (codes == null || codes.isEmpty()) {
            return new DiscountApplication(currentTotal, false, label());
        }

        // garder uniquement ceux connus
        List<String> known = codes.stream()
                .map(s -> s.trim().toUpperCase())
                .filter(catalog::containsKey)
                .distinct()
                .toList();

        if (known.isEmpty()) {
            return new DiscountApplication(currentTotal, false, label());
        }

        boolean hasNonStackable = known.stream().anyMatch(c -> !catalog.get(c).stackable());

        Money out = currentTotal;

        if (hasNonStackable) {
            // appliquer le meilleur code non-cumulable (max discount)
            String best = null;
            BigDecimal bestDiscount = BigDecimal.ZERO;

            for (String c : known) {
                Promo p = catalog.get(c);
                if (p.stackable()) continue;
                BigDecimal disc = out.asBigDecimal().multiply(p.rate());
                if (disc.compareTo(bestDiscount) > 0) {
                    bestDiscount = disc;
                    best = c;
                }
            }

            if (best == null) return new DiscountApplication(currentTotal, false, label());

            Promo p = catalog.get(best);
            out = Money.of(out.asBigDecimal().multiply(BigDecimal.ONE.subtract(p.rate())).toPlainString());
            return new DiscountApplication(out, true, "COUPON:" + best);
        }

        // sinon, appliquer tous les cumulables (séquentiel)
        for (String c : known) {
            Promo p = catalog.get(c);
            out = Money.of(out.asBigDecimal().multiply(BigDecimal.ONE.subtract(p.rate())).toPlainString());
        }
        return new DiscountApplication(out, true, label());
    }

    @Override
    public String label() {
        return "PROMO_CODES";
    }
}
