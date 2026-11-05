package com.buywise.modules.purchase.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Ingress DTO registering an acquired asset into the user's permanent ownership library.
 * 
 * Demonstrates:
 * - Temporal Invariant Input: Captures purchaseDate and warrantyMonths used to automatically
 *   derive warrantyExpirationDate and schedule calendar deadline alerts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchaseRequest {

    @NotBlank(message = "Product name is required")
    private String productName;

    private String modelNumber;
    private String category;

    @NotBlank(message = "Seller name is required")
    private String sellerName;

    @NotNull(message = "Purchase date is required")
    private LocalDate purchaseDate;

    @NotNull(message = "Price paid is required")
    @DecimalMin(value = "0.0", message = "Price paid must be non-negative")
    private BigDecimal pricePaid;

    @Builder.Default
    private String currency = "MAD";

    @Min(value = 0, message = "Warranty months must be non-negative")
    @Builder.Default
    private Integer warrantyMonths = 12;

    private LocalDate returnDeadline;
    private String serialNumber;
    private String notes;
}
