package com.buywise.modules.deadline.dto;

import com.buywise.modules.deadline.model.DeadlineType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDeadlineRequest {

    private Long purchaseId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Deadline type is required")
    private DeadlineType deadlineType;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private String urgency;
    private String notes;
}
