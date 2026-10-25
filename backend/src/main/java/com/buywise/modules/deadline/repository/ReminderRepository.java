package com.buywise.modules.deadline.repository;

import com.buywise.modules.deadline.model.Reminder;
import com.buywise.modules.deadline.model.ReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findByUserIdAndStatusOrderByTriggerDateDesc(Long userId, ReminderStatus status);

    @Query("SELECT r FROM Reminder r WHERE r.status = 'PENDING' AND r.triggerDate <= :currentDate")
    List<Reminder> findPendingRemindersDue(@Param("currentDate") LocalDate currentDate);

    boolean existsByDeadlineIdAndTriggerDate(Long deadlineId, LocalDate triggerDate);
}
