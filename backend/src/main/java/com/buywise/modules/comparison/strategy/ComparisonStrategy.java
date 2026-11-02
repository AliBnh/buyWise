package com.buywise.modules.comparison.strategy;

import com.buywise.modules.comparison.model.Offer;

import java.util.List;

/**
 * ==============================================================================================
 * STRATEGY PATTERN CONTRACT (GoF) — PURCHASE COMPARISON ALGORITHMS
 * ==============================================================================================
 * 
 * DESIGN PATTERNS APPLIED:
 * 1. Strategy Pattern (GoF):
 *    - Encapsulates ranking algorithms into interchangeable objects sharing a uniform contract.
 *    - Eliminates conditional complexity: Replaces monolithic `if (strategy.equals("PRICE")) ... else if ...`
 *      blocks with clean object-oriented polymorphism.
 *
 * SOLID PRINCIPLES APPLIED:
 * 1. Open/Closed Principle (OCP):
 *    - The comparison subsystem is open for extension (any developer can add new algorithms)
 *      but closed for modification (no existing evaluation code needs alterations).
 *
 * 2. Liskov Substitution Principle (LSP):
 *    - Any concrete implementation of {@link ComparisonStrategy} can seamlessly replace any other
 *      implementation at runtime without altering the correctness or stability of the caller.
 *
 * 3. Interface Segregation Principle (ISP):
 *    - Keeps the contract lean and cohesive: defines only essential methods required for ranking
 *      and self-description (`getStrategyType()`, `evaluate()`). No client is forced to implement
 *      unused lifecycle or persistence methods.
 *
 * 4. Single Responsibility Principle (SRP):
 *    - Focuses strictly on the mathematical or logical evaluation and ordering of offers.
 *
 * ARCHITECTURAL VALUE & TRANSPARENCY:
 * Q: "Why is the Strategy pattern critical in an AI-powered purchase intelligence platform?"
 * A: Because purchase decisions are multi-dimensional. A consumer buying a refrigerator prioritizes
 *    long warranties, while a consumer buying an adapter prioritizes lowest immediate price.
 *    The Strategy Pattern gives the user transparent agency over how offers are scored without
 *    relying on an opaque LLM prompt that could hallucinate rankings.
 * ==============================================================================================
 */
public interface ComparisonStrategy {

    /**
     * Identifies the enum strategy token.
     */
    StrategyType getStrategyType();

    /**
     * User-facing readable title for UI display.
     */
    String getDisplayName();

    /**
     * Explanatory rationale describing how the score is weighted.
     */
    String getDescription();

    /**
     * Deterministically scores and ranks the provided offers.
     * Guaranteed Invariant: Rank 1 corresponds to the top recommended offer.
     *
     * @param offers List of offers to rank
     * @return Ordered list of scored results (sorted from best to worst)
     */
    List<OfferScoreResult> evaluate(List<Offer> offers);
}
