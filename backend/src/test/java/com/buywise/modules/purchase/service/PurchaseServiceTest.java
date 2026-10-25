package com.buywise.modules.purchase.service;

import com.buywise.common.security.UserPrincipal;
import com.buywise.modules.deadline.service.DeadlineService;
import com.buywise.modules.purchase.dto.CreatePurchaseRequest;
import com.buywise.modules.purchase.dto.PurchaseResponse;
import com.buywise.modules.purchase.dto.PurchaseStatsDto;
import com.buywise.modules.purchase.model.Purchase;
import com.buywise.modules.purchase.model.PurchaseStatus;
import com.buywise.modules.purchase.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Purchase Service — Ownership & Warranty Invariant Verification")
class PurchaseServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private DeadlineService deadlineService;

    @Mock
    private com.buywise.common.metrics.BuyWiseMetricsService metricsService;

    @InjectMocks
    private PurchaseService purchaseService;

    private UserPrincipal testUserPrincipal;

    @BeforeEach
    void setUp() {
        testUserPrincipal = new UserPrincipal(1L, "demo@buywise.com", "pass", "Karim Bennani", "MAD", Collections.emptyList());
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(testUserPrincipal, null, testUserPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Should register purchase and compute exact warranty expiration date: purchaseDate + warrantyMonths")
    void testRegisterPurchaseWarrantyCalculation() {
        LocalDate purchaseDate = LocalDate.of(2024, 1, 15);
        CreatePurchaseRequest request = CreatePurchaseRequest.builder()
                .productName("Samsung EcoBubble 8kg Washing Machine")
                .modelNumber("WW80TA046AX")
                .category("Major Appliances")
                .sellerName("Cosmos Electro")
                .purchaseDate(purchaseDate)
                .pricePaid(new BigDecimal("4299.00"))
                .currency("MAD")
                .warrantyMonths(24)
                .returnDeadline(purchaseDate.plusDays(14))
                .build();

        Purchase savedMock = Purchase.builder()
                .id(10L)
                .userId(1L)
                .productName(request.getProductName())
                .modelNumber(request.getModelNumber())
                .category(request.getCategory())
                .sellerName(request.getSellerName())
                .purchaseDate(purchaseDate)
                .pricePaid(request.getPricePaid())
                .currency("MAD")
                .warrantyMonths(24)
                .warrantyExpirationDate(purchaseDate.plusMonths(24))
                .returnDeadline(purchaseDate.plusDays(14))
                .status(PurchaseStatus.ACTIVE)
                .build();

        when(purchaseRepository.save(any(Purchase.class))).thenReturn(savedMock);

        PurchaseResponse response = purchaseService.registerPurchase(request);

        assertNotNull(response);
        assertEquals(LocalDate.of(2026, 1, 15), response.getWarrantyExpirationDate(),
                "Warranty expiration date must be exactly 2024-01-15 plus 24 months = 2026-01-15");
        verify(purchaseRepository).save(any(Purchase.class));
        verify(deadlineService).registerPurchaseDeadlines(savedMock);
    }

    @Test
    @DisplayName("Should calculate purchase library ownership metrics")
    void testGetPurchaseStats() {
        when(purchaseRepository.countByUserId(1L)).thenReturn(2L);
        when(purchaseRepository.sumTotalSpentByUserId(1L)).thenReturn(new BigDecimal("15789.00"));

        Purchase p1 = Purchase.builder()
                .id(1L)
                .pricePaid(new BigDecimal("4299.00"))
                .warrantyExpirationDate(LocalDate.now().plusMonths(18))
                .currency("MAD")
                .build();

        Purchase p2 = Purchase.builder()
                .id(2L)
                .pricePaid(new BigDecimal("11490.00"))
                .warrantyExpirationDate(LocalDate.now().plusDays(20)) // Expiring soon (<30 days)
                .currency("MAD")
                .build();

        when(purchaseRepository.findByUserIdOrderByPurchaseDateDesc(1L)).thenReturn(List.of(p1, p2));

        PurchaseStatsDto stats = purchaseService.getPurchaseStats();

        assertEquals(2L, stats.getTotalOwnedProducts());
        assertEquals(new BigDecimal("15789.00"), stats.getTotalExpenditure());
        assertEquals(2L, stats.getActiveWarrantiesCount());
        assertEquals(1L, stats.getExpiringSoonWarrantiesCount());
    }
}
