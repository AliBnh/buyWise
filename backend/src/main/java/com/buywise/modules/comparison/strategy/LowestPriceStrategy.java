package com.buywise.modules.comparison.strategy;

import com.buywise.modules.comparison.model.Offer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * ==============================================================================================
 * ARCHITECTURAL DESIGN NOTE: STRATEGY PATTERN & DETERMINISTIC FINANCIAL PRECISION
 * ==============================================================================================
 * Design Patterns & Clean Code Principles:
 * 1. Strategy Pattern (GoF):
 *    - Concrete Strategy: Implements {@link ComparisonStrategy} focusing strictly on deterministic cost ranking.
 *    - Encapsulates the pricing algorithm, allowing dynamic runtime selection via {@link ComparisonStrategyFactory}.
 *
 * 2. Financial Precision (The "Why BigDecimal?" Question):
 *    - Never use float or double for monetary calculations! Floating-point IEEE 754 representations
 *      introduce precision drift (e.g., 0.1 + 0.2 = 0.30000000000000004).
 *    - We enforce {@link BigDecimal} with explicit {@link RoundingMode#HALF_UP} and fixed scale,
 *      preventing ArithmeticException: Non-terminating decimal expansion during divisions.
 *
 * 3. Total Cost Invariant:
 *    - Evaluates total price: `totalPrice = basePrice + deliveryFee + additionalFees - discountAmount`.
 *
 * 4. Normalization Formula:
 *    - The cheapest offer receives 100.00 points.
 *    - More expensive offers are penalized proportionally relative to the price range across offers:
 *      `Score = 100 - ((offerPrice - minPrice) / (maxPrice - minPrice) * 50)`.
 * ==============================================================================================
 */
@Component
public class LowestPriceStrategy implements ComparisonStrategy {

    @Override
    public StrategyType getStrategyType() {
        return StrategyType.LOWEST_PRICE;
    }

    @Override
    public String getDisplayName() {
        return "Lowest Total Price";
    }

    @Override
    public String getDescription() {
        return "Prioritizes minimal immediate out-of-pocket expenditure including delivery and discounts.";
    }

    @Override
    public List<OfferScoreResult> evaluate(List<Offer> offers) {
        if (offers == null || offers.isEmpty()) {
            return List.of();
        }

        // Sort ascending by total price
        List<Offer> sorted = new ArrayList<>(offers);
        sorted.sort(Comparator.comparing(Offer::getTotalPrice));

        BigDecimal minPrice = sorted.get(0).getTotalPrice();
        BigDecimal maxPrice = sorted.get(sorted.size() - 1).getTotalPrice();
        BigDecimal priceRange = maxPrice.subtract(minPrice);

        List<OfferScoreResult> results = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            Offer offer = sorted.get(i);
            BigDecimal score;
            if (priceRange.compareTo(BigDecimal.ZERO) == 0) {
                score = new BigDecimal("100.00");
            } else {
                // Score = 100 - ((price - min) / range * 50)
                BigDecimal penalty = offer.getTotalPrice().subtract(minPrice)
                        .divide(priceRange, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("50.00"));
                score = new BigDecimal("100.00").subtract(penalty).setScale(2, RoundingMode.HALF_UP);
            }

            BigDecimal diffFromLowest = offer.getTotalPrice().subtract(minPrice);
            String rationale;
            if (i == 0) {
                rationale = String.format("Lowest total price at %s %s (Base: %s, Delivery: %s)",
                        offer.getTotalPrice(), offer.getCurrency(), offer.getBasePrice(), offer.getDeliveryFee());
            } else {
                rationale = String.format("+%s %s higher than the cheapest offer",
                        diffFromLowest, offer.getCurrency());
            }

            results.add(OfferScoreResult.builder()
                    .offerId(offer.getId())
                    .offerTitle(offer.getTitle())
                    .sellerName(offer.getSellerName())
                    .score(score)
                    .rank(i + 1)
                    .rationale(rationale)
                    .isWinner(i == 0)
                    .build());
        }

        return results;
    }
}
