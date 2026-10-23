package com.buywise.modules.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Structured output contract for document understanding.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentExtractionAiResult {

    private String detectedDocumentType;
    private String documentSummary;
    private List<ExtractedFactItem> extractedFacts;
}
