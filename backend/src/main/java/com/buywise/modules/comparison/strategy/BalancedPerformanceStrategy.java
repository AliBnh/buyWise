package com.buywise.modules.comparison.strategy;

import com.buywise.modules.comparison.model.Offer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Concrete Strategy calculating a balanced performance and total cost-of-ownership rating.
 * 
 * Strategy Pattern: Implements {@link ComparisonStrategy} focusing on multi-year ownership equilibrium.
 */
@Component
public class BalancedPerformanceStrategy implements ComparisonStrategy {

    @Override
    public StrategyType getStrategyType() {
        return StrategyType.BALANCED;
    }

    @Override
    public String getDisplayName() {
        return "Balanced Total Ownership";
    }

    @Override
    public String getDescription() {
        return "Balances up-front investment, warranty peace-of-mind, and seller flexibility over a 24-month horizon.";
    }

    @Override
    public List<OfferScoreResult> evaluate(List<Offer> offers) {
        if (offers == null || offers.isEmpty()) {
            return List.of();
        }

        BigDecimal minPrice = offers.stream().map(Offer::getTotalPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ONE);
        int maxWarranty = offers.stream().mapToInt(Offer::getWarrantyMonths).max().orElse(12);

        List<OfferScoreResult> raw = new ArrayList<>();

        for (Offer offer : offers) {
            BigDecimal priceComp = minPrice.divide(offer.getTotalPrice().max(BigDecimal.ONE), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("50.00"));

            BigDecimal warrantyComp = BigDecimal.valueOf(offer.getWarrantyMonths())
                    .divide(BigDecimal.valueOf(Math.max(1, maxWarranty)), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("50.00"));

            BigDecimal totalScore = priceComp.add(warrantyComp).setScale(2, RoundingMode.HALF_UP);

            String rationale = String.format("Balanced 2-year ownership score: %s/100 (Cost efficiency: %s/50, Protection: %s/50)",
                    totalScore, priceComp.setScale(1, RoundingMode.HALF_UP), warrantyComp.setScale(1, RoundingMode.HALF_UP));

            raw.add(OfferScoreResult.builder()
                    .offerId(offer.getId())
                    .offerTitle(offer.getTitle())
                    .sellerName(offer.getSellerName())
                    .score(totalScore)
                    .rationale(rationale)
                    .build());
        }

        raw.sort(Comparator.comparing(OfferScoreResult::getScore).reversed());

        for (int i = 0; i < raw.size(); i++) {
            OfferScoreResult r = raw.get(i);
            r.setRank(i + 1);
            r.setWinner(i == 0);
        }

        return raw;
    }
}
