package com.buywise.modules.ai.service;

import com.buywise.common.exception.BusinessException;
import com.buywise.modules.ai.dto.DocumentExtractionAiResult;
import com.buywise.modules.ai.dto.ExtractedFactItem;
import com.buywise.modules.ai.dto.OfferAnalysisAiResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Structured Output Validator — AI Guardrail Verification")
class StructuredOutputValidatorTest {

    private StructuredOutputValidator validator;

    @BeforeEach
    void setUp() {
        validator = new StructuredOutputValidator();
    }

    @Test
    @DisplayName("Should accept valid offer analysis with executive summary and confidence score")
    void testValidOfferAnalysis() {
        OfferAnalysisAiResult result = OfferAnalysisAiResult.builder()
                .executiveSummary("Offer B provides better storage and warranty value.")
                .confidenceScore(new BigDecimal("0.95"))
                .offerEvaluations(List.of())
                .build();

        assertDoesNotThrow(() -> validator.validateOfferAnalysis(result));
    }

    @Test
    @DisplayName("Should throw BusinessException when executive summary is null or empty")
    void testOfferAnalysisMissingSummary() {
        OfferAnalysisAiResult result = OfferAnalysisAiResult.builder()
                .executiveSummary("")
                .confidenceScore(new BigDecimal("0.95"))
                .build();

        assertThrows(BusinessException.class, () -> validator.validateOfferAnalysis(result));
    }

    @Test
    @DisplayName("Should normalize out-of-range confidence scores to standard default")
    void testNormalizeConfidenceScore() {
        OfferAnalysisAiResult result = OfferAnalysisAiResult.builder()
                .executiveSummary("Valid analysis summary.")
                .confidenceScore(new BigDecimal("1.50")) // Exceeds 1.0
                .build();

        validator.validateOfferAnalysis(result);
        assertEquals(new BigDecimal("0.85"), result.getConfidenceScore());
    }

    @Test
    @DisplayName("Should repair missing fact keys in document extraction results")
    void testRepairMissingFactKeys() {
        List<ExtractedFactItem> items = new ArrayList<>();
        items.add(ExtractedFactItem.builder()
                .factKey(null)
                .extractedValue("Some note")
                .build());

        DocumentExtractionAiResult result = DocumentExtractionAiResult.builder()
                .detectedDocumentType("RECEIPT")
                .extractedFacts(items)
                .build();

        validator.validateDocumentExtraction(result);
        assertEquals("GENERAL_NOTE", items.get(0).getFactKey());
        assertEquals(new BigDecimal("0.80"), items.get(0).getConfidenceScore());
    }
}
