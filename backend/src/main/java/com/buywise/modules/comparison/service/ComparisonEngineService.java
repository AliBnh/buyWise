package com.buywise.modules.comparison.service;

import com.buywise.common.metrics.BuyWiseMetricsService;
import com.buywise.modules.comparison.dto.DeterministicComparisonSummaryDto;
import com.buywise.modules.comparison.dto.DeterministicOfferSummary;
import com.buywise.modules.comparison.model.Offer;
import com.buywise.modules.comparison.strategy.ComparisonStrategyFactory;
import com.buywise.modules.comparison.strategy.OfferScoreResult;
import com.buywise.modules.comparison.strategy.StrategyType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * ==============================================================================================
 * ARCHITECTURAL DESIGN NOTE: DETERMINISTIC ARITHMETIC INVARIANT & AI SEPARATION OF CONCERNS
 * ==============================================================================================
 * 
 * Architectural Invariants & Engineering Rationale:
 * 
 * 1. THE NON-NEGOTIABLE AI ARCHITECTURAL BOUNDARY:
 *    - Rule: An LLM must NEVER be responsible for arithmetic that can be computed deterministically!
 *    - LLMs are probabilistic token predictors; they are notorious for hallucinating addition,
 *      miscalculating percentage differences, and misstating currency amounts.
 *    - In BuyWise, ALL cost calculations, savings percentages, price deltas, and strategy scores
 *      are computed purely in Java by this engine. The LLM only receives verified mathematical
 *      ground-truth as immutable context for qualitative textual summarization.
 *
 * 2. SINGLE RESPONSIBILITY PRINCIPLE (SRP):
 *    - This class does exactly one thing: deterministic mathematical comparison and specification matrix
 *      synthesis across multiple seller offers. It performs zero database persistence and zero network I/O.
 *
 * 3. ARITHMETIC PRECISION (BigDecimal / RoundingMode.HALF_UP):
 *    - Explicitly sets scale to 2 decimal places for currency, 4 decimal places for intermediate ratios.
 *    - Eliminates IEEE 754 floating-point inaccuracies and division errors.
 *
 * 4. OBSERVABILITY INSTRUMENTATION:
 *    - Emits `buywise_deterministic_calculations_total{strategy="..."}` counter metrics to Prometheus
 *      for real-time Grafana dashboard analytics.
 *
 * Architectural Rationale & Trade-offs:
 * Q: "Why calculate comparison metrics deterministically instead of asking GPT-4 or Groq to do it?"
 * A: LLMs lack an ALU (Arithmetic Logic Unit). They predict next tokens based on probability distributions,
 *    meaning 199.99 + 49.99 can easily become 259.98 or 249.98. In e-commerce and fintech, inaccurate
 *    totals destroy consumer trust and breach legal compliance. Pure Java with BigDecimal provides
 *    deterministic, reproducible, and verifiable arithmetic with 100% mathematical certainty.
 * ==============================================================================================
 */
@Slf4j
@Service
public class ComparisonEngineService {

    private final ComparisonStrategyFactory strategyFactory;
    private final ObjectMapper objectMapper;
    private final BuyWiseMetricsService metricsService;

    public ComparisonEngineService(ComparisonStrategyFactory strategyFactory, ObjectMapper objectMapper) {
        this(strategyFactory, objectMapper, null);
    }

    @Autowired
    public ComparisonEngineService(
            ComparisonStrategyFactory strategyFactory,
            ObjectMapper objectMapper,
            @Autowired(required = false) BuyWiseMetricsService metricsService) {
        this.strategyFactory = strategyFactory;
        this.objectMapper = objectMapper;
        this.metricsService = metricsService;
    }

    public DeterministicComparisonSummaryDto calculateSummary(List<Offer> offers) {
        if (offers == null || offers.isEmpty()) {
            return DeterministicComparisonSummaryDto.builder()
                    .offerCount(0)
                    .currency("MAD")
                    .offerSummaries(List.of())
                    .rankingsByStrategy(Map.of())
                    .specificationComparisonMatrix(Map.of())
                    .build();
        }

        // Ensure all total prices are deterministically recomputed
        offers.forEach(Offer::recalculateTotalPrice);

        // Sort ascending to find lowest price
        List<Offer> priceSorted = new ArrayList<>(offers);
        priceSorted.sort(Comparator.comparing(Offer::getTotalPrice));

        Offer lowestPriceOffer = priceSorted.get(0);
        Offer highestPriceOffer = priceSorted.get(priceSorted.size() - 1);
        BigDecimal minPrice = lowestPriceOffer.getTotalPrice();
        BigDecimal maxPrice = highestPriceOffer.getTotalPrice();
        BigDecimal absDiff = maxPrice.subtract(minPrice);

        BigDecimal pctDiff = BigDecimal.ZERO;
        if (minPrice.compareTo(BigDecimal.ZERO) > 0) {
            pctDiff = absDiff.divide(minPrice, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100.00"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // Warranty metrics
        int maxWarranty = offers.stream().mapToInt(Offer::getWarrantyMonths).max().orElse(0);
        int minWarranty = offers.stream().mapToInt(Offer::getWarrantyMonths).min().orElse(0);
        int warrantyDiff = maxWarranty - minWarranty;
        Offer bestWarrantyOffer = offers.stream()
                .filter(o -> o.getWarrantyMonths() == maxWarranty)
                .findFirst()
                .orElse(lowestPriceOffer);

        // Individual offer summaries
        List<DeterministicOfferSummary> summaries = new ArrayList<>();
        for (Offer offer : offers) {
            BigDecimal diffFromLowest = offer.getTotalPrice().subtract(minPrice);
            BigDecimal pctFromLowest = BigDecimal.ZERO;
            if (minPrice.compareTo(BigDecimal.ZERO) > 0) {
                pctFromLowest = diffFromLowest.divide(minPrice, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100.00"))
                        .setScale(2, RoundingMode.HALF_UP);
            }

            int months = Math.max(1, offer.getWarrantyMonths());
            BigDecimal costPerWarrantyMonth = offer.getTotalPrice().divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);

            summaries.add(DeterministicOfferSummary.builder()
                    .offerId(offer.getId())
                    .sellerName(offer.getSellerName())
                    .title(offer.getTitle())
                    .productModel(offer.getProductModel())
                    .currency(offer.getCurrency())
                    .basePrice(offer.getBasePrice())
                    .deliveryFee(offer.getDeliveryFee())
                    .additionalFees(offer.getAdditionalFees())
                    .discountAmount(offer.getDiscountAmount())
                    .totalPrice(offer.getTotalPrice())
                    .warrantyMonths(offer.getWarrantyMonths())
                    .returnWindowDays(offer.getReturnWindowDays())
                    .monthlyCostPerWarrantyMonth(costPerWarrantyMonth)
                    .priceDifferenceFromLowest(diffFromLowest)
                    .percentageDifferenceFromLowest(pctFromLowest)
                    .build());
        }

        // Strategy rankings
        Map<StrategyType, List<OfferScoreResult>> rankings = new EnumMap<>(StrategyType.class);
        for (StrategyType type : StrategyType.values()) {
            rankings.put(type, strategyFactory.getStrategy(type).evaluate(offers));
            if (metricsService != null) {
                metricsService.recordDeterministicCalculation(type.name());
            }
        }

        // Build Spec Matrix
        Map<String, Map<Long, String>> specMatrix = extractSpecificationMatrix(offers);

        return DeterministicComparisonSummaryDto.builder()
                .offerCount(offers.size())
                .currency(lowestPriceOffer.getCurrency())
                .cheapestOfferId(lowestPriceOffer.getId())
                .cheapestOfferSeller(lowestPriceOffer.getSellerName())
                .lowestTotalPrice(minPrice)
                .highestPriceOfferId(highestPriceOffer.getId())
                .highestPriceOfferSeller(highestPriceOffer.getSellerName())
                .highestTotalPrice(maxPrice)
                .absolutePriceDifference(absDiff)
                .percentagePriceDifference(pctDiff)
                .bestWarrantyOfferId(bestWarrantyOffer.getId())
                .longestWarrantyMonths(maxWarranty)
                .shortestWarrantyMonths(minWarranty)
                .warrantyDifferenceMonths(warrantyDiff)
                .offerSummaries(summaries)
                .rankingsByStrategy(rankings)
                .specificationComparisonMatrix(specMatrix)
                .build();
    }

    private Map<String, Map<Long, String>> extractSpecificationMatrix(List<Offer> offers) {
        Map<String, Map<Long, String>> matrix = new TreeMap<>();
        for (Offer offer : offers) {
            if (offer.getSpecificationsJson() == null || offer.getSpecificationsJson().isBlank()) {
                continue;
            }
            try {
                Map<String, Object> specs = objectMapper.readValue(
                        offer.getSpecificationsJson(),
                        new TypeReference<>() {}
                );
                for (Map.Entry<String, Object> entry : specs.entrySet()) {
                    String specKey = entry.getKey();
                    String specVal = entry.getValue() != null ? entry.getValue().toString() : "-";
                    matrix.computeIfAbsent(specKey, k -> new HashMap<>()).put(offer.getId(), specVal);
                }
            } catch (Exception ex) {
                log.warn("Failed to parse specifications JSON for offer {}: {}", offer.getId(), ex.getMessage());
            }
        }
        return matrix;
    }
}
