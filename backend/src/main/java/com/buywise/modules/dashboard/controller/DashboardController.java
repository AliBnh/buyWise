package com.buywise.modules.dashboard.controller;

import com.buywise.common.security.SecurityUtils;
import com.buywise.common.security.UserPrincipal;
import com.buywise.modules.comparison.service.ComparisonService;
import com.buywise.modules.dashboard.dto.DashboardOverviewDto;
import com.buywise.modules.deadline.service.DeadlineService;
import com.buywise.modules.purchase.dto.PurchaseStatsDto;
import com.buywise.modules.purchase.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

/**
 * REST controller consolidating multi-domain telemetry for the main application landing view.
 * 
 * Demonstrates:
 * - API Gateway / Backend-for-Frontend (BFF) Pattern: Aggregates data from Purchase, Comparison,
 *   and Deadline bounded contexts into a single unified JSON response, preventing HTTP waterfall
 *   round-trips on initial page render.
 * - Least Privilege / Multi-Tenancy: Scopes all aggregated sub-queries strictly to the authenticated user ID.
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Consolidated user metrics, recent activities, and upcoming alert summaries")
public class DashboardController {

    private final PurchaseService purchaseService;
    private final ComparisonService comparisonService;
    private final DeadlineService deadlineService;

    /**
     * Gathers high-level summary statistics, recent comparisons, owned assets, and imminent expiration alerts.
     * 
     * @return HTTP 200 OK with fully composed {@link DashboardOverviewDto}.
     */
    @GetMapping("/overview")
    @Operation(summary = "Get high-level dashboard metrics for the authenticated user")
    public ResponseEntity<DashboardOverviewDto> getOverview() {
        UserPrincipal user = SecurityUtils.getCurrentUser();
        PurchaseStatsDto stats = purchaseService.getPurchaseStats();

        // Fetches top 3 recent comparisons to minimize payload weight
        var recentComparisons = comparisonService.getUserComparisons().stream()
                .limit(3)
                .collect(Collectors.toList());

        // Fetches top 3 recent purchases
        var recentPurchases = purchaseService.getUserPurchases().stream()
                .limit(3)
                .collect(Collectors.toList());

        // Gathers top 5 imminent deadlines occurring within the next 30 days
        var upcomingDeadlines = deadlineService.getUpcomingDeadlines(30).stream()
                .limit(5)
                .collect(Collectors.toList());

        // Gathers pending notifications requiring consumer attention
        var activeReminders = deadlineService.getActiveReminders();

        DashboardOverviewDto overview = DashboardOverviewDto.builder()
                .userFullName(user.getFullName())
                .preferredCurrency(user.getPreferredCurrency())
                .totalPurchasesCount(stats.getTotalOwnedProducts())
                .totalExpenditureAmount(stats.getTotalExpenditure())
                .activeComparisonsCount(recentComparisons.size())
                .activeWarrantiesCount(stats.getActiveWarrantiesCount())
                .expiringSoonWarrantiesCount(stats.getExpiringSoonWarrantiesCount())
                .upcomingDeadlines(upcomingDeadlines)
                .activeReminders(activeReminders)
                .recentComparisons(recentComparisons)
                .recentPurchases(recentPurchases)
                .build();

        return ResponseEntity.ok(overview);
    }
}
