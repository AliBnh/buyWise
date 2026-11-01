package com.buywise.modules.comparison.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOfferRequest {

    @NotBlank(message = "Seller name is required")
    private String sellerName;

    private Long sellerId;

    @NotBlank(message = "Offer title is required")
    private String title;

    private String productModel;
    private String sourceUrl;

    @Builder.Default
    private String currency = "MAD";

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", message = "Base price must be non-negative")
    private BigDecimal basePrice;

    private BigDecimal deliveryFee;
    private BigDecimal additionalFees;
    private BigDecimal discountAmount;

    @Min(value = 0, message = "Warranty months must be non-negative")
    private Integer warrantyMonths;

    @Min(value = 0, message = "Return window days must be non-negative")
    private Integer returnWindowDays;

    private Map<String, Object> specifications;
    private String specificationsJson;
    private String warrantyConditions;
    private String returnPolicy;
}
