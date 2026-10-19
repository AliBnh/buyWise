package com.buywise.modules.comparison.dto;

import com.buywise.modules.comparison.model.ComparisonStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonResponse {

    private Long id;
    private Long userId;
    private String title;
    private String categoryName;
    private ComparisonStatus status;
    private String notes;
    private String aiAnalysisJson;
    private Instant aiAnalyzedAt;
    private List<OfferResponse> offers;
    private DeterministicComparisonSummaryDto deterministicSummary;
    private Instant createdAt;
    private Instant updatedAt;
}
