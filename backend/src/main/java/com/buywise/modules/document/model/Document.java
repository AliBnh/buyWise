package com.buywise.modules.document.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain entity representing an uploaded consumer document (receipt, invoice, warranty deed).
 * 
 * Demonstrates:
 * - Aggregate Root: Coordinates child {@link ExtractedFact} items.
 * - Source Grounding: Stores raw text content and references original storage paths for full auditability.
 */
@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "purchase_id")
    private Long purchaseId;

    @Column(name = "comparison_id")
    private Long comparisonId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "stored_file_path", nullable = false, length = 500)
    private String storedFilePath;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private DocumentType documentType;

    @Column(name = "raw_text_content", columnDefinition = "TEXT")
    private String rawTextContent;

    @Column(name = "extraction_status", nullable = false, length = 50)
    @Builder.Default
    private String extractionStatus = "PENDING"; // PENDING, COMPLETED, FAILED

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ExtractedFact> extractedFacts = new ArrayList<>();

    public void addExtractedFact(ExtractedFact fact) {
        extractedFacts.add(fact);
        fact.setDocument(this);
    }
}
