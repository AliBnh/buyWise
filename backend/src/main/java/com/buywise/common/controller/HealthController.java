package com.buywise.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Public liveness and readiness probe endpoint used by Docker healthchecks and reverse proxies.
 * 
 * Demonstrates:
 * - Kubernetes/Docker Health Check Standard: Provides a zero-dependency, non-authenticated endpoint
 *   for container orchestrators to probe HTTP responsiveness without triggering session or DB load.
 */
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health Check", description = "Liveness and Readiness probe endpoint")
public class HealthController {

    /**
     * Responds immediately with HTTP 200 OK and heartbeat timestamp to signal container health.
     * 
     * @return HTTP 200 OK with health status metadata.
     */
    @GetMapping
    @Operation(summary = "Application health status")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "application", "BuyWise Intelligence Platform",
                "timestamp", Instant.now().toString(),
                "version", "1.0.0"
        ));
    }
}
