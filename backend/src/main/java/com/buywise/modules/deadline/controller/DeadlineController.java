package com.buywise.modules.deadline.controller;

import com.buywise.modules.deadline.dto.CreateDeadlineRequest;
import com.buywise.modules.deadline.dto.DeadlineDto;
import com.buywise.modules.deadline.dto.ReminderDto;
import com.buywise.modules.deadline.service.DeadlineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/deadlines")
@RequiredArgsConstructor
@Tag(name = "Deadlines & Reminders", description = "Endpoints for warranty expiration dates, return windows, and alerts")
public class DeadlineController {

    private final DeadlineService deadlineService;

    @GetMapping
    @Operation(summary = "List all deadlines for the current user")
    public ResponseEntity<List<DeadlineDto>> getDeadlines(
            @RequestParam(required = false, defaultValue = "false") Boolean onlyPending) {
        return ResponseEntity.ok(deadlineService.getUserDeadlines(onlyPending));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "List upcoming deadlines within the specified days (default 30 days)")
    public ResponseEntity<List<DeadlineDto>> getUpcomingDeadlines(
            @RequestParam(required = false, defaultValue = "30") int daysAhead) {
        return ResponseEntity.ok(deadlineService.getUpcomingDeadlines(daysAhead));
    }

    @GetMapping("/expired")
    @Operation(summary = "List already expired deadlines")
    public ResponseEntity<List<DeadlineDto>> getExpiredDeadlines() {
        return ResponseEntity.ok(deadlineService.getExpiredDeadlines());
    }

    @PostMapping
    @Operation(summary = "Create a custom deadline")
    public ResponseEntity<DeadlineDto> createDeadline(@Valid @RequestBody CreateDeadlineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deadlineService.createCustomDeadline(request));
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Mark a deadline as completed")
    public ResponseEntity<DeadlineDto> markCompleted(@PathVariable Long id) {
        return ResponseEntity.ok(deadlineService.markCompleted(id));
    }

    @GetMapping("/reminders")
    @Operation(summary = "List active triggered in-app reminder notifications")
    public ResponseEntity<List<ReminderDto>> getActiveReminders() {
        return ResponseEntity.ok(deadlineService.getActiveReminders());
    }

    @PatchMapping("/reminders/{id}/dismiss")
    @Operation(summary = "Dismiss an active reminder")
    public ResponseEntity<Void> dismissReminder(@PathVariable Long id) {
        deadlineService.dismissReminder(id);
        return ResponseEntity.noContent().build();
    }
}
