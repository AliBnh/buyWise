package com.buywise.modules.document.dto;

import com.buywise.modules.document.model.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractedFactDto {

    private Long id;
    private Long documentId;
    private String factKey;
    private String extractedValue;
    private String verifiedValue;
    private String sourceExcerpt;
    private Integer pageNumber;
    private BigDecimal confidenceScore;
    private VerificationStatus verificationStatus;
    private Instant createdAt;
}
