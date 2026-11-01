package com.buywise.modules.purchase.service;

import com.buywise.common.exception.ResourceNotFoundException;
import com.buywise.common.security.SecurityUtils;
import com.buywise.modules.deadline.service.DeadlineService;
import com.buywise.modules.purchase.dto.CreatePurchaseRequest;
import com.buywise.modules.purchase.dto.PurchaseResponse;
import com.buywise.modules.purchase.dto.PurchaseStatsDto;
import com.buywise.modules.purchase.dto.UpdatePurchaseRequest;
import com.buywise.modules.purchase.model.Purchase;
import com.buywise.modules.purchase.model.PurchaseStatus;
import com.buywise.modules.purchase.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * =================================================================================================
 * ARCHITECTURAL DESIGN NOTE: PURCHASE SERVICE & CROSS-MODULE EVENT PROPAGATION
 * =================================================================================================
 * Architectural Patterns & Transaction Flow:
 * 1. Synchronous Domain Coordination:
 *    - Upon registering a new purchase asset, the transaction coordinates:
 *      a) Saving the core asset to the `purchases` table.
 *      b) Invoking {@link DeadlineService#registerPurchaseDeadlines} to populate warranty and return deadlines.
 *    - Enclosed within an atomic @Transactional boundary; if deadline generation fails, the entire
 *      purchase registration rolls back to prevent inconsistent states.
 *
 * 2. Spring IoC & Metric Telemetry:
 *    - Injects {@link BuyWiseMetricsService} (Prometheus Micrometer facade) via constructor injection.
 *    - Automatically increments real-time counter metrics (`buywise_purchases_created_total`) on every
 *      successful registration, streaming live metrics to Grafana.
 * =================================================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final DeadlineService deadlineService;
    private final com.buywise.common.metrics.BuyWiseMetricsService metricsService;

    @Transactional
    public PurchaseResponse registerPurchase(CreatePurchaseRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        Purchase purchase = Purchase.builder()
                .userId(userId)
                .productName(request.getProductName().trim())
                .modelNumber(request.getModelNumber() != null ? request.getModelNumber().trim() : null)
                .category(request.getCategory() != null ? request.getCategory().trim() : null)
                .sellerName(request.getSellerName().trim())
                .purchaseDate(request.getPurchaseDate())
                .pricePaid(request.getPricePaid())
                .currency(request.getCurrency() != null ? request.getCurrency().toUpperCase().trim() : "MAD")
                .warrantyMonths(request.getWarrantyMonths() != null ? request.getWarrantyMonths() : 12)
                .returnDeadline(request.getReturnDeadline())
                .serialNumber(request.getSerialNumber() != null ? request.getSerialNumber().trim() : null)
                .status(PurchaseStatus.ACTIVE)
                .notes(request.getNotes())
                .build();

        purchase.calculateExpirationDate();
        Purchase saved = purchaseRepository.save(purchase);
        metricsService.incrementPurchaseCreated();
        log.info("Registered new purchase id: {} for user: {}", saved.getId(), userId);

        // Automatically register time-sensitive deadlines & reminders
        deadlineService.registerPurchaseDeadlines(saved);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PurchaseResponse> getUserPurchases() {
        Long userId = SecurityUtils.getCurrentUserId();
        return purchaseRepository.findByUserIdOrderByPurchaseDateDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PurchaseResponse getPurchaseById(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Purchase purchase = purchaseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase", "id", id));
        return mapToResponse(purchase);
    }

    @Transactional
    public PurchaseResponse updatePurchase(Long id, UpdatePurchaseRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Purchase purchase = purchaseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase", "id", id));

        if (request.getProductName() != null) purchase.setProductName(request.getProductName().trim());
        if (request.getModelNumber() != null) purchase.setModelNumber(request.getModelNumber().trim());
        if (request.getCategory() != null) purchase.setCategory(request.getCategory().trim());
        if (request.getSellerName() != null) purchase.setSellerName(request.getSellerName().trim());
        if (request.getPurchaseDate() != null) purchase.setPurchaseDate(request.getPurchaseDate());
        if (request.getPricePaid() != null) purchase.setPricePaid(request.getPricePaid());
        if (request.getCurrency() != null) purchase.setCurrency(request.getCurrency().toUpperCase().trim());
        if (request.getWarrantyMonths() != null) purchase.setWarrantyMonths(request.getWarrantyMonths());
        if (request.getReturnDeadline() != null) purchase.setReturnDeadline(request.getReturnDeadline());
        if (request.getSerialNumber() != null) purchase.setSerialNumber(request.getSerialNumber().trim());
        if (request.getStatus() != null) purchase.setStatus(request.getStatus());
        if (request.getNotes() != null) purchase.setNotes(request.getNotes());

        purchase.calculateExpirationDate();
        Purchase updated = purchaseRepository.save(purchase);

        // Update/ensure deadlines
        deadlineService.registerPurchaseDeadlines(updated);

        return mapToResponse(updated);
    }

    @Transactional
    public void deletePurchase(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Purchase purchase = purchaseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase", "id", id));

        purchaseRepository.delete(purchase);
        log.info("Deleted purchase id: {} for user: {}", id, userId);
    }

    @Transactional(readOnly = true)
    public PurchaseStatsDto getPurchaseStats() {
        Long userId = SecurityUtils.getCurrentUserId();
        long totalCount = purchaseRepository.countByUserId(userId);
        BigDecimal totalSpent = purchaseRepository.sumTotalSpentByUserId(userId);

        LocalDate now = LocalDate.now();
        List<Purchase> all = purchaseRepository.findByUserIdOrderByPurchaseDateDesc(userId);
        long activeWarranties = all.stream()
                .filter(p -> p.getWarrantyExpirationDate() != null && !p.getWarrantyExpirationDate().isBefore(now))
                .count();

        long expiringSoon = all.stream()
                .filter(p -> p.getWarrantyExpirationDate() != null
                        && !p.getWarrantyExpirationDate().isBefore(now)
                        && p.getWarrantyExpirationDate().isBefore(now.plusDays(30)))
                .count();

        String currency = all.isEmpty() ? "MAD" : all.get(0).getCurrency();

        return PurchaseStatsDto.builder()
                .totalOwnedProducts(totalCount)
                .totalExpenditure(totalSpent)
                .currency(currency)
                .activeWarrantiesCount(activeWarranties)
                .expiringSoonWarrantiesCount(expiringSoon)
                .build();
    }

    public PurchaseResponse mapToResponse(Purchase p) {
        LocalDate today = LocalDate.now();
        boolean isWarrantyActive = p.getWarrantyExpirationDate() != null && !p.getWarrantyExpirationDate().isBefore(today);
        long daysUntilExpiry = p.getWarrantyExpirationDate() != null ? ChronoUnit.DAYS.between(today, p.getWarrantyExpirationDate()) : 0;
        boolean isReturnActive = p.getReturnDeadline() != null && !p.getReturnDeadline().isBefore(today);

        return PurchaseResponse.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .productName(p.getProductName())
                .modelNumber(p.getModelNumber())
                .category(p.getCategory())
                .sellerName(p.getSellerName())
                .purchaseDate(p.getPurchaseDate())
                .pricePaid(p.getPricePaid())
                .currency(p.getCurrency())
                .warrantyMonths(p.getWarrantyMonths())
                .warrantyExpirationDate(p.getWarrantyExpirationDate())
                .returnDeadline(p.getReturnDeadline())
                .serialNumber(p.getSerialNumber())
                .status(p.getStatus())
                .notes(p.getNotes())
                .daysUntilWarrantyExpiry(daysUntilExpiry)
                .isWarrantyActive(isWarrantyActive)
                .isReturnWindowActive(isReturnActive)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
