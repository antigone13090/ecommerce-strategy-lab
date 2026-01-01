# Explications — Ecommerce Strategy Lab

## 1) Objectif du projet
Cette application est un mini-lab Spring Boot pour tester **le pattern Strategy** dans un contexte e-commerce :
- calcul d’un **subtotal** (somme des items)
- application de **remises** (loyalty, code promo)
- calcul de la **livraison** (en fonction du pays, poids, total, etc.)
- calcul de la **TVA**
- calcul de **frais de paiement**
- exposition via un endpoint HTTP : `POST /api/checkout/quote`

Le but est de pouvoir **remplacer/combiner des règles métier** (remise, shipping, taxe, paiement) sans modifier tout le code.

---

## 2) Le pattern Strategy (principe)
### Problème
Si tu codes les règles métier avec des `if/else` partout (ex: promo, loyalty, country, poids, moyens de paiement), le code devient :
- difficile à lire
- difficile à tester
- difficile à faire évoluer (toute modification casse autre chose)

### Solution : Strategy
On définit une **interface** (ex: `DiscountStrategy`) et plusieurs **implémentations** (ex: `LoyaltyDiscountStrategy`, `PromoCodeDiscountStrategy`, etc.).
Le service central (ex: `PricingService`) ne connaît que l’interface.
Il applique la stratégie choisie/configurée.

Bénéfices :
- ajouter une nouvelle règle = **nouvelle classe**, pas un “gros if”
- tests unitaires simples (une stratégie = un comportement)
- configuration flexible (changer l’ordre ou les stratégies actives)

---

## 3) Stratégies présentes dans le projet (vue d’ensemble)
Chemin principal : `src/main/java/com/acme/ecom/domain/pricing/`

### A) Remises (discount)
- `DiscountStrategy` : interface
- `LoyaltyDiscountStrategy` : remise selon niveau fidélité
- `PromoCodeDiscountStrategy` : remise selon code promo

### B) Livraison (shipping)
- `ShippingStrategy` : interface
- `CountryBasedShippingAfterDiscountsStrategy` / `FixedShippingAfterDiscountsStrategy` / `WeightZoneShippingAfterDiscountsStrategy` :
  calcul de livraison selon pays/zone + poids, en utilisant le total **après remises**.

### C) Taxes
- `TaxStrategy` : interface
- `VatByCountryTaxStrategy` : TVA selon pays/zone (EU → TVA, etc.)

### D) Frais de paiement
- `PaymentFeeStrategy` : interface
- `PaymentFeeByMethodStrategy` : frais selon méthode (`CARD`, etc.)
- `ZeroPaymentFeeStrategy` : aucun frais (fallback)

### E) Résultat central
- `PricingResult` : regroupe tous les montants calculés + détails (“applied rules”)
- `PricingService` : orchestre l’appel aux stratégies dans l’ordre logique

---

## 4) Flux de l’application (du navigateur au calcul)
1. Le navigateur charge `/` (fichiers statiques : `static/index.html`, `app.js`, `style.css`)
2. Le bouton “Calculer” appelle `POST /api/checkout/quote`
3. `CheckoutController` reçoit le JSON (`QuoteRequest`)
4. Le controller appelle le service (ex: `CheckoutService` / `PricingService`)
5. `PricingService` :
   - calcule subtotal
   - applique discounts
   - calcule shipping
   - calcule taxes
   - calcule frais de paiement
6. Retour JSON (`QuoteResponse`) avec :
   - subtotal, afterDiscounts, shippingCost, taxAmount, paymentFee, finalTotal
   - liste des règles appliquées (utile pour debug)

---

## 5) Lancer l’application en local (commandes exactes)

### 5.1 Prérequis
- Java 17
- Gradle wrapper (déjà inclus)

Vérifier Java :
```bash
java -version
