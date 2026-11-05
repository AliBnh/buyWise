package com.buywise.modules.purchase.controller;

import com.buywise.modules.purchase.dto.CreatePurchaseRequest;
import com.buywise.modules.purchase.dto.PurchaseResponse;
import com.buywise.modules.purchase.dto.PurchaseStatsDto;
import com.buywise.modules.purchase.dto.UpdatePurchaseRequest;
import com.buywise.modules.purchase.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller managing post-purchase asset registration, warranty tracking, and ownership analytics.
 * 
 * Demonstrates:
 * - Domain-Driven Architecture: Encapsulates post-purchase lifecycle distinct from pre-purchase comparisons.
 * - Automatic Deadline Propagation: Registering an asset automatically schedules warranty and return deadlines.
 * - Tenant Scoping: Guarantees users can only view, mutate, or aggregate their own registered assets.
 */
@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
@Tag(name = "Purchases & Ownership", description = "Endpoints for managing registered consumer items, receipts, and ownership lifecycle")
public class PurchaseController {

    private final PurchaseService purchaseService;

    /**
     * Registers a new purchased item, links it to an optional comparison offer, and triggers deadline creation.
     * 
     * @param request Validated purchase registration DTO.
     * @return HTTP 201 CREATED with stored purchase entity and calculated warranty expiration dates.
     */
    @PostMapping
    @Operation(summary = "Register a newly purchased product into the ownership library")
    public ResponseEntity<PurchaseResponse> registerPurchase(@Valid @RequestBody CreatePurchaseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseService.registerPurchase(request));
    }

    /**
     * Lists all registered assets owned by the calling user.
     * 
     * @return HTTP 200 OK with collection of user purchases.
     */
    @GetMapping
    @Operation(summary = "List all owned products for the authenticated user")
    public ResponseEntity<List<PurchaseResponse>> getUserPurchases() {
        return ResponseEntity.ok(purchaseService.getUserPurchases());
    }

    /**
     * Fetches detailed information for a specific purchase, including attached receipt documents and deadlines.
     * 
     * @param id Purchase ID.
     * @return HTTP 200 OK with full purchase record.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get detailed record of a purchased product")
    public ResponseEntity<PurchaseResponse> getPurchaseById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.getPurchaseById(id));
    }

    /**
     * Updates an existing purchase record (e.g. adding serial numbers, invoice notes, or retailer details).
     * 
     * @param id Target purchase ID.
     * @param request Validated update fields.
     * @return HTTP 200 OK with updated record.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update purchase details or serial numbers")
    public ResponseEntity<PurchaseResponse> updatePurchase(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePurchaseRequest request) {
        return ResponseEntity.ok(purchaseService.updatePurchase(id, request));
    }

    /**
     * Deletes a registered purchase and automatically cancels pending reminders.
     * 
     * @param id Purchase ID.
     * @return HTTP 204 NO CONTENT.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete purchase record")
    public ResponseEntity<Void> deletePurchase(@PathVariable Long id) {
        purchaseService.deletePurchase(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Aggregates total expenditure, active warranty count, and items with expiring return windows.
     * 
     * @return HTTP 200 OK with portfolio metrics.
     */
    @GetMapping("/stats")
    @Operation(summary = "Get high-level ownership and warranty statistics")
    public ResponseEntity<PurchaseStatsDto> getPurchaseStats() {
        return ResponseEntity.ok(purchaseService.getPurchaseStats());
    }
}
