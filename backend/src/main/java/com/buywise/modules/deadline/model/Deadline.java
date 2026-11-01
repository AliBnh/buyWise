package com.buywise.modules.deadline.model;

import com.buywise.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * =================================================================================================
 * ARCHITECTURAL DESIGN NOTE: DEADLINE ENTITY & DECOUPLED LIFECYCLE MANAGEMENT
 * =================================================================================================
 * Relational Theory & BCNF:
 * 1. Normalization & Decoupling from Purchase:
 *    - Deadlines are separated from the `purchases` table to achieve 3NF and BCNF.
 *    - A single purchase can have multiple time-sensitive deadlines (e.g. 14-day return window,
 *      12-month manufacturer warranty, 24-month extended warranty).
 *    - If deadlines were embedded as columns in `purchases`, we would violate 1NF/2NF or produce
 *      sparse null-filled tables.
 *
 * 2. Candidate Key & Dependencies:
 *    - Candidate Key: {id}
 *    - FDs: id -> {userId, purchaseId, title, deadlineType, dueDate, urgency, isCompleted, notes}
 *    - Satisfies BCNF because the sole determinant {id} is a superkey.
 * =================================================================================================
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
