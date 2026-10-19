package com.buywise.modules.comparison.controller;

import com.buywise.modules.comparison.dto.*;
import com.buywise.modules.comparison.service.ComparisonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comparisons")
@RequiredArgsConstructor
@Tag(name = "Comparisons", description = "Endpoints for managing purchasing comparisons and competing seller offers")
public class ComparisonController {

    private final ComparisonService comparisonService;

    @PostMapping
    @Operation(summary = "Create a new purchasing comparison")
    public ResponseEntity<ComparisonResponse> createComparison(@Valid @RequestBody CreateComparisonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(comparisonService.createComparison(request));
    }

    @GetMapping
    @Operation(summary = "List all comparisons belonging to the authenticated user")
    public ResponseEntity<List<ComparisonResponse>> getUserComparisons() {
        return ResponseEntity.ok(comparisonService.getUserComparisons());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get comparison by ID with its offers and deterministic mathematical summary")
    public ResponseEntity<ComparisonResponse> getComparisonById(@PathVariable Long id) {
        return ResponseEntity.ok(comparisonService.getComparisonById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update comparison metadata")
    public ResponseEntity<ComparisonResponse> updateComparison(
            @PathVariable Long id,
            @Valid @RequestBody UpdateComparisonRequest request) {
        return ResponseEntity.ok(comparisonService.updateComparison(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete comparison")
    public ResponseEntity<Void> deleteComparison(@PathVariable Long id) {
        comparisonService.deleteComparison(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/offers")
    @Operation(summary = "Add an offer to an existing comparison")
    public ResponseEntity<ComparisonResponse> addOffer(
            @PathVariable Long id,
            @Valid @RequestBody CreateOfferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(comparisonService.addOffer(id, request));
    }

    @PutMapping("/{id}/offers/{offerId}")
    @Operation(summary = "Update offer details")
    public ResponseEntity<ComparisonResponse> updateOffer(
            @PathVariable Long id,
            @PathVariable Long offerId,
            @Valid @RequestBody UpdateOfferRequest request) {
        return ResponseEntity.ok(comparisonService.updateOffer(id, offerId, request));
    }

    @DeleteMapping("/{id}/offers/{offerId}")
    @Operation(summary = "Remove offer from comparison")
    public ResponseEntity<ComparisonResponse> deleteOffer(
            @PathVariable Long id,
            @PathVariable Long offerId) {
        return ResponseEntity.ok(comparisonService.deleteOffer(id, offerId));
    }
}
