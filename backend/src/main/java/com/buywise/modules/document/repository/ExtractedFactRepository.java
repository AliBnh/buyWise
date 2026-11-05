package com.buywise.modules.document.repository;

import com.buywise.modules.document.model.ExtractedFact;
import com.buywise.modules.document.model.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Persistence repository for individual semantic facts extracted by AI from uploaded documents.
 * 
 * Demonstrates:
 * - Granular Status Filtering: Supports querying facts by verification lifecycle state
 *   (PENDING_VERIFICATION, VERIFIED, REJECTED, MODIFIED).
 */
@Repository
public interface ExtractedFactRepository extends JpaRepository<ExtractedFact, Long> {

    /**
     * Retrieves all facts extracted from a parent document.
     */
    List<ExtractedFact> findByDocumentId(Long documentId);

    /**
     * Filters facts for a specific document by Human-in-the-Loop review status.
     */
    List<ExtractedFact> findByDocumentIdAndVerificationStatus(Long documentId, VerificationStatus status);
}
