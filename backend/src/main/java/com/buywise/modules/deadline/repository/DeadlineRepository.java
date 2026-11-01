package com.buywise.modules.deadline.repository;

import com.buywise.modules.deadline.model.Deadline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeadlineRepository extends JpaRepository<Deadline, Long> {

    List<Deadline> findByUserIdOrderByDueDateAsc(Long userId);

    List<Deadline> findByUserIdAndIsCompletedFalseOrderByDueDateAsc(Long userId);

    Optional<Deadline> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT d FROM Deadline d WHERE d.userId = :userId AND d.isCompleted = false AND d.dueDate BETWEEN :startDate AND :endDate ORDER BY d.dueDate ASC")
    List<Deadline> findUpcomingDeadlines(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT d FROM Deadline d WHERE d.userId = :userId AND d.isCompleted = false AND d.dueDate < :currentDate ORDER BY d.dueDate ASC")
    List<Deadline> findExpiredDeadlines(
            @Param("userId") Long userId,
            @Param("currentDate") LocalDate currentDate
    );

    boolean existsByPurchaseIdAndDeadlineType(Long purchaseId, com.buywise.modules.deadline.model.DeadlineType type);
}
