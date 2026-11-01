package com.buywise.modules.comparison.strategy;

import com.buywise.modules.comparison.model.Offer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Concrete Strategy calculating a multi-factor value index.
 * 
 * Strategy Pattern: Implements {@link ComparisonStrategy} balancing price, warranty, return window, and fees.
 */
@Component
public class BestValueScoreStrategy implements ComparisonStrategy {

    @Override
    public StrategyType getStrategyType() {
        return StrategyType.BEST_VALUE;
    }

    @Override
    public String getDisplayName() {
        return "Best Overall Value";
    }

    @Override
    public String getDescription() {
        return "Synthesizes price competitiveness (45%), warranty duration (35%), return policy (10%), and delivery terms (10%).";
    }

    @Override
    public List<OfferScoreResult> evaluate(List<Offer> offers) {
        if (offers == null || offers.isEmpty()) {
            return List.of();
        }

        BigDecimal minPrice = offers.stream().map(Offer::getTotalPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ONE);
        int maxWarranty = offers.stream().mapToInt(Offer::getWarrantyMonths).max().orElse(12);
        int maxReturn = offers.stream().mapToInt(Offer::getReturnWindowDays).max().orElse(14);

        List<OfferScoreResult> rawResults = new ArrayList<>();

        for (Offer offer : offers) {
            // 1. Price Score: max 45 points (minPrice / currentPrice * 45)
            BigDecimal priceScore = minPrice.divide(offer.getTotalPrice().max(BigDecimal.ONE), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("45.00"));

            // 2. Warranty Score: max 35 points (warranty / maxWarranty * 35)
            BigDecimal warrantyScore = BigDecimal.valueOf(offer.getWarrantyMonths())
                    .divide(BigDecimal.valueOf(Math.max(1, maxWarranty)), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("35.00"));

            // 3. Return Window Score: max 10 points (returnWindow / maxReturn * 10)
            BigDecimal returnScore = BigDecimal.valueOf(offer.getReturnWindowDays())
                    .divide(BigDecimal.valueOf(Math.max(1, maxReturn)), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("10.00"));

            // 4. Delivery Score: max 10 points (free delivery = 10, else scaled)
            BigDecimal deliveryScore = (offer.getDeliveryFee().compareTo(BigDecimal.ZERO) == 0)
                    ? new BigDecimal("10.00")
                    : new BigDecimal("5.00");

            BigDecimal totalScore = priceScore.add(warrantyScore).add(returnScore).add(deliveryScore)
                    .setScale(2, RoundingMode.HALF_UP);

            String rationale = String.format("Balanced value score of %s/100 (Price: %s/45, Warranty: %s/35, Policy: %s/20)",
                    totalScore,
                    priceScore.setScale(1, RoundingMode.HALF_UP),
                    warrantyScore.setScale(1, RoundingMode.HALF_UP),
                    returnScore.add(deliveryScore).setScale(1, RoundingMode.HALF_UP));

            rawResults.add(OfferScoreResult.builder()
                    .offerId(offer.getId())
                    .offerTitle(offer.getTitle())
                    .sellerName(offer.getSellerName())
                    .score(totalScore)
                    .rationale(rationale)
                    .build());
        }

        // Sort descending by score
        rawResults.sort(Comparator.comparing(OfferScoreResult::getScore).reversed());

        for (int i = 0; i < rawResults.size(); i++) {
            OfferScoreResult res = rawResults.get(i);
            res.setRank(i + 1);
            res.setWinner(i == 0);
        }

        return rawResults;
    }
}
