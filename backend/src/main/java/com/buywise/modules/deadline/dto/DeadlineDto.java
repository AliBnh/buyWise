package com.buywise.modules.deadline.dto;

import com.buywise.modules.deadline.model.DeadlineType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeadlineDto {

    private Long id;
    private Long userId;
    private Long purchaseId;
    private String title;
    private DeadlineType deadlineType;
    private LocalDate dueDate;
    private String urgency;
    private Boolean isCompleted;
    private String notes;
    private long daysRemaining;
    private boolean isExpired;
    private Instant createdAt;
}
