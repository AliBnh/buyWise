package com.buywise.modules.comparison.strategy;

import com.buywise.modules.comparison.model.Offer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Concrete Strategy evaluating offers based on warranty longevity and protection cost-efficiency.
 * 
 * Strategy Pattern: Implements {@link ComparisonStrategy} focusing on consumer risk protection.
 */
@Component
public class BestWarrantyStrategy implements ComparisonStrategy {

    @Override
    public StrategyType getStrategyType() {
        return StrategyType.BEST_WARRANTY;
    }

    @Override
    public String getDisplayName() {
        return "Best Warranty & Protection";
    }

    @Override
    public String getDescription() {
        return "Prioritizes extended warranty duration and lowest monthly cost per month of verified coverage.";
    }

    @Override
    public List<OfferScoreResult> evaluate(List<Offer> offers) {
        if (offers == null || offers.isEmpty()) {
            return List.of();
        }

        // Sort descending by warranty months, then ascending by monthly cost
        List<Offer> sorted = new ArrayList<>(offers);
        sorted.sort((o1, o2) -> {
            int cmp = Integer.compare(o2.getWarrantyMonths(), o1.getWarrantyMonths());
            if (cmp != 0) return cmp;
            return o1.getTotalPrice().compareTo(o2.getTotalPrice());
        });

        int maxWarranty = sorted.get(0).getWarrantyMonths();
        List<OfferScoreResult> results = new ArrayList<>();

        for (int i = 0; i < sorted.size(); i++) {
            Offer offer = sorted.get(i);
            int months = Math.max(1, offer.getWarrantyMonths());
            BigDecimal costPerMonth = offer.getTotalPrice().divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);

            BigDecimal score = BigDecimal.valueOf(offer.getWarrantyMonths())
                    .divide(BigDecimal.valueOf(Math.max(1, maxWarranty)), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100.00"))
                    .setScale(2, RoundingMode.HALF_UP);

            String rationale = String.format("%d months warranty coverage (%s %s/month covered)",
                    offer.getWarrantyMonths(), costPerMonth, offer.getCurrency());

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
