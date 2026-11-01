package com.buywise.modules.document.dto;

import com.buywise.modules.document.model.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDetailResponse {

    private Long id;
    private Long userId;
    private Long purchaseId;
    private Long comparisonId;
    private String fileName;
    private String contentType;
    private Long fileSizeBytes;
    private DocumentType documentType;
    private String rawTextContent;
    private String extractionStatus;
    private Instant createdAt;
    private List<ExtractedFactDto> extractedFacts;
}
