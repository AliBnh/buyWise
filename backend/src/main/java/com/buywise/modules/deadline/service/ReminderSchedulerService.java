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
 * Scheduled background daemon evaluating due reminders.
 * 
 * Demonstrates:
 * - Idempotent Batch Processing: Safe to execute repeatedly without duplicating alerts.
 * - Decoupled Architecture: Evaluates triggers asynchronously without blocking user requests.
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
