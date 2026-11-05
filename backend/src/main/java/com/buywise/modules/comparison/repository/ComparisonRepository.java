package com.buywise.modules.comparison.repository;

import com.buywise.modules.comparison.model.Comparison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Persistence repository for purchasing comparisons with multi-tenant isolation and fetch optimizations.
 * 
 * Demonstrates:
 * - Elimination of N+1 Hibernate Select Problem: Uses JPQL LEFT JOIN FETCH to load the comparison
 *   and its collection of offers in a single atomic SQL query.
 * - Insecure Direct Object Reference (IDOR) Prevention: Couples entity ID with authenticated userId
 *   at the SQL WHERE clause level, ensuring tenant data boundaries cannot be breached.
 */
@Repository
public interface ComparisonRepository extends JpaRepository<Comparison, Long> {

    /**
     * Retrieves all comparisons created by a specific user, sorted chronologically.
     */
    List<Comparison> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Finds comparison by ID while verifying tenant ownership (IDOR defense).
     */
    Optional<Comparison> findByIdAndUserId(Long id, Long userId);

    /**
     * Eagerly fetches a comparison and all associated competing merchant offers in a single roundtrip.
     * Prevents N+1 query loops when constructing comparison matrices.
     */
    @Query("SELECT c FROM Comparison c LEFT JOIN FETCH c.offers WHERE c.id = :id AND c.userId = :userId")
    Optional<Comparison> findByIdAndUserIdWithOffers(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * Verifies existence and ownership without retrieving table columns into memory.
     */
    boolean existsByIdAndUserId(Long id, Long userId);

    /**
     * Fast COUNT(*) aggregation for dashboard metrics.
     */
    long countByUserId(Long userId);
}
