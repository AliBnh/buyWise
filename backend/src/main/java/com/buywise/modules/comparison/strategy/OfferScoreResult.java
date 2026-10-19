package com.buywise.modules.comparison.strategy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Result of evaluating an offer under a specific evaluation strategy.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferScoreResult {

    private Long offerId;
    private String offerTitle;
    private String sellerName;
    private BigDecimal score; // 0.0 - 100.0
    private int rank;
    private String rationale;
    private boolean isWinner;
}
