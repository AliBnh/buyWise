package com.buywise.modules.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerDto {
    private Long id;
    private String name;
    private String websiteUrl;
    private BigDecimal reputationScore;
    private String returnPolicySummary;
    private Boolean isVerified;
}
