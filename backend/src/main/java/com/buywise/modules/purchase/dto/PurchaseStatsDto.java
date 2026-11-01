package com.buywise.modules.purchase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseStatsDto {

    private long totalOwnedProducts;
    private BigDecimal totalExpenditure;
    private String currency;
    private long activeWarrantiesCount;
    private long expiringSoonWarrantiesCount;
}
