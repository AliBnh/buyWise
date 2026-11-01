package com.buywise.modules.document.repository;

import com.buywise.modules.document.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Document> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT d FROM Document d LEFT JOIN FETCH d.extractedFacts WHERE d.id = :id AND d.userId = :userId")
    Optional<Document> findByIdAndUserIdWithFacts(@Param("id") Long id, @Param("userId") Long userId);

    List<Document> findByPurchaseIdAndUserId(Long purchaseId, Long userId);

    List<Document> findByComparisonIdAndUserId(Long comparisonId, Long userId);
}
