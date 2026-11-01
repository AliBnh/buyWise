package com.buywise.common.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BuyWiseMetricsServiceTest {

    private SimpleMeterRegistry meterRegistry;
    private BuyWiseMetricsService metricsService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsService = new BuyWiseMetricsService(meterRegistry);
    }

    @Test
    @DisplayName("Should increment business domain counters accurately")
    void shouldIncrementCounters() {
        metricsService.incrementComparisonCreated();
        metricsService.incrementComparisonAnalyzed();
        metricsService.incrementPurchaseCreated();
        metricsService.incrementDocumentUploaded();
        metricsService.incrementDeadlineCompleted();
        metricsService.recordDeterministicCalculation("LOWEST_PRICE");
        metricsService.recordAiRequest("analyze_offers", "SUCCESS", 250);

        assertThat(meterRegistry.find("buywise_comparisons_created_total").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.find("buywise_comparisons_analyzed_total").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.find("buywise_purchases_created_total").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.find("buywise_documents_uploaded_total").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.find("buywise_deadlines_completed_total").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.find("buywise_deterministic_calculations_total").tag("strategy", "LOWEST_PRICE").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.find("buywise_ai_requests_total").tag("operation", "analyze_offers").counter().count()).isEqualTo(1.0);
    }
}
