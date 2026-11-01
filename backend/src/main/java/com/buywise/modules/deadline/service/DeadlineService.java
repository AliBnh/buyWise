package com.buywise.modules.deadline.service;

import com.buywise.common.exception.ResourceNotFoundException;
import com.buywise.common.security.SecurityUtils;
import com.buywise.modules.deadline.dto.CreateDeadlineRequest;
import com.buywise.modules.deadline.dto.DeadlineDto;
import com.buywise.modules.deadline.dto.ReminderDto;
import com.buywise.modules.deadline.model.Deadline;
import com.buywise.modules.deadline.model.DeadlineType;
import com.buywise.modules.deadline.model.Reminder;
import com.buywise.modules.deadline.model.ReminderStatus;
import com.buywise.modules.deadline.repository.DeadlineRepository;
import com.buywise.modules.deadline.repository.ReminderRepository;
import com.buywise.modules.purchase.model.Purchase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * =================================================================================================
 * ARCHITECTURAL DESIGN NOTE: DEADLINE & IDEMPOTENT REMINDER SERVICE
 * =================================================================================================
 * Architectural Principles:
 * 1. Idempotency & Repeatable Scheduling:
 *    - In distributed microservice and containerized environments, nodes may restart or retry jobs.
 *    - Reminder registration checks if a trigger for a specific deadline and date already exists
 *      before inserting, ensuring that duplicate reminders are never spawned.
 *
 * 2. Single Responsibility Principle (SRP):
 *    - Decouples deadline calculations (days remaining, overdue status, urgency calculation)
 *      from the notification delivery and presentation layers.
 * =================================================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeadlineService {

    private final DeadlineRepository deadlineRepository;
    private final ReminderRepository reminderRepository;

    @Transactional
    public DeadlineDto createCustomDeadline(CreateDeadlineRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        Deadline deadline = Deadline.builder()
                .userId(userId)
                .purchaseId(request.getPurchaseId())
                .title(request.getTitle().trim())
                .deadlineType(request.getDeadlineType())
                .dueDate(request.getDueDate())
                .urgency(request.getUrgency() != null ? request.getUrgency().toUpperCase() : "MEDIUM")
                .notes(request.getNotes())
                .isCompleted(false)
                .build();

        Deadline saved = deadlineRepository.save(deadline);
        scheduleStandardReminders(saved);

        return mapToDto(saved);
    }

    @Transactional
    public void registerPurchaseDeadlines(Purchase purchase) {
        // 1. Warranty Expiration Deadline
        if (purchase.getWarrantyExpirationDate() != null) {
            boolean exists = deadlineRepository.existsByPurchaseIdAndDeadlineType(
                    purchase.getId(), DeadlineType.WARRANTY_EXPIRY);
            if (!exists) {
                Deadline warrantyDeadline = Deadline.builder()
                        .userId(purchase.getUserId())
                        .purchaseId(purchase.getId())
                        .title("Warranty Expiry: " + purchase.getProductName())
                        .deadlineType(DeadlineType.WARRANTY_EXPIRY)
                        .dueDate(purchase.getWarrantyExpirationDate())
                        .urgency(calculateUrgency(purchase.getWarrantyExpirationDate()))
                        .isCompleted(false)
                        .notes("Manufacturer/Seller warranty coverage period ends.")
                        .build();

                Deadline saved = deadlineRepository.save(warrantyDeadline);
                scheduleWarrantyReminders(saved, purchase);
            }
        }

        // 2. Return Window Deadline (if specified)
        if (purchase.getReturnDeadline() != null) {
            boolean exists = deadlineRepository.existsByPurchaseIdAndDeadlineType(
                    purchase.getId(), DeadlineType.RETURN_WINDOW);
            if (!exists) {
                Deadline returnDeadline = Deadline.builder()
                        .userId(purchase.getUserId())
                        .purchaseId(purchase.getId())
                        .title("Return Window Closing: " + purchase.getProductName())
                        .deadlineType(DeadlineType.RETURN_WINDOW)
                        .dueDate(purchase.getReturnDeadline())
                        .urgency("HIGH")
                        .isCompleted(false)
                        .notes("Last day to return for refund or replacement under seller policy.")
                        .build();

                Deadline saved = deadlineRepository.save(returnDeadline);
                scheduleReturnReminders(saved, purchase);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<DeadlineDto> getUserDeadlines(Boolean onlyPending) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Deadline> deadlines = Boolean.TRUE.equals(onlyPending)
                ? deadlineRepository.findByUserIdAndIsCompletedFalseOrderByDueDateAsc(userId)
                : deadlineRepository.findByUserIdOrderByDueDateAsc(userId);

        return deadlines.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DeadlineDto> getUpcomingDeadlines(int daysAhead) {
        Long userId = SecurityUtils.getCurrentUserId();
        LocalDate now = LocalDate.now();
        LocalDate target = now.plusDays(daysAhead);

        return deadlineRepository.findUpcomingDeadlines(userId, now, target).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DeadlineDto> getExpiredDeadlines() {
        Long userId = SecurityUtils.getCurrentUserId();
        LocalDate now = LocalDate.now();

        return deadlineRepository.findExpiredDeadlines(userId, now).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DeadlineDto markCompleted(Long deadlineId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Deadline deadline = deadlineRepository.findByIdAndUserId(deadlineId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Deadline", "id", deadlineId));

        deadline.setIsCompleted(true);
        return mapToDto(deadlineRepository.save(deadline));
    }

    @Transactional(readOnly = true)
    public List<ReminderDto> getActiveReminders() {
        Long userId = SecurityUtils.getCurrentUserId();
        return reminderRepository.findByUserIdAndStatusOrderByTriggerDateDesc(userId, ReminderStatus.TRIGGERED)
                .stream().map(this::mapReminderToDto).collect(Collectors.toList());
    }

    @Transactional
    public void dismissReminder(Long reminderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder", "id", reminderId));

        if (!reminder.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Reminder", "id", reminderId);
        }

        reminder.setStatus(ReminderStatus.DISMISSED);
        reminderRepository.save(reminder);
    }

    private void scheduleWarrantyReminders(Deadline deadline, Purchase purchase) {
        LocalDate expiry = deadline.getDueDate();
        // 30 days prior alert
        createReminderIfNotExists(deadline.getId(), purchase.getUserId(),
                expiry.minusDays(30),
                String.format("Warranty for '%s' expires in 30 days on %s. Check product functionality now.",
                        purchase.getProductName(), expiry));

        // 7 days prior alert
        createReminderIfNotExists(deadline.getId(), purchase.getUserId(),
                expiry.minusDays(7),
                String.format("URGENT: Warranty for '%s' expires in 7 days (%s).",
                        purchase.getProductName(), expiry));
    }

    private void scheduleReturnReminders(Deadline deadline, Purchase purchase) {
        LocalDate deadlineDate = deadline.getDueDate();
        // 3 days prior alert
        createReminderIfNotExists(deadline.getId(), purchase.getUserId(),
                deadlineDate.minusDays(3),
                String.format("Return window for '%s' closes in 3 days (%s). Ensure packaging is intact if returning.",
                        purchase.getProductName(), deadlineDate));

        // 1 day prior alert
        createReminderIfNotExists(deadline.getId(), purchase.getUserId(),
                deadlineDate.minusDays(1),
                String.format("FINAL NOTICE: Tomorrow (%s) is the last day to return '%s'.",
                        deadlineDate, purchase.getProductName()));
    }

    private void scheduleStandardReminders(Deadline deadline) {
        createReminderIfNotExists(deadline.getId(), deadline.getUserId(),
                deadline.getDueDate().minusDays(3),
                String.format("Reminder: '%s' is due in 3 days on %s.", deadline.getTitle(), deadline.getDueDate()));
    }

    private void createReminderIfNotExists(Long deadlineId, Long userId, LocalDate triggerDate, String message) {
        if (!triggerDate.isBefore(LocalDate.now()) && !reminderRepository.existsByDeadlineIdAndTriggerDate(deadlineId, triggerDate)) {
            Reminder reminder = Reminder.builder()
                    .deadlineId(deadlineId)
                    .userId(userId)
                    .triggerDate(triggerDate)
                    .message(message)
                    .status(ReminderStatus.PENDING)
                    .build();
            reminderRepository.save(reminder);
        }
    }

    private String calculateUrgency(LocalDate dueDate) {
        long days = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
        if (days < 0) return "EXPIRED";
        if (days <= 7) return "CRITICAL";
        if (days <= 30) return "HIGH";
        if (days <= 90) return "MEDIUM";
        return "LOW";
    }

    public DeadlineDto mapToDto(Deadline d) {
        LocalDate today = LocalDate.now();
        long days = ChronoUnit.DAYS.between(today, d.getDueDate());
        boolean isExpired = d.getDueDate().isBefore(today);

        return DeadlineDto.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .purchaseId(d.getPurchaseId())
                .title(d.getTitle())
                .deadlineType(d.getDeadlineType())
                .dueDate(d.getDueDate())
                .urgency(d.getUrgency())
                .isCompleted(d.getIsCompleted())
                .notes(d.getNotes())
                .daysRemaining(days)
                .isExpired(isExpired)
                .createdAt(d.getCreatedAt())
                .build();
    }

    private ReminderDto mapReminderToDto(Reminder r) {
        return ReminderDto.builder()
                .id(r.getId())
                .deadlineId(r.getDeadlineId())
                .userId(r.getUserId())
                .triggerDate(r.getTriggerDate())
                .message(r.getMessage())
                .status(r.getStatus())
                .triggeredAt(r.getTriggeredAt())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
