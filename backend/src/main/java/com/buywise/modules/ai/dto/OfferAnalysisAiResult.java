package com.buywise.modules.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Structured, validated output contract for semantic offer analysis.
 * 
 * Demonstrates:
 * - Anti-Hallucination Guardrails: Separates verified differences from missing information and risk alerts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferAnalysisAiResult {

    private String executiveSummary;
    private Long recommendedOfferId;
    private String recommendedOfferSeller;
    private String recommendationRationale;
    private List<String> keySpecificationDifferences;
    private List<OfferTradeoffAnalysis> offerEvaluations;
    private List<String> missingInformationAlerts;
    private List<String> purchasingRisks;
    private BigDecimal confidenceScore;
}
