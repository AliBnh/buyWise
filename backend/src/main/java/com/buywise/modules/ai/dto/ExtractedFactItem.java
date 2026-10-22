package com.buywise.modules.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractedFactItem {

    private String factKey;
    private String extractedValue;
    private String sourceExcerpt;
    private Integer pageNumber;
    private BigDecimal confidenceScore;
}
