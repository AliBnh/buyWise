package com.buywise.modules.deadline.service;

import com.buywise.modules.deadline.model.Reminder;
import com.buywise.modules.deadline.model.ReminderStatus;
import com.buywise.modules.deadline.repository.ReminderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * =================================================================================================
 * ARCHITECTURAL DESIGN NOTE: BACKGROUND SCHEDULING & STATE MACHINE PROCESSING
 * =================================================================================================
 * Architectural Patterns:
 * 1. Scheduled Background Daemon (@Scheduled):
 *    - Runs every 15 minutes via cron expression: runs every 15 minutes.
 *    - Decoupled from user HTTP requests; ensures timely notification dispatch without degrading
 *      consumer web request response latencies.
 *
 * 2. State Machine & Idempotent Batch Processing:
 *    - Queries ONLY reminders where `status = 'PENDING'` and `trigger_date <= CURRENT_DATE`.
 *    - Immediately flips status to `TRIGGERED` and records `triggeredAt` timestamp in an atomic
 *      database transaction, preventing duplicate processing if multiple scheduler instances fire.
 * =================================================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderSchedulerService {

    private final ReminderRepository reminderRepository;

    @Scheduled(cron = "0 */15 * * * *") // Every 15 minutes
    @Transactional
    public void processDueReminders() {
        LocalDate today = LocalDate.now();
        List<Reminder> dueReminders = reminderRepository.findPendingRemindersDue(today);

        if (!dueReminders.isEmpty()) {
            log.info("Triggering {} due reminders for date: {}", dueReminders.size(), today);
            for (Reminder reminder : dueReminders) {
                reminder.setStatus(ReminderStatus.TRIGGERED);
                reminder.setTriggeredAt(Instant.now());
            }
            reminderRepository.saveAll(dueReminders);
        }
    }
}
