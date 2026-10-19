package com.buywise.modules.comparison.strategy;

import com.buywise.modules.comparison.model.Offer;

import java.util.List;

/**
 * Strategy interface defining interchangeable algorithms for evaluating and ranking purchasing offers.
 * 
 * Demonstrates:
 * - Strategy Pattern (GoF): Encapsulates each comparison scoring algorithm in an isolated, interchangeable class.
 * - Open/Closed Principle (OCP): New comparison algorithms (e.g., EcoFriendlyStrategy, LowestShippingCostStrategy)
 *   can be plugged in without modifying the comparison engine or existing strategy code.
 * - Single Responsibility Principle (SRP): Each strategy is solely responsible for its specific ranking criteria.
 */
public interface ComparisonStrategy {

    StrategyType getStrategyType();

    String getDisplayName();

    String getDescription();

    /**
     * Deterministically scores and ranks the provided offers.
     *
     * @param offers List of offers to rank
     * @return Ordered list of scored results (rank 1 = best)
     */
    List<OfferScoreResult> evaluate(List<Offer> offers);
}
