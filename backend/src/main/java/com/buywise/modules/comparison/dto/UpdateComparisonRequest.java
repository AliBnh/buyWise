package com.buywise.modules.comparison.dto;

import com.buywise.modules.comparison.model.ComparisonStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateComparisonRequest {

    private String title;
    private String categoryName;
    private String notes;
    private ComparisonStatus status;
}
