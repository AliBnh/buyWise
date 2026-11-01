package com.buywise.modules.dashboard.dto;

import com.buywise.modules.comparison.dto.ComparisonResponse;
import com.buywise.modules.deadline.dto.DeadlineDto;
import com.buywise.modules.deadline.dto.ReminderDto;
import com.buywise.modules.purchase.dto.PurchaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Consolidated dashboard metrics and summary overview.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewDto {

    private String userFullName;
    private String preferredCurrency;
    private long totalPurchasesCount;
    private BigDecimal totalExpenditureAmount;
    private long activeComparisonsCount;
    private long activeWarrantiesCount;
    private long expiringSoonWarrantiesCount;
    private List<DeadlineDto> upcomingDeadlines;
    private List<ReminderDto> activeReminders;
    private List<ComparisonResponse> recentComparisons;
    private List<PurchaseResponse> recentPurchases;
}
