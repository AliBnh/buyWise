package com.buywise.modules.dashboard.controller;

import com.buywise.common.security.UserPrincipal;
import com.buywise.modules.comparison.service.ComparisonService;
import com.buywise.modules.dashboard.dto.DashboardOverviewDto;
import com.buywise.modules.deadline.service.DeadlineService;
import com.buywise.modules.purchase.dto.PurchaseStatsDto;
import com.buywise.modules.purchase.service.PurchaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private PurchaseService purchaseService;

    @Mock
    private ComparisonService comparisonService;

    @Mock
    private DeadlineService deadlineService;

    @InjectMocks
    private DashboardController dashboardController;

    @BeforeEach
    void setUp() {
        UserPrincipal userPrincipal = new UserPrincipal(
                1L, "demo@buywise.com", "pass", "Karim Bennani", "MAD",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Should aggregate dashboard metrics and return complete overview")
    void shouldReturnDashboardOverview() {
        PurchaseStatsDto stats = PurchaseStatsDto.builder()
                .totalOwnedProducts(6)
                .totalExpenditure(new BigDecimal("51967.00"))
                .activeWarrantiesCount(5)
                .expiringSoonWarrantiesCount(1)
                .build();

        when(purchaseService.getPurchaseStats()).thenReturn(stats);
        when(comparisonService.getUserComparisons()).thenReturn(Collections.emptyList());
        when(purchaseService.getUserPurchases()).thenReturn(Collections.emptyList());
        when(deadlineService.getUpcomingDeadlines(30)).thenReturn(Collections.emptyList());
        when(deadlineService.getActiveReminders()).thenReturn(Collections.emptyList());

        ResponseEntity<DashboardOverviewDto> response = dashboardController.getOverview();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        DashboardOverviewDto body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getUserFullName()).isEqualTo("Karim Bennani");
        assertThat(body.getPreferredCurrency()).isEqualTo("MAD");
        assertThat(body.getTotalPurchasesCount()).isEqualTo(6);
        assertThat(body.getTotalExpenditureAmount()).isEqualTo(new BigDecimal("51967.00"));
        assertThat(body.getActiveWarrantiesCount()).isEqualTo(5);
        assertThat(body.getExpiringSoonWarrantiesCount()).isEqualTo(1);
    }
}
