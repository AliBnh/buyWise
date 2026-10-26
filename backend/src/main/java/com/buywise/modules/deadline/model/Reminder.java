package com.buywise.modules.deadline.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Domain entity representing an alert or scheduled in-app notification for a deadline.
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
