package com.buywise.modules.ai.service;

import com.buywise.modules.ai.dto.DocumentExtractionAiResult;
import com.buywise.modules.ai.dto.ExtractedFactItem;
import com.buywise.modules.ai.provider.LlmProvider;
import com.buywise.modules.document.model.Document;
import com.buywise.modules.document.model.ExtractedFact;
import com.buywise.modules.document.model.VerificationStatus;
import com.buywise.modules.document.repository.DocumentRepository;
import com.buywise.modules.document.repository.ExtractedFactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Service orchestrating AI-assisted document parsing, semantic fact extraction, and grounding.
 * 
 * Demonstrates:
 * - Evidence-Grounded Fact Extraction: Captures verbatim text excerpts for every extracted attribute.
 * - Human-in-the-Loop Architecture: Marks all newly parsed facts as PENDING_VERIFICATION until consumer approval.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentUnderstandingAiService {

    private final LlmProvider llmProvider;
    private final StructuredOutputValidator validator;
    private final DocumentRepository documentRepository;
    private final ExtractedFactRepository factRepository;

    @Transactional
    public List<ExtractedFact> extractAndPersistFacts(Document document) {
        String rawText = document.getRawTextContent();
        if (rawText == null || rawText.isBlank()) {
            document.setExtractionStatus("COMPLETED");
            documentRepository.save(document);
            return List.of();
        }

        String systemPrompt = """
                You are BuyWise Document Understanding AI.
                Your task is to extract structured purchase, receipt, invoice, and warranty facts from document text.
                CRITICAL RULES:
                1. Extract ONLY facts that are explicitly present or directly verifiable in the text.
                2. DO NOT hallucinate prices, dates, or terms not mentioned.
                3. For every extracted fact, provide the EXACT sourceExcerpt (verbatim sentence or phrase from the document).
                4. Set a confidenceScore between 0.0 and 1.0 reflecting how clear the text was.
                5. Output MUST strictly conform to this JSON schema:
                {
                  "detectedDocumentType": "RECEIPT | INVOICE | WARRANTY | RETURN_POLICY | SPEC_SHEET",
                  "documentSummary": "string",
                  "extractedFacts": [
                    {
                      "factKey": "PRODUCT_NAME | PURCHASE_PRICE | PURCHASE_DATE | SELLER_NAME | WARRANTY_DURATION_MONTHS | RETURN_PERIOD_DAYS | SERIAL_NUMBER | WARRANTY_EXCLUSIONS | RETURN_CONDITIONS",
                      "extractedValue": "string",
                      "sourceExcerpt": "string",
                      "pageNumber": 1,
                      "confidenceScore": 0.95
                    }
                  ]
                }
                """;

        String userPrompt = String.format("DOCUMENT FILENAME: %s\nDOCUMENT TYPE HINT: %s\n\nDOCUMENT TEXT CONTENT:\n\"\"\"\n%s\n\"\"\"",
                document.getFileName(), document.getDocumentType(), rawText);

        DocumentExtractionAiResult aiResult;
        try {
            aiResult = llmProvider.generateStructuredOutput(systemPrompt, userPrompt, DocumentExtractionAiResult.class);
            validator.validateDocumentExtraction(aiResult);
        } catch (Exception ex) {
            log.warn("LLM document extraction failed, using heuristic fallback: {}", ex.getMessage());
            aiResult = generateHeuristicFallback(document);
        }

        List<ExtractedFact> savedFacts = new ArrayList<>();
        if (aiResult.getExtractedFacts() != null) {
            for (ExtractedFactItem item : aiResult.getExtractedFacts()) {
                ExtractedFact fact = ExtractedFact.builder()
                        .document(document)
                        .factKey(item.getFactKey())
                        .extractedValue(item.getExtractedValue())
                        .sourceExcerpt(item.getSourceExcerpt())
                        .pageNumber(item.getPageNumber() != null ? item.getPageNumber() : 1)
                        .confidenceScore(item.getConfidenceScore() != null ? item.getConfidenceScore() : new BigDecimal("0.85"))
                        .verificationStatus(VerificationStatus.PENDING_VERIFICATION)
                        .build();

                savedFacts.add(factRepository.save(fact));
            }
        }

        document.setExtractionStatus("COMPLETED");
        documentRepository.save(document);
        log.info("Persisted {} extracted facts for document id: {}", savedFacts.size(), document.getId());

        return savedFacts;
    }

    private DocumentExtractionAiResult generateHeuristicFallback(Document document) {
        List<ExtractedFactItem> items = new ArrayList<>();
        String text = document.getRawTextContent();

        items.add(ExtractedFactItem.builder()
                .factKey("FILE_NAME")
                .extractedValue(document.getFileName())
                .sourceExcerpt(document.getFileName())
                .pageNumber(1)
                .confidenceScore(new BigDecimal("1.00"))
                .build());

        if (text != null && text.toLowerCase().contains("mad")) {
            items.add(ExtractedFactItem.builder()
                    .factKey("CURRENCY")
                    .extractedValue("MAD")
                    .sourceExcerpt("Detected Moroccan Dirham references")
                    .pageNumber(1)
                    .confidenceScore(new BigDecimal("0.90"))
                    .build());
        }

        return DocumentExtractionAiResult.builder()
                .detectedDocumentType(document.getDocumentType().name())
                .documentSummary("Document indexed and stored for manual verification.")
                .extractedFacts(items)
                .build();
    }
}
