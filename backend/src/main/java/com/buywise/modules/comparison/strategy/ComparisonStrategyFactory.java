package com.buywise.modules.comparison.strategy;

import com.buywise.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * ==============================================================================================
 * FACTORY PATTERN + SPRING IoC DEPENDENCY INJECTION ARCHITECTURE
 * ==============================================================================================
 * 
 * DESIGN PATTERNS APPLIED:
 * 1. Factory Pattern (GoF):
 *    - Problem: Clients needing to rank offers shouldn't directly instantiate concrete strategy
 *      classes (`new LowestPriceStrategy()`), which would create tight coupling.
 *    - Solution: This factory acts as a single point of dispatch, mapping incoming {@link StrategyType}
 *      tokens (from client API requests) to the appropriate pre-instantiated strategy instance.
 *
 * 2. Strategy Pattern (GoF):
 *    - In concert with {@link ComparisonStrategy}, allows the comparison engine to switch ranking
 *      criteria dynamically at runtime based on consumer preference (Price vs. Warranty vs. Value).
 *
 * SOLID PRINCIPLES APPLIED:
 * 1. Open/Closed Principle (OCP):
 *    - Open for Extension: Creating a new ranking algorithm (e.g., `BrandReputationStrategy`)
 *      only requires authoring a new `@Component` class implementing `ComparisonStrategy`.
 *    - Closed for Modification: This factory does NOT need to be edited to support new strategies.
 *      Spring automatically discovers and injects any new bean implementing the interface.
 *
 * 2. Dependency Inversion Principle (DIP):
 *    - The factory and consumer services depend exclusively on the abstract interface
 *      {@link ComparisonStrategy}, not on concrete implementations like `LowestPriceStrategy`.
 *
 * 3. Single Responsibility Principle (SRP):
 *    - Sole responsibility is mapping `StrategyType` keys to `ComparisonStrategy` beans and managing
 *      their lifecycle in an O(1) lookup map. It performs zero arithmetic or ranking calculations.
 *
 * COMMON TECHNICAL INTERVIEW QUESTIONS & ANSWERS:
 * Q1: "How does Spring automatically populate the `List<ComparisonStrategy> strategies` parameter?"
 * A1: During ApplicationContext initialization, Spring's BeanFactory discovers all managed beans
 *     implementing `ComparisonStrategy`. When resolving constructor dependencies for this factory,
 *     Spring inspects the generic type parameter `List<ComparisonStrategy>` and automatically
 *     collects all matching beans into an unmodifiable collection.
 *
 * Q2: "Why use an EnumMap instead of a standard HashMap?"
 * A2: `EnumMap` is an extremely high-performance map specialized for enum keys. Internally, it is
 *     backed by a compact Java array indexed by the enum's `ordinal()`. This yields zero hashing
 *     overhead, zero collision resolution, lower memory footprint, and guaranteed O(1) retrieval.
 * ==============================================================================================
 */
@Component
public class ComparisonStrategyFactory {

    private final Map<StrategyType, ComparisonStrategy> strategyMap = new EnumMap<>(StrategyType.class);

    /**
     * Constructor Injection (Spring IoC best practice):
     * Spring automatically collects and injects all managed beans implementing {@link ComparisonStrategy}.
     *
     * @param strategies All active strategy beans discovered during classpath component scanning
     */
    public ComparisonStrategyFactory(List<ComparisonStrategy> strategies) {
        for (ComparisonStrategy strategy : strategies) {
            strategyMap.put(strategy.getStrategyType(), strategy);
        }
    }

    /**
     * Resolves a strategy instance in O(1) constant time based on client preference.
     *
     * @param type The strategy type selected by the client (e.g. LOWEST_PRICE, BEST_WARRANTY)
     * @return The concrete strategy bean
     * @throws BusinessException If an unknown or unmapped strategy type is requested
     */
    public ComparisonStrategy getStrategy(StrategyType type) {
        ComparisonStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new BusinessException("Unsupported comparison strategy type: " + type);
        }
        return strategy;
    }

    /**
     * Returns all available strategies for frontend dropdowns and capability discovery.
     */
    public List<ComparisonStrategy> getAllStrategies() {
        return List.copyOf(strategyMap.values());
    }
}
