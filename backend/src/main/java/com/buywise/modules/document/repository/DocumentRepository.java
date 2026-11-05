package com.buywise.modules.document.repository;

import com.buywise.modules.document.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Persistence repository for uploaded consumer documents (receipts, warranties, invoices).
 * 
 * Demonstrates:
 * - N+1 Query Elimination: Uses LEFT JOIN FETCH to pull the document metadata and its associated
 *   extracted facts in a single database roundtrip.
 * - Multi-Tenant Authorization: Enforces strict scoping by userId on all access methods.
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    /**
     * Lists documents uploaded by the user, ordered newest first.
     */
    List<Document> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Finds document while asserting tenant ownership.
     */
    Optional<Document> findByIdAndUserId(Long id, Long userId);

    /**
     * Eagerly fetches a document and all child ExtractedFact rows in a single query.
     */
    @Query("SELECT d FROM Document d LEFT JOIN FETCH d.extractedFacts WHERE d.id = :id AND d.userId = :userId")
    Optional<Document> findByIdAndUserIdWithFacts(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * Finds documents linked to a specific purchased asset.
     */
    List<Document> findByPurchaseIdAndUserId(Long purchaseId, Long userId);

    /**
     * Finds documents linked to a specific comparison workspace.
     */
    List<Document> findByComparisonIdAndUserId(Long comparisonId, Long userId);
}
