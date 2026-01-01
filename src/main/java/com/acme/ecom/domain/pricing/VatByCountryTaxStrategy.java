package com.acme.ecom.domain.pricing;

import com.acme.ecom.domain.model.Cart;
import com.acme.ecom.domain.money.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public final class VatByCountryTaxStrategy implements TaxStrategy {

    private final Map<String, BigDecimal> ratesByCountry;
    private final BigDecimal defaultRate;

    public VatByCountryTaxStrategy(Map<String, BigDecimal> ratesByCountry, BigDecimal defaultRate) {
        this.ratesByCountry = ratesByCountry;
        this.defaultRate = defaultRate;
    }

    @Override
    public TaxQuote quote(Money taxableBase, Cart cart, CheckoutContext ctx) {
        String c = (ctx == null || ctx.country() == null) ? "OTHER" : ctx.country().trim().toUpperCase();
        BigDecimal rate = ratesByCountry.getOrDefault(c, defaultRate);

        BigDecimal base = taxableBase.asBigDecimal();
        BigDecimal tax = base.multiply(rate).setScale(2, RoundingMode.HALF_UP);

        return new TaxQuote(Money.of(tax.toPlainString()), rate, "VAT:" + c);
    }

    @Override
    public String label() {
        return "VAT_BY_COUNTRY";
    }
}
