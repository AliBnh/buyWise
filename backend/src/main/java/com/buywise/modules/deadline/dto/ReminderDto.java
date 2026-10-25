package com.buywise.modules.deadline.dto;

import com.buywise.modules.deadline.model.ReminderStatus;
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
public class ReminderDto {

    private Long id;
    private Long deadlineId;
    private Long userId;
    private LocalDate triggerDate;
    private String message;
    private ReminderStatus status;
    private Instant triggeredAt;
    private Instant createdAt;
}
