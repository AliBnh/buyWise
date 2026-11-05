package com.buywise.modules.purchase.repository;

import com.buywise.modules.purchase.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Persistence repository for owned consumer assets, warranty ranges, and portfolio expenditure aggregations.
 * 
 * Demonstrates:
 * - In-Database Aggregation Performance: Offloads sum calculations to PostgreSQL via COALESCE(SUM(...), 0)
 *   instead of retrieving all records into JVM memory ($O(1)$ network payload vs $O(N)$ row hydration).
 * - Multi-Tenant Isolation (IDOR Defense): Strictly enforces user ownership across all reads and aggregations.
 * - Temporal Range Indexing: Supports optimized queries on warrantyExpirationDate between calendar boundaries.
 */
@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    /**
     * Lists all registered purchases owned by a user, ordered with newest acquisitions first.
     */
    List<Purchase> findByUserIdOrderByPurchaseDateDesc(Long userId);

    /**
     * Retrieves a purchase record while enforcing tenant ownership checks.
     */
    Optional<Purchase> findByIdAndUserId(Long id, Long userId);

    /**
     * Finds active warranties that will expire within an upcoming calendar interval.
     */
    @Query("SELECT p FROM Purchase p WHERE p.userId = :userId AND p.warrantyExpirationDate BETWEEN :startDate AND :endDate ORDER BY p.warrantyExpirationDate ASC")
    List<Purchase> findExpiringWarranties(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Counts total owned products for user dashboard metrics.
     */
    long countByUserId(Long userId);

    /**
     * Computes the cumulative financial expenditure of the user directly inside the database.
     * Uses COALESCE to guarantee a safe 0 return when no purchases exist, preventing NullPointerExceptions.
     */
    @Query("SELECT COALESCE(SUM(p.pricePaid), 0) FROM Purchase p WHERE p.userId = :userId")
    BigDecimal sumTotalSpentByUserId(@Param("userId") Long userId);
}
