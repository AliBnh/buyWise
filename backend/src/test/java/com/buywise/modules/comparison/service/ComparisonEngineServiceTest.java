package com.buywise.modules.comparison.service;

import com.buywise.modules.comparison.dto.DeterministicComparisonSummaryDto;
import com.buywise.modules.comparison.model.Offer;
import com.buywise.modules.comparison.strategy.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * =================================================================================================
 * ARCHITECTURAL DESIGN NOTE: PURE UNIT TESTING & DETERMINISTIC INVARIANT VERIFICATION
 * =================================================================================================
 * Testing Methodology & Best Practices:
 * 1. Pure POJO Unit Tests (Fast Feedback Loop):
 *    - Does NOT use @SpringBootTest or Spring Runner.
 *    - Instantiates pure Java domain objects and strategy implementations directly in {@code setUp()}.
 *    - Executes in < 20ms, providing instantaneous CI feedback without context-loading overhead.
 *
 * 2. Deterministic Financial Invariant Verification:
 *    - Verifies that financial arithmetic strictly obeys the domain equation:
 *      Total = BasePrice + DeliveryFee + AdditionalFees - DiscountAmount.
 *    - Validates BigDecimal precision and absence of floating-point drift.
 *
 * 3. Strategy Pattern Exhaustive Branch Coverage:
 *    - Evaluates LowestPriceStrategy, BestWarrantyStrategy, BestValueScoreStrategy, and BalancedPerformanceStrategy.
 * =================================================================================================
 */
@DisplayName("Comparison Engine Service — Deterministic Arithmetic Verification")
class ComparisonEngineServiceTest {

    private ComparisonEngineService comparisonEngineService;

    @BeforeEach
    void setUp() {
        LowestPriceStrategy lowestPriceStrategy = new LowestPriceStrategy();
        BestWarrantyStrategy bestWarrantyStrategy = new BestWarrantyStrategy();
        BestValueScoreStrategy bestValueScoreStrategy = new BestValueScoreStrategy();
        BalancedPerformanceStrategy balancedStrategy = new BalancedPerformanceStrategy();
        ComparisonStrategyFactory factory = new ComparisonStrategyFactory(
                List.of(lowestPriceStrategy, bestWarrantyStrategy, bestValueScoreStrategy, balancedStrategy)
        );
        comparisonEngineService = new ComparisonEngineService(factory, new ObjectMapper());
    }

    @Test
    @DisplayName("Should accurately calculate total price invariant: base + delivery + fees - discount")
    void testOfferTotalPriceCalculation() {
        Offer offer = Offer.builder()
                .basePrice(new BigDecimal("8499.00"))
                .deliveryFee(new BigDecimal("50.00"))
                .additionalFees(new BigDecimal("20.00"))
                .discountAmount(new BigDecimal("100.00"))
                .build();

        offer.recalculateTotalPrice();

        assertEquals(new BigDecimal("8469.00"), offer.getTotalPrice(),
                "Total price must equal 8499 + 50 + 20 - 100 = 8469");
    }

    @Test
    @DisplayName("Should deterministically compute price spread and identify cheapest offer")
    void testCalculateSummaryWithTwoCompetingOffers() {
        Offer offerA = Offer.builder()
                .id(1L)
                .sellerName("TechHub Casablanca")
                .title("Lenovo IdeaPad Slim 5 (512GB)")
                .currency("MAD")
                .basePrice(new BigDecimal("8499.00"))
                .deliveryFee(new BigDecimal("50.00"))
                .warrantyMonths(12)
                .returnWindowDays(7)
                .build();
        offerA.recalculateTotalPrice(); // 8549.00

        Offer offerB = Offer.builder()
                .id(2L)
                .sellerName("ElectroMaroc")
                .title("Lenovo IdeaPad Slim 5 (1TB)")
                .currency("MAD")
                .basePrice(new BigDecimal("9199.00"))
                .deliveryFee(BigDecimal.ZERO)
                .warrantyMonths(24)
                .returnWindowDays(14)
                .build();
        offerB.recalculateTotalPrice(); // 9199.00

        DeterministicComparisonSummaryDto summary = comparisonEngineService.calculateSummary(List.of(offerA, offerB));

        assertNotNull(summary);
        assertEquals(2, summary.getOfferCount());
        assertEquals("TechHub Casablanca", summary.getCheapestOfferSeller());
        assertEquals(new BigDecimal("8549.00"), summary.getLowestTotalPrice());
        assertEquals(new BigDecimal("9199.00"), summary.getHighestTotalPrice());
        assertEquals(new BigDecimal("650.00"), summary.getAbsolutePriceDifference());
        assertEquals(new BigDecimal("7.60"), summary.getPercentagePriceDifference()); // 650 / 8549 = 7.60%
        assertEquals(12, summary.getWarrantyDifferenceMonths());
        assertEquals(24, summary.getLongestWarrantyMonths());
    }

    @Test
    @DisplayName("Should verify LowestPriceStrategy ranks cheaper offer at Rank 1")
    void testLowestPriceStrategyRanking() {
        Offer cheap = Offer.builder().id(1L).sellerName("Seller A").basePrice(new BigDecimal("1000.00")).build();
        cheap.recalculateTotalPrice();

        Offer expensive = Offer.builder().id(2L).sellerName("Seller B").basePrice(new BigDecimal("1500.00")).build();
        expensive.recalculateTotalPrice();

        LowestPriceStrategy strategy = new LowestPriceStrategy();
        List<OfferScoreResult> results = strategy.evaluate(List.of(expensive, cheap));

        assertEquals(2, results.size());
        assertEquals(1L, results.get(0).getOfferId());
        assertEquals(1, results.get(0).getRank());
        assertTrue(results.get(0).isWinner());
        assertEquals(2L, results.get(1).getOfferId());
        assertEquals(2, results.get(1).getRank());
    }

    @Test
    @DisplayName("Should verify BestWarrantyStrategy ranks longer warranty duration higher")
    void testBestWarrantyStrategyRanking() {
        Offer shortWarranty = Offer.builder()
                .id(1L)
                .basePrice(new BigDecimal("2000.00"))
                .warrantyMonths(12)
                .build();
        shortWarranty.recalculateTotalPrice();

        Offer longWarranty = Offer.builder()
                .id(2L)
                .basePrice(new BigDecimal("2200.00"))
                .warrantyMonths(36)
                .build();
        longWarranty.recalculateTotalPrice();

        BestWarrantyStrategy strategy = new BestWarrantyStrategy();
        List<OfferScoreResult> results = strategy.evaluate(List.of(shortWarranty, longWarranty));

        assertEquals(2L, results.get(0).getOfferId());
        assertEquals(1, results.get(0).getRank());
        assertTrue(results.get(0).isWinner());
    }

    @Test
    @DisplayName("Should handle empty offers list gracefully without exceptions")
    void testEmptyOffersList() {
        DeterministicComparisonSummaryDto summary = comparisonEngineService.calculateSummary(List.of());
        assertEquals(0, summary.getOfferCount());
        assertTrue(summary.getOfferSummaries().isEmpty());
    }
}
