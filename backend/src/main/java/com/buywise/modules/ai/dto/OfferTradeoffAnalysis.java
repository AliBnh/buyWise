package com.buywise.modules.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferTradeoffAnalysis {

    private Long offerId;
    private String sellerName;
    private List<String> strengths;
    private List<String> weaknesses;
    private String warrantyAssessment;
    private String returnPolicyAssessment;
    private List<String> sellerCredibilityNotes;
    private List<String> itemsToVerifyBeforeBuying;
}
