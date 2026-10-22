package com.buywise.modules.document.model;

import com.buywise.common.audit.AuditableEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Child entity representing a discrete fact parsed from an uploaded document.
 * 
 * Demonstrates:
 * - Traceable AI Provenance: Stores exact source excerpt and confidence rating.
 * - Human-in-the-Loop Verification: Distinguishes extracted values from user-verified values.
 */
@Entity
@Table(name = "extracted_facts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtractedFact extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    @JsonIgnore
    private Document document;

    @Column(name = "fact_key", nullable = false, length = 100)
    private String factKey;

    @Column(name = "extracted_value", nullable = false, columnDefinition = "TEXT")
    private String extractedValue;

    @Column(name = "verified_value", columnDefinition = "TEXT")
    private String verifiedValue;

    @Column(name = "source_excerpt", columnDefinition = "TEXT")
    private String sourceExcerpt;

    @Column(name = "page_number")
    @Builder.Default
    private Integer pageNumber = 1;

    @Column(name = "confidence_score", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal confidenceScore = new BigDecimal("0.85");

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 50)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING_VERIFICATION;
}
