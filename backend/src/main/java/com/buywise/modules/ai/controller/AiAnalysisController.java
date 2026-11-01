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

@RestController
@RequestMapping("/api/v1/ai/comparisons")
@RequiredArgsConstructor
@Tag(name = "AI Comparison Analysis", description = "Semantic analysis of purchasing offers, specifications, and warranty trade-offs")
public class AiAnalysisController {

    private final OfferAnalysisAiService offerAnalysisAiService;

    @PostMapping("/{comparisonId}/analyze")
    @Operation(summary = "Run AI semantic offer analysis on comparison offers")
    public ResponseEntity<OfferAnalysisAiResult> analyzeComparison(@PathVariable Long comparisonId) {
        return ResponseEntity.ok(offerAnalysisAiService.analyzeComparison(comparisonId));
    }
}
