package com.buywise.modules.deadline.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

/**
 * =================================================================================================
 * ARCHITECTURAL DESIGN NOTE: REMINDER ENTITY & SCHEDULED ALERT DISPATCH
 * =================================================================================================
 * Relational Theory & BCNF:
 * 1. Decoupled Alert Subsystem:
 *    - Reminders are decoupled from Deadlines into their own relation to support multi-stage alerts
 *      (e.g., alert 7 days before, alert 3 days before, alert on expiration day).
 *    - Schema satisfies BCNF: Candidate Key is {id}; determinant {id} uniquely dictates
 *      {deadlineId, userId, triggerDate, message, status, triggeredAt, createdAt}.
 *
 * 2. Idempotent State Machine:
 *    - Uses {@link ReminderStatus} (PENDING -> TRIGGERED -> DISMISSED).
 *    - Prevents duplicate email/push notifications during scheduled background sweeps.
 * =================================================================================================
 */
@Entity
@Table(name = "reminders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "deadline_id", nullable = false)
    private Long deadlineId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "trigger_date", nullable = false)
    private LocalDate triggerDate;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private ReminderStatus status = ReminderStatus.PENDING;

    @Column(name = "triggered_at")
    private Instant triggeredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
