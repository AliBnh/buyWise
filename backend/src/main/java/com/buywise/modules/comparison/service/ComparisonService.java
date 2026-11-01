package com.buywise.modules.comparison.service;

import com.buywise.common.exception.ResourceNotFoundException;
import com.buywise.common.exception.UnauthorizedException;
import com.buywise.common.security.SecurityUtils;
import com.buywise.modules.comparison.dto.*;
import com.buywise.modules.comparison.model.Comparison;
import com.buywise.modules.comparison.model.ComparisonStatus;
import com.buywise.modules.comparison.model.Offer;
import com.buywise.modules.comparison.repository.ComparisonRepository;
import com.buywise.modules.comparison.repository.OfferRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service managing purchase comparisons and seller offers with strict user data isolation.
 * 
 * Demonstrates:
 * - Domain-Driven Design (DDD): Comparison acts as the Aggregate Root coordinating associated Offers.
 * - Resource Ownership Verification: Enforces that users cannot inspect, modify, or delete another user's comparison data.
 * - Transaction Management: Ensures atomic updates to comparison aggregates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComparisonService {

    private final ComparisonRepository comparisonRepository;
    private final OfferRepository offerRepository;
    private final ComparisonEngineService comparisonEngineService;
    private final ObjectMapper objectMapper;

    @Transactional
    public ComparisonResponse createComparison(CreateComparisonRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        Comparison comparison = Comparison.builder()
                .userId(currentUserId)
                .title(request.getTitle().trim())
                .categoryName(request.getCategoryName().trim())
                .notes(request.getNotes())
                .status(ComparisonStatus.DRAFT)
                .build();

        if (request.getInitialOffers() != null) {
            for (CreateOfferRequest offerReq : request.getInitialOffers()) {
                Offer offer = buildOfferFromRequest(offerReq);
                comparison.addOffer(offer);
            }
        }

        Comparison saved = comparisonRepository.save(comparison);
        log.info("Created comparison id: {} for user: {}", saved.getId(), currentUserId);
        return mapToResponse(saved, true);
    }

    @Transactional(readOnly = true)
    public List<ComparisonResponse> getUserComparisons() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return comparisonRepository.findByUserIdOrderByCreatedAtDesc(currentUserId).stream()
                .map(c -> mapToResponse(c, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ComparisonResponse getComparisonById(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Comparison comparison = comparisonRepository.findByIdAndUserIdWithOffers(id, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Comparison", "id", id));

        return mapToResponse(comparison, true);
    }

    @Transactional
    public ComparisonResponse updateComparison(Long id, UpdateComparisonRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Comparison comparison = comparisonRepository.findByIdAndUserId(id, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Comparison", "id", id));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            comparison.setTitle(request.getTitle().trim());
        }
        if (request.getCategoryName() != null && !request.getCategoryName().isBlank()) {
            comparison.setCategoryName(request.getCategoryName().trim());
        }
        if (request.getNotes() != null) {
            comparison.setNotes(request.getNotes());
        }
        if (request.getStatus() != null) {
            comparison.setStatus(request.getStatus());
        }

        Comparison updated = comparisonRepository.save(comparison);
        return mapToResponse(updated, true);
    }

    @Transactional
    public void deleteComparison(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Comparison comparison = comparisonRepository.findByIdAndUserId(id, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Comparison", "id", id));

        comparisonRepository.delete(comparison);
        log.info("Deleted comparison id: {} for user: {}", id, currentUserId);
    }

    @Transactional
    public ComparisonResponse addOffer(Long comparisonId, CreateOfferRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Comparison comparison = comparisonRepository.findByIdAndUserId(comparisonId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Comparison", "id", comparisonId));

        Offer offer = buildOfferFromRequest(request);
        comparison.addOffer(offer);

        Comparison saved = comparisonRepository.save(comparison);
        return mapToResponse(saved, true);
    }

    @Transactional
    public ComparisonResponse updateOffer(Long comparisonId, Long offerId, UpdateOfferRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Comparison comparison = comparisonRepository.findByIdAndUserId(comparisonId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Comparison", "id", comparisonId));

        Offer offer = offerRepository.findByIdAndComparisonId(offerId, comparisonId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer", "id", offerId));

        if (request.getTitle() != null) offer.setTitle(request.getTitle().trim());
        if (request.getSellerName() != null) offer.setSellerName(request.getSellerName().trim());
        if (request.getSellerId() != null) offer.setSellerId(request.getSellerId());
        if (request.getProductModel() != null) offer.setProductModel(request.getProductModel().trim());
        if (request.getSourceUrl() != null) offer.setSourceUrl(request.getSourceUrl());
        if (request.getCurrency() != null) offer.setCurrency(request.getCurrency().trim().toUpperCase());
        if (request.getBasePrice() != null) offer.setBasePrice(request.getBasePrice());
        if (request.getDeliveryFee() != null) offer.setDeliveryFee(request.getDeliveryFee());
        if (request.getAdditionalFees() != null) offer.setAdditionalFees(request.getAdditionalFees());
        if (request.getDiscountAmount() != null) offer.setDiscountAmount(request.getDiscountAmount());
        if (request.getWarrantyMonths() != null) offer.setWarrantyMonths(request.getWarrantyMonths());
        if (request.getReturnWindowDays() != null) offer.setReturnWindowDays(request.getReturnWindowDays());
        if (request.getWarrantyConditions() != null) offer.setWarrantyConditions(request.getWarrantyConditions());
        if (request.getReturnPolicy() != null) offer.setReturnPolicy(request.getReturnPolicy());

        if (request.getSpecifications() != null) {
            try {
                offer.setSpecificationsJson(objectMapper.writeValueAsString(request.getSpecifications()));
            } catch (Exception e) {
                log.warn("Failed to serialize specifications", e);
            }
        } else if (request.getSpecificationsJson() != null) {
            offer.setSpecificationsJson(request.getSpecificationsJson());
        }

        offer.recalculateTotalPrice();
        offerRepository.save(offer);

        return mapToResponse(comparison, true);
    }

    @Transactional
    public ComparisonResponse deleteOffer(Long comparisonId, Long offerId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Comparison comparison = comparisonRepository.findByIdAndUserId(comparisonId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Comparison", "id", comparisonId));

        Offer offer = offerRepository.findByIdAndComparisonId(offerId, comparisonId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer", "id", offerId));

        comparison.removeOffer(offer);
        comparisonRepository.save(comparison);

        return mapToResponse(comparison, true);
    }

    private Offer buildOfferFromRequest(CreateOfferRequest request) {
        String specsJson = request.getSpecificationsJson();
        if ((specsJson == null || specsJson.isBlank()) && request.getSpecifications() != null) {
            try {
                specsJson = objectMapper.writeValueAsString(request.getSpecifications());
            } catch (Exception e) {
                log.warn("Failed to serialize specs", e);
            }
        }

        Offer offer = Offer.builder()
                .sellerName(request.getSellerName().trim())
                .sellerId(request.getSellerId())
                .title(request.getTitle().trim())
                .productModel(request.getProductModel())
                .sourceUrl(request.getSourceUrl())
                .currency(request.getCurrency() != null ? request.getCurrency().toUpperCase().trim() : "MAD")
                .basePrice(request.getBasePrice())
                .deliveryFee(request.getDeliveryFee() != null ? request.getDeliveryFee() : BigDecimal.ZERO)
                .additionalFees(request.getAdditionalFees() != null ? request.getAdditionalFees() : BigDecimal.ZERO)
                .discountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO)
                .warrantyMonths(request.getWarrantyMonths() != null ? request.getWarrantyMonths() : 12)
                .returnWindowDays(request.getReturnWindowDays() != null ? request.getReturnWindowDays() : 14)
                .specificationsJson(specsJson)
                .warrantyConditions(request.getWarrantyConditions())
                .returnPolicy(request.getReturnPolicy())
                .build();

        offer.recalculateTotalPrice();
        return offer;
    }

    public ComparisonResponse mapToResponse(Comparison comparison, boolean includeSummary) {
        List<OfferResponse> offerResponses = comparison.getOffers() != null
                ? comparison.getOffers().stream().map(this::mapOfferToResponse).collect(Collectors.toList())
                : List.of();

        DeterministicComparisonSummaryDto summary = null;
        if (includeSummary && comparison.getOffers() != null) {
            summary = comparisonEngineService.calculateSummary(comparison.getOffers());
        }

        return ComparisonResponse.builder()
                .id(comparison.getId())
                .userId(comparison.getUserId())
                .title(comparison.getTitle())
                .categoryName(comparison.getCategoryName())
                .status(comparison.getStatus())
                .notes(comparison.getNotes())
                .aiAnalysisJson(comparison.getAiAnalysisJson())
                .aiAnalyzedAt(comparison.getAiAnalyzedAt())
                .offers(offerResponses)
                .deterministicSummary(summary)
                .createdAt(comparison.getCreatedAt())
                .updatedAt(comparison.getUpdatedAt())
                .build();
    }

    private OfferResponse mapOfferToResponse(Offer offer) {
        Map<String, Object> specsMap = null;
        if (offer.getSpecificationsJson() != null && !offer.getSpecificationsJson().isBlank()) {
            try {
                specsMap = objectMapper.readValue(offer.getSpecificationsJson(), new TypeReference<>() {});
            } catch (Exception ignored) {}
        }

        return OfferResponse.builder()
                .id(offer.getId())
                .comparisonId(offer.getComparison() != null ? offer.getComparison().getId() : null)
                .sellerName(offer.getSellerName())
                .sellerId(offer.getSellerId())
                .title(offer.getTitle())
                .productModel(offer.getProductModel())
                .sourceUrl(offer.getSourceUrl())
                .currency(offer.getCurrency())
                .basePrice(offer.getBasePrice())
                .deliveryFee(offer.getDeliveryFee())
                .additionalFees(offer.getAdditionalFees())
                .discountAmount(offer.getDiscountAmount())
                .totalPrice(offer.getTotalPrice())
                .warrantyMonths(offer.getWarrantyMonths())
                .returnWindowDays(offer.getReturnWindowDays())
                .specifications(specsMap)
                .specificationsJson(offer.getSpecificationsJson())
                .warrantyConditions(offer.getWarrantyConditions())
                .returnPolicy(offer.getReturnPolicy())
                .isRecommended(offer.getIsRecommended())
                .valueScore(offer.getValueScore())
                .createdAt(offer.getCreatedAt())
                .updatedAt(offer.getUpdatedAt())
                .build();
    }
}
