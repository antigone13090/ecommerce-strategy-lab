package com.acme.ecom.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public final class QuoteRequest {

    @NotEmpty
    @Valid
    public List<Item> items;

    @NotBlank
    public String loyaltyTier;

    public String promoCode;

    @NotBlank
    public String country;

    // optionnel: STANDARD si absent
    public String deliveryMode;

    public static final class Item {
        @NotBlank public String sku;
        @NotBlank public String unitPrice;
        @Min(1)   public int qty;

        // optionnel: 0 si absent
        @Min(0)   public Integer weightGrams;
    }
}
