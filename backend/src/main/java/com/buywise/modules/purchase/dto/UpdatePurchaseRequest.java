package com.buywise.modules.purchase.dto;

import com.buywise.modules.purchase.model.PurchaseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePurchaseRequest {

    private String productName;
    private String modelNumber;
    private String category;
    private String sellerName;
    private LocalDate purchaseDate;
    private BigDecimal pricePaid;
    private String currency;
    private Integer warrantyMonths;
    private LocalDate returnDeadline;
    private String serialNumber;
    private PurchaseStatus status;
    private String notes;
}
