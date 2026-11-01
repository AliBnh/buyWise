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
 * =================================================================================================
 * ARCHITECTURAL DESIGN NOTE: TRANSACTION MANAGEMENT (ACID) & SPRING IOC CONSTRUCTOR INJECTION
 * =================================================================================================
 * Core Architectural Invariants:
 * 1. ACID Transaction Guarantees (@Transactional):
 *    - Atomicity: Comparison aggregates and their associated offers are persisted or rolled back
 *      together as a single atomic unit of work. If an offer fails validation, no dirty records persist.
 *    - Consistency: Enforces relational BCNF constraints and entity-level invariants before commit.
 *    - Isolation: Default Read Committed isolation in PostgreSQL prevents dirty reads. Read-only queries
 *      use `@Transactional(readOnly = true)` which optimizes connection pooling, disables Hibernate
 *      dirty-checking snapshot overhead, and prevents accidental flush operations.
 *    - Durability: Changes committed through the EntityManager are flushed to PostgreSQL's
 *      Write-Ahead Log (WAL), surviving unexpected server restarts.
 *
 * 2. Dependency Injection & Spring IoC Best Practices:
 *    - Uses Constructor Injection via Lombok's `@RequiredArgsConstructor` on `private final` fields.
 *    - Why Constructor Injection over @Autowired on fields?
 *      a) Immutability: Dependencies are marked `final` and cannot be modified post-construction.
 *      b) Fail-Fast: Guarantees that the service cannot be instantiated in a half-initialized state.
 *      c) Pure Unit Testability: Classes can be tested with Mockito in plain Java without spinning up
 *         the expensive Spring ApplicationContext or relying on reflection (`ReflectionTestUtils`).
 *      d) Circular Dependency Detection: Circular bean dependencies are exposed immediately at startup.
 *
 * 3. Multi-Tenant Security & IDOR Elimination (OWASP API Top 10):
 *    - Insecure Direct Object Reference (IDOR) is eliminated by binding every query to the caller:
 *      {@code comparisonRepository.findByIdAndUserId(id, currentUserId)}.
 *    - Even if a malicious tenant guesses an integer ID belonging to another user, the database
 *      returns Optional.empty(), which translates to HTTP 404 (or 403), preventing data leaks.
 *
 * Technical Interview Q&A Crib Sheet:
 * Q: "Why use @Transactional(readOnly = true) on query methods?"
 * A: It yields three major performance and safety benefits:
 *    1. Memory: Hibernate skips creation of entity state snapshots for dirty-checking.
 *    2. CPU: Session flush is disabled upon transaction completion.
 *    3. Infrastructure: In cluster topologies with read-replicas, Spring routes readOnly queries
 *       to replica nodes, freeing the primary write master for mutations.
 * =================================================================================================
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
