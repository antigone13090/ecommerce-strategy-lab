# Diagrammes — Ecommerce Strategy Lab

## 1) Diagramme de séquence (flux HTTP)

```text
Navigateur
  |
  |  GET /
  v
Spring Boot (Tomcat)  --->  static/index.html + app.js + style.css
  |
  |  (app.js) POST /api/checkout/quote  JSON (QuoteRequest)
  v
CheckoutController
  |
  v
CheckoutService / PricingService
  |
  |-- subtotal = somme(items)
  |
  |-- Discounts (Strategy)
  |     - LoyaltyDiscountStrategy
  |     - PromoCodeDiscountStrategy
  |   => afterDiscounts
  |
  |-- Shipping (Strategy) (souvent basé sur afterDiscounts)
  |     - WeightZoneShippingAfterDiscountsStrategy
  |     - CountryBasedShippingAfterDiscountsStrategy
  |     - FixedShippingAfterDiscountsStrategy
  |
  |-- Taxes (Strategy)
  |     - VatByCountryTaxStrategy
  |
  |-- Payment fee (Strategy)
  |     - PaymentFeeByMethodStrategy
  |     - ZeroPaymentFeeStrategy (fallback)
  |
  v
PricingResult  -->  QuoteResponse (JSON)
  |
  v
Navigateur (affiche le JSON + KPI)
com.acme.ecom
├── web
│   ├── CheckoutController          (HTTP: /api/checkout/quote)
│   └── dto
│       ├── QuoteRequest            (entrée JSON)
│       └── QuoteResponse           (sortie JSON)
│
├── application
│   └── CheckoutService             (cas d’usage)
│
├── domain
│   ├── money
│   │   └── Money                   (valeurs monétaires)
│   └── pricing
│       ├── PricingService          (orchestrateur)
│       ├── PricingResult           (résultat interne)
│       ├── CheckoutContext         (contexte de calcul)
│       │
│       ├── DiscountStrategy        (interface)
│       ├── LoyaltyDiscountStrategy (impl)
│       ├── PromoCodeDiscountStrategy (impl)
│       │
│       ├── ShippingStrategy        (interface)
│       ├── WeightZoneShippingAfterDiscountsStrategy (impl)
│       ├── CountryBasedShippingAfterDiscountsStrategy (impl)
│       ├── FixedShippingAfterDiscountsStrategy (impl)
│       │
│       ├── TaxStrategy             (interface)
│       └── VatByCountryTaxStrategy (impl)
│
└── config
    └── PricingConfig               (assemblage / wiring des stratégies)
