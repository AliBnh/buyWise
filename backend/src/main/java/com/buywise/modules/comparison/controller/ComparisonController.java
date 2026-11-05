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

/**
 * REST controller exposing purchasing comparison workflows and seller offer management.
 * 
 * Demonstrates:
 * - RESTful Hierarchical Resource Modeling: Sub-resources (offers) nested cleanly under parent comparisons:
 *   POST /api/v1/comparisons/{id}/offers
 * - Multi-Tenant Data Isolation (IDOR Protection): All endpoints authenticate the caller and enforce
 *   user-ownership invariants at the service/database layer.
 * - Stateless Deterministic Evaluation: Returns dynamically computed pricing summaries with zero client lag.
 */
@RestController
@RequestMapping("/api/v1/comparisons")
@RequiredArgsConstructor
@Tag(name = "Comparisons", description = "Endpoints for managing purchasing comparisons and competing seller offers")
public class ComparisonController {

    private final ComparisonService comparisonService;

    /**
     * Creates a new purchasing comparison workspace, optionally initialized with candidate offers.
     * 
     * @param request Validated comparison creation DTO.
     * @return HTTP 201 CREATED with persisted comparison and initial scoring matrix.
     */
    @PostMapping
    @Operation(summary = "Create a new purchasing comparison")
    public ResponseEntity<ComparisonResponse> createComparison(@Valid @RequestBody CreateComparisonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(comparisonService.createComparison(request));
    }

    /**
     * Lists all purchasing comparisons created by the authenticated user, ordered by update timestamp.
     * 
     * @return HTTP 200 OK with list of comparison summaries.
     */
    @GetMapping
    @Operation(summary = "List all comparisons belonging to the authenticated user")
    public ResponseEntity<List<ComparisonResponse>> getUserComparisons() {
        return ResponseEntity.ok(comparisonService.getUserComparisons());
    }

    /**
     * Retrieves full comparison details by ID, including nested offers, normalized specs, and mathematical metrics.
     * 
     * @param id Unique identifier of the comparison.
     * @return HTTP 200 OK with populated comparison response.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get comparison by ID with its offers and deterministic mathematical summary")
    public ResponseEntity<ComparisonResponse> getComparisonById(@PathVariable Long id) {
        return ResponseEntity.ok(comparisonService.getComparisonById(id));
    }

    /**
     * Updates comparison workspace metadata (title, category, status).
     * 
     * @param id Comparison ID.
     * @param request Validated update payload.
     * @return HTTP 200 OK with modified comparison state.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update comparison metadata")
    public ResponseEntity<ComparisonResponse> updateComparison(
            @PathVariable Long id,
            @Valid @RequestBody UpdateComparisonRequest request) {
        return ResponseEntity.ok(comparisonService.updateComparison(id, request));
    }

    /**
     * Deletes a comparison workspace and all cascaded offers.
     * 
     * @param id Comparison ID to delete.
     * @return HTTP 204 NO CONTENT indicating successful deletion.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete comparison")
    public ResponseEntity<Void> deleteComparison(@PathVariable Long id) {
        comparisonService.deleteComparison(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Appends a new competing merchant offer to an existing comparison.
     * 
     * @param id Parent comparison ID.
     * @param request Offer details (seller, base price, shipping, warranty, specs).
     * @return HTTP 201 CREATED with recalculated comparison summary.
     */
    @PostMapping("/{id}/offers")
    @Operation(summary = "Add an offer to an existing comparison")
    public ResponseEntity<ComparisonResponse> addOffer(
            @PathVariable Long id,
            @Valid @RequestBody CreateOfferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(comparisonService.addOffer(id, request));
    }

    /**
     * Modifies price, delivery terms, or specs of a specific merchant offer.
     * 
     * @param id Parent comparison ID.
     * @param offerId Target offer ID.
     * @param request Validated offer update payload.
     * @return HTTP 200 OK with recomputed comparison scoring.
     */
    @PutMapping("/{id}/offers/{offerId}")
    @Operation(summary = "Update offer details")
    public ResponseEntity<ComparisonResponse> updateOffer(
            @PathVariable Long id,
            @PathVariable Long offerId,
            @Valid @RequestBody UpdateOfferRequest request) {
        return ResponseEntity.ok(comparisonService.updateOffer(id, offerId, request));
    }

    /**
     * Removes an offer from a comparison and triggers recalculation of remaining offer scores.
     * 
     * @param id Parent comparison ID.
     * @param offerId Offer ID to remove.
     * @return HTTP 200 OK with refreshed comparison state.
     */
    @DeleteMapping("/{id}/offers/{offerId}")
    @Operation(summary = "Remove offer from comparison")
    public ResponseEntity<ComparisonResponse> deleteOffer(
            @PathVariable Long id,
            @PathVariable Long offerId) {
        return ResponseEntity.ok(comparisonService.deleteOffer(id, offerId));
    }
}
