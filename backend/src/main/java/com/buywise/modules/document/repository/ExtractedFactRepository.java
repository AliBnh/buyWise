package com.buywise.modules.document.repository;

import com.buywise.modules.document.model.ExtractedFact;
import com.buywise.modules.document.model.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExtractedFactRepository extends JpaRepository<ExtractedFact, Long> {

    List<ExtractedFact> findByDocumentId(Long documentId);

    List<ExtractedFact> findByDocumentIdAndVerificationStatus(Long documentId, VerificationStatus status);
}
