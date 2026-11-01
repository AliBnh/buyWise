package com.buywise.modules.purchase.dto;

import com.buywise.modules.purchase.model.PurchaseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseResponse {

    private Long id;
    private Long userId;
    private String productName;
    private String modelNumber;
    private String category;
    private String sellerName;
    private LocalDate purchaseDate;
    private BigDecimal pricePaid;
    private String currency;
    private Integer warrantyMonths;
    private LocalDate warrantyExpirationDate;
    private LocalDate returnDeadline;
    private String serialNumber;
    private PurchaseStatus status;
    private String notes;
    private long daysUntilWarrantyExpiry;
    private boolean isWarrantyActive;
    private boolean isReturnWindowActive;
    private Instant createdAt;
    private Instant updatedAt;
}
