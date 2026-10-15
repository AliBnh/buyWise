package com.buywise.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Standardized API error response payload.
 * 
 * Demonstrates:
 * - Builder Pattern: Allows flexible construction of error objects with optional validation field mappings.
 * - Uniform API Design: Ensures client consumers always receive predictable, typed error structures.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final Map<String, String> validationErrors;
}
