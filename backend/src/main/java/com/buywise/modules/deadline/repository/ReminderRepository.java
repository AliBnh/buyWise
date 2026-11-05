package com.buywise.modules.deadline.repository;

import com.buywise.modules.deadline.model.Reminder;
import com.buywise.modules.deadline.model.ReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Persistence repository for individual scheduled reminder notification alerts.
 * 
 * Demonstrates:
 * - Daemon Batch Query Optimization: findPendingRemindersDue retrieves all due alerts in a single batch
 *   for the background cron scheduler.
 * - Double-Dispatch Prevention: existsByDeadlineIdAndTriggerDate ensures the system never schedules duplicate
 *   reminders for the same deadline on the same date.
 */
@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    /**
     * Lists reminder notifications for a user filtered by status (e.g. SENT, DISMISSED).
     */
    List<Reminder> findByUserIdAndStatusOrderByTriggerDateDesc(Long userId, ReminderStatus status);

    /**
     * Polling query used by {@link com.buywise.modules.deadline.service.ReminderSchedulerService}
     * to find all pending alerts whose trigger date is reached or passed.
     */
    @Query("SELECT r FROM Reminder r WHERE r.status = 'PENDING' AND r.triggerDate <= :currentDate")
    List<Reminder> findPendingRemindersDue(@Param("currentDate") LocalDate currentDate);

    /**
     * Idempotency constraint check before creating a reminder alert.
     */
    boolean existsByDeadlineIdAndTriggerDate(Long deadlineId, LocalDate triggerDate);
}
