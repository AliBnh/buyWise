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

@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
@Tag(name = "Purchases & Ownership", description = "Endpoints for managing registered consumer items, receipts, and ownership lifecycle")
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping
    @Operation(summary = "Register a newly purchased product into the ownership library")
    public ResponseEntity<PurchaseResponse> registerPurchase(@Valid @RequestBody CreatePurchaseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseService.registerPurchase(request));
    }

    @GetMapping
    @Operation(summary = "List all owned products for the authenticated user")
    public ResponseEntity<List<PurchaseResponse>> getUserPurchases() {
        return ResponseEntity.ok(purchaseService.getUserPurchases());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed record of a purchased product")
    public ResponseEntity<PurchaseResponse> getPurchaseById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.getPurchaseById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update purchase details or serial numbers")
    public ResponseEntity<PurchaseResponse> updatePurchase(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePurchaseRequest request) {
        return ResponseEntity.ok(purchaseService.updatePurchase(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete purchase record")
    public ResponseEntity<Void> deletePurchase(@PathVariable Long id) {
        purchaseService.deletePurchase(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @Operation(summary = "Get high-level ownership and warranty statistics")
    public ResponseEntity<PurchaseStatsDto> getPurchaseStats() {
        return ResponseEntity.ok(purchaseService.getPurchaseStats());
    }
}
