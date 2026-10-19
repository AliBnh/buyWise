package com.buywise.modules.comparison.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferResponse {

    private Long id;
    private Long comparisonId;
    private String sellerName;
    private Long sellerId;
    private String title;
    private String productModel;
    private String sourceUrl;
    private String currency;
    private BigDecimal basePrice;
    private BigDecimal deliveryFee;
    private BigDecimal additionalFees;
    private BigDecimal discountAmount;
    private BigDecimal totalPrice;
    private Integer warrantyMonths;
    private Integer returnWindowDays;
    private Map<String, Object> specifications;
    private String specificationsJson;
    private String warrantyConditions;
    private String returnPolicy;
    private Boolean isRecommended;
    private BigDecimal valueScore;
    private Instant createdAt;
    private Instant updatedAt;
}
