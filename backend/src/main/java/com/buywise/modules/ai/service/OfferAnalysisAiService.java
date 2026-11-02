package com.buywise.modules.ai.service;

import com.buywise.common.exception.ResourceNotFoundException;
import com.buywise.common.security.SecurityUtils;
import com.buywise.modules.ai.dto.OfferAnalysisAiResult;
import com.buywise.modules.ai.provider.LlmProvider;
import com.buywise.modules.comparison.dto.DeterministicComparisonSummaryDto;
import com.buywise.modules.comparison.model.Comparison;
import com.buywise.modules.comparison.model.ComparisonStatus;
import com.buywise.modules.comparison.model.Offer;
import com.buywise.modules.comparison.repository.ComparisonRepository;
import com.buywise.modules.comparison.service.ComparisonEngineService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * =================================================================================================
 * ARCHITECTURAL DESIGN NOTE: AI ORCHESTRATION & ANTI-HALLUCINATION GUARDRAILS
 * =================================================================================================
 * Architectural Invariants:
 * 1. Strict Boundary Separation (AI vs. Math Engine):
 *    - LLMs are probabilistic language models prone to calculation errors, hallucinations, and floating-point drift.
 *    - Therefore, the LLM NEVER performs arithmetic. Total prices, price deltas, value scores, and rankings
 *      are calculated deterministically by {@link ComparisonEngineService} using BigDecimal.
 *    - The deterministic calculations are injected into the prompt as IMMUTABLE GROUND TRUTH. The LLM is
 *      constrained strictly to qualitative, semantic synthesis (trade-offs, hidden warranty clauses, return risks).
 *
 * 2. Grounded Prompt Engineering & Negative Constraints:
 *    - Explicit system instructions forbid inventing prices, specs, or merchant reputation.
 *    - Requires structured JSON schema adherence, validated through {@link StructuredOutputValidator}.
 *
 * 3. Multi-Tenant Data Isolation (OWASP Top 10 API Security):
 *    - Insecure Direct Object Reference (IDOR) prevention:
 *      {@code comparisonRepository.findByIdAndUserIdWithOffers(comparisonId, userId)} guarantees that a user
 *      can ONLY trigger AI analysis on comparisons they own.
 *
 * Architectural Trade-off Analysis & FAQ:
 * Q: "How do you prevent LLMs from hallucinating incorrect prices or bogus warranty advice in production?"
 * A: We employ a 3-layer defensive architecture:
 *    1. Grounding Layer: The prompt includes only verified DB entities and pre-computed BigDecimal values.
 *    2. Boundary Layer: Negative constraints in the system prompt explicitly penalize speculative math.
 *    3. Validation Layer: Output is parsed through a strict JSON schema and sanitized by StructuredOutputValidator,
 *       discarding any hallucinated fields or non-numeric IDs.
 * =================================================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OfferAnalysisAiService {

    private final ComparisonRepository comparisonRepository;
    private final ComparisonEngineService comparisonEngineService;
    private final LlmProvider llmProvider;
    private final StructuredOutputValidator validator;
    private final ObjectMapper objectMapper;
    private final com.buywise.common.metrics.BuyWiseMetricsService metricsService;

    @Transactional
    public OfferAnalysisAiResult analyzeComparison(Long comparisonId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Comparison comparison = comparisonRepository.findByIdAndUserIdWithOffers(comparisonId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Comparison", "id", comparisonId));

        List<Offer> offers = comparison.getOffers();
        if (offers == null || offers.isEmpty()) {
            return OfferAnalysisAiResult.builder()
                    .executiveSummary("No offers currently added to this comparison.")
                    .keySpecificationDifferences(List.of())
                    .offerEvaluations(List.of())
                    .missingInformationAlerts(List.of("Please add at least one offer to compare."))
                    .purchasingRisks(List.of())
                    .build();
        }

        DeterministicComparisonSummaryDto mathSummary = comparisonEngineService.calculateSummary(offers);

        String systemPrompt = """
                You are BuyWise Purchase Intelligence AI.
                Your task is to analyze competing purchasing offers for ordinary consumers.
                RULES:
                1. DO NOT invent prices, specifications, seller names, or warranty terms. Use ONLY the provided data.
                2. Arithmetic (total prices, price differences, rankings) has ALREADY been calculated deterministically by the backend. DO NOT recalculate or contradict these numbers.
                3. Explain genuine trade-offs instead of simply declaring the cheapest offer the winner.
                4. Highlight specification differences (e.g., storage, RAM, warranty duration, delivery costs).
                5. Explicitly identify missing information and risks the consumer must verify before purchasing.
                6. Output MUST strictly follow the JSON schema:
                {
                  "executiveSummary": "string",
                  "recommendedOfferId": number,
                  "recommendedOfferSeller": "string",
                  "recommendationRationale": "string",
                  "keySpecificationDifferences": ["string"],
                  "offerEvaluations": [
                    {
                      "offerId": number,
                      "sellerName": "string",
                      "strengths": ["string"],
                      "weaknesses": ["string"],
                      "warrantyAssessment": "string",
                      "returnPolicyAssessment": "string",
                      "sellerCredibilityNotes": ["string"],
                      "itemsToVerifyBeforeBuying": ["string"]
                    }
                  ],
                  "missingInformationAlerts": ["string"],
                  "purchasingRisks": ["string"],
                  "confidenceScore": 0.95
                }
                """;

        String userPrompt = buildAnalysisPrompt(comparison, offers, mathSummary);

        log.info("Requesting LLM offer analysis for comparison id: {}", comparisonId);
        long startTime = System.currentTimeMillis();
        OfferAnalysisAiResult result;
        try {
            result = llmProvider.generateStructuredOutput(systemPrompt, userPrompt, OfferAnalysisAiResult.class);
            validator.validateOfferAnalysis(result);
            metricsService.recordAiRequest("offer_analysis", "success", System.currentTimeMillis() - startTime);
            metricsService.incrementComparisonAnalyzed();
        } catch (Exception ex) {
            log.warn("LLM offer analysis failed, generating grounded deterministic fallback: {}", ex.getMessage());
            metricsService.recordAiRequest("offer_analysis", "fallback", System.currentTimeMillis() - startTime);
            metricsService.incrementComparisonAnalyzed();
            result = generateGroundedFallback(comparison, offers, mathSummary);
        }

        // Persist AI analysis in comparison entity
        try {
            comparison.setAiAnalysisJson(objectMapper.writeValueAsString(result));
            comparison.setAiAnalyzedAt(Instant.now());
            comparison.setStatus(ComparisonStatus.ANALYZED);
            comparisonRepository.save(comparison);
        } catch (Exception e) {
            log.error("Failed to persist AI analysis JSON", e);
        }

        return result;
    }

    private String buildAnalysisPrompt(Comparison comparison, List<Offer> offers, DeterministicComparisonSummaryDto math) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("PRODUCT CATEGORY: %s\nCOMPARISON TITLE: %s\n\n", comparison.getCategoryName(), comparison.getTitle()));
        sb.append(String.format("DETERMINISTIC SUMMARY:\n- Cheapest Offer: %s (%s %s)\n- Highest Offer: %s (%s %s)\n- Absolute Price Difference: %s %s (%s%%)\n- Warranty Difference: %d months\n\n",
                math.getCheapestOfferSeller(), math.getLowestTotalPrice(), math.getCurrency(),
                math.getHighestPriceOfferSeller(), math.getHighestTotalPrice(), math.getCurrency(),
                math.getAbsolutePriceDifference(), math.getCurrency(), math.getPercentagePriceDifference(),
                math.getWarrantyDifferenceMonths()));

        sb.append("OFFERS TO ANALYZE:\n");
        for (Offer o : offers) {
            sb.append(String.format("- Offer ID: %d | Seller: %s | Title: %s | Model: %s\n",
                    o.getId(), o.getSellerName(), o.getTitle(), o.getProductModel() != null ? o.getProductModel() : "N/A"));
            sb.append(String.format("  Base Price: %s %s | Delivery: %s | Net Total: %s %s\n",
                    o.getBasePrice(), o.getCurrency(), o.getDeliveryFee(), o.getTotalPrice(), o.getCurrency()));
            sb.append(String.format("  Warranty: %d months | Return Window: %d days\n",
                    o.getWarrantyMonths(), o.getReturnWindowDays()));
            if (o.getSpecificationsJson() != null && !o.getSpecificationsJson().isBlank()) {
                sb.append(String.format("  Specifications: %s\n", o.getSpecificationsJson()));
            }
            if (o.getWarrantyConditions() != null) {
                sb.append(String.format("  Warranty Conditions: %s\n", o.getWarrantyConditions()));
            }
            if (o.getReturnPolicy() != null) {
                sb.append(String.format("  Return Policy: %s\n", o.getReturnPolicy()));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private OfferAnalysisAiResult generateGroundedFallback(Comparison comparison, List<Offer> offers, DeterministicComparisonSummaryDto math) {
        return OfferAnalysisAiResult.builder()
                .executiveSummary(String.format("Comparison of %d offers for %s. Total prices range from %s %s to %s %s, reflecting a %s%% spread.",
                        offers.size(), comparison.getTitle(), math.getLowestTotalPrice(), math.getCurrency(),
                        math.getHighestTotalPrice(), math.getCurrency(), math.getPercentagePriceDifference()))
                .recommendedOfferId(math.getCheapestOfferId())
                .recommendedOfferSeller(math.getCheapestOfferSeller())
                .recommendationRationale(String.format("Cheapest option delivered at %s %s with %d months warranty.",
                        math.getLowestTotalPrice(), math.getCurrency(), offers.get(0).getWarrantyMonths()))
                .keySpecificationDifferences(List.of(
                        String.format("Price difference of %s %s between competing offers", math.getAbsolutePriceDifference(), math.getCurrency()),
                        String.format("Warranty spread of %d months across sellers", math.getWarrantyDifferenceMonths())
                ))
                .offerEvaluations(List.of())
                .missingInformationAlerts(List.of("Verify official distributor invoice and return policy window prior to payment."))
                .purchasingRisks(List.of("Examine whether seller warranty is supported directly by authorized service centers in Morocco."))
                .build();
    }
}
