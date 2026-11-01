package com.buywise.modules.comparison.dto;

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
public class UpdateOfferRequest {

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
    private Integer warrantyMonths;
    private Integer returnWindowDays;
    private Map<String, Object> specifications;
    private String specificationsJson;
    private String warrantyConditions;
    private String returnPolicy;
}
