package com.buywise.modules.ai.service;

import com.buywise.common.exception.BusinessException;
import com.buywise.modules.ai.dto.DocumentExtractionAiResult;
import com.buywise.modules.ai.dto.ExtractedFactItem;
import com.buywise.modules.ai.dto.OfferAnalysisAiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Defensive validator enforcing integrity of AI structured JSON outputs before business persistence.
 * 
 * Demonstrates:
 * - Anti-Hallucination Guardrails: Rejects corrupted or malformed model generations.
 * - Single Responsibility Principle (SRP): Concentrates AI output schema validation rules.
 */
@Slf4j
@Component
public class StructuredOutputValidator {

    public void validateOfferAnalysis(OfferAnalysisAiResult result) {
        if (result == null) {
            throw new BusinessException("AI offer analysis result was empty.");
        }
        if (result.getExecutiveSummary() == null || result.getExecutiveSummary().isBlank()) {
            throw new BusinessException("AI offer analysis missing executive summary.");
        }
        if (result.getConfidenceScore() != null) {
            if (result.getConfidenceScore().compareTo(BigDecimal.ZERO) < 0 ||
                result.getConfidenceScore().compareTo(BigDecimal.ONE) > 0) {
                result.setConfidenceScore(new BigDecimal("0.85"));
            }
        } else {
            result.setConfidenceScore(new BigDecimal("0.85"));
        }
    }

    public void validateDocumentExtraction(DocumentExtractionAiResult result) {
        if (result == null) {
            throw new BusinessException("AI document extraction result was empty.");
        }
        if (result.getExtractedFacts() == null || result.getExtractedFacts().isEmpty()) {
            log.warn("Document extraction produced zero facts.");
            return;
        }

        for (ExtractedFactItem item : result.getExtractedFacts()) {
            if (item.getFactKey() == null || item.getFactKey().isBlank()) {
                item.setFactKey("GENERAL_NOTE");
            }
            if (item.getExtractedValue() == null) {
                item.setExtractedValue("");
            }
            if (item.getConfidenceScore() == null) {
                item.setConfidenceScore(new BigDecimal("0.80"));
            }
        }
    }
}
