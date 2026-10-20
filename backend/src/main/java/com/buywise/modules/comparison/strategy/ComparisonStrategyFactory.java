package com.buywise.modules.comparison.strategy;

import com.buywise.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Factory for resolving comparison evaluation strategies.
 * 
 * Demonstrates:
 * - Factory Pattern (GoF): Centralizes dynamic resolution of strategy instances based on runtime selection.
 * - Dependency Injection: Spring automatically injects all beans implementing {@link ComparisonStrategy} into the constructor list.
 * - Open/Closed Principle (OCP): New strategies are automatically discovered and registered without modifying this factory.
 */
@Component
public class ComparisonStrategyFactory {

    private final Map<StrategyType, ComparisonStrategy> strategyMap = new EnumMap<>(StrategyType.class);

    public ComparisonStrategyFactory(List<ComparisonStrategy> strategies) {
        for (ComparisonStrategy strategy : strategies) {
            strategyMap.put(strategy.getStrategyType(), strategy);
        }
    }

    public ComparisonStrategy getStrategy(StrategyType type) {
        ComparisonStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new BusinessException("Unsupported comparison strategy type: " + type);
        }
        return strategy;
    }

    public List<ComparisonStrategy> getAllStrategies() {
        return List.copyOf(strategyMap.values());
    }
}
