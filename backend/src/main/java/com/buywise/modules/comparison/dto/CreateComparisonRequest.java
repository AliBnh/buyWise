package com.buywise.modules.comparison.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
