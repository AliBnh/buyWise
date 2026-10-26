package com.buywise.modules.deadline.model;

import com.buywise.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Domain entity tracking time-sensitive consumer events (warranties, return policies, document reviews).
 */
@Entity
@Table(name = "deadlines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deadline extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "purchase_id")
    private Long purchaseId;

    @Column(nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "deadline_type", nullable = false, length = 50)
    private DeadlineType deadlineType;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String urgency = "MEDIUM"; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "is_completed")
    @Builder.Default
    private Boolean isCompleted = false;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
