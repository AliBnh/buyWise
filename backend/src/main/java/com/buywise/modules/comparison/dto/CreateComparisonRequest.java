package com.buywise.modules.comparison.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Ingress DTO creating a comparison workspace, optionally bootstrapping it with initial merchant offers.
 * 
 * Demonstrates:
 * - Atomic Composite Creation: Allows creating a parent comparison and child offers in a single transaction.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateComparisonRequest {

    @NotBlank(message = "Comparison title is required")
    private String title;

    @NotBlank(message = "Category name is required")
    private String categoryName;

    private String notes;

    private List<CreateOfferRequest> initialOffers;
}
