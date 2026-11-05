package com.buywise.modules.deadline.repository;

import com.buywise.modules.deadline.model.Deadline;
import com.buywise.modules.deadline.model.DeadlineType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Persistence repository for warranty expiration dates, return windows, and scheduled alerts.
 * 
 * Demonstrates:
 * - Temporal Index Optimization: Queries index on (user_id, due_date, is_completed) for instant calendar retrieval.
 * - Idempotency Enforcement: existsByPurchaseIdAndDeadlineType prevents duplicate deadlines when re-processing documents.
 * - Strict Tenant Isolation: Secures deadline records from cross-tenant visibility.
 */
@Repository
public interface DeadlineRepository extends JpaRepository<Deadline, Long> {

    /**
     * Lists all chronological deadlines for a user.
     */
    List<Deadline> findByUserIdOrderByDueDateAsc(Long userId);

    /**
     * Retrieves active, unresolved deadlines sorted with nearest due date first.
     */
    List<Deadline> findByUserIdAndIsCompletedFalseOrderByDueDateAsc(Long userId);

    /**
     * Retrieves a deadline while asserting tenant ownership.
     */
    Optional<Deadline> findByIdAndUserId(Long id, Long userId);

    /**
     * Finds active deadlines falling within a specific lookahead calendar window.
     */
    @Query("SELECT d FROM Deadline d WHERE d.userId = :userId AND d.isCompleted = false AND d.dueDate BETWEEN :startDate AND :endDate ORDER BY d.dueDate ASC")
    List<Deadline> findUpcomingDeadlines(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Finds overdue deadlines that have lapsed without completion.
     */
    @Query("SELECT d FROM Deadline d WHERE d.userId = :userId AND d.isCompleted = false AND d.dueDate < :currentDate ORDER BY d.dueDate ASC")
    List<Deadline> findExpiredDeadlines(
            @Param("userId") Long userId,
            @Param("currentDate") LocalDate currentDate
    );

    /**
     * Idempotency safeguard: checks whether a deadline of a specific type already exists for a purchase.
     */
    boolean existsByPurchaseIdAndDeadlineType(Long purchaseId, DeadlineType type);
}
