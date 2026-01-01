# Ecommerce Strategy Lab (Spring Boot)

Mini-projet de démonstration du pattern **Strategy** appliqué au pricing e-commerce :
- remises (loyalty / code promo)
- livraison (shipping)
- taxes (TVA)
- frais de paiement
- endpoint HTTP pour calculer un devis (quote)

## Prérequis
- Java 17 (recommandé)
- Gradle wrapper inclus (`./gradlew`)
- (optionnel) `jq` pour afficher joliment le JSON

## Lancer en local

### 1) Tests
```bash
./gradlew clean test
