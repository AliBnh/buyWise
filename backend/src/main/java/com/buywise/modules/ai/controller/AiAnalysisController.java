package com.buywise.modules.ai.controller;

import com.buywise.modules.ai.dto.OfferAnalysisAiResult;
import com.buywise.modules.ai.service.OfferAnalysisAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller triggering asynchronous or on-demand AI strategic analysis for purchasing comparisons.
 * 
 * Demonstrates:
 * - Separation of Concerns: Delegates mathematical calculations to backend services while utilizing LLM for semantic synthesis.
 * - Anti-Hallucination Grounding: Injects deterministic price differentials and seller terms as unalterable prompt constraints.
 * - High-Availability Fallback: Automatically falls back to deterministic synthesis if LLM inference times out.
 */
@RestController
@RequestMapping("/api/v1/ai/comparisons")
@RequiredArgsConstructor
@Tag(name = "AI Comparison Analysis", description = "Semantic analysis of purchasing offers, specifications, and warranty trade-offs")
public class AiAnalysisController {

    private final OfferAnalysisAiService offerAnalysisAiService;

    /**
     * Executes grounded AI trade-off evaluation across candidate offers, identifying purchasing risks and seller credibility notes.
     * 
     * @param comparisonId Unique identifier of the comparison to analyze.
     * @return HTTP 200 OK with validated structured output (OfferAnalysisAiResult).
     */
    @PostMapping("/{comparisonId}/analyze")
    @Operation(summary = "Run AI semantic offer analysis on comparison offers")
    public ResponseEntity<OfferAnalysisAiResult> analyzeComparison(@PathVariable Long comparisonId) {
        return ResponseEntity.ok(offerAnalysisAiService.analyzeComparison(comparisonId));
    }
}
