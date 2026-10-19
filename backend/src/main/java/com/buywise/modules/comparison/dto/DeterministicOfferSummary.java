package com.buywise.modules.comparison.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeterministicOfferSummary {

    private Long offerId;
    private String sellerName;
    private String title;
    private String productModel;
    private String currency;
    private BigDecimal basePrice;
    private BigDecimal deliveryFee;
    private BigDecimal additionalFees;
    private BigDecimal discountAmount;
    private BigDecimal totalPrice;
    private int warrantyMonths;
    private int returnWindowDays;
    private BigDecimal monthlyCostPerWarrantyMonth;
    private BigDecimal priceDifferenceFromLowest;
    private BigDecimal percentageDifferenceFromLowest;
}
