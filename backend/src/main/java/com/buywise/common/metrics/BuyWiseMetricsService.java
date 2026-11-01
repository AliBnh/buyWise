package com.buywise.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for publishing custom business domain metrics to Prometheus via Micrometer.
 */
@Service
public class BuyWiseMetricsService {

    private final MeterRegistry meterRegistry;

    private final Counter comparisonCreatedCounter;
    private final Counter comparisonAnalyzedCounter;
    private final Counter purchaseCreatedCounter;
    private final Counter documentUploadedCounter;
    private final Counter deadlineCompletedCounter;

    public BuyWiseMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.comparisonCreatedCounter = Counter.builder("buywise_comparisons_created_total")
                .description("Total number of purchasing comparisons created")
                .register(meterRegistry);

        this.comparisonAnalyzedCounter = Counter.builder("buywise_comparisons_analyzed_total")
                .description("Total number of comparisons analyzed with AI")
                .register(meterRegistry);

        this.purchaseCreatedCounter = Counter.builder("buywise_purchases_created_total")
                .description("Total number of user purchases registered")
                .register(meterRegistry);

        this.documentUploadedCounter = Counter.builder("buywise_documents_uploaded_total")
                .description("Total number of receipts and documents uploaded")
                .register(meterRegistry);

        this.deadlineCompletedCounter = Counter.builder("buywise_deadlines_completed_total")
                .description("Total number of deadlines marked completed by users")
                .register(meterRegistry);
    }

    public void incrementComparisonCreated() {
        comparisonCreatedCounter.increment();
    }

    public void incrementComparisonAnalyzed() {
        comparisonAnalyzedCounter.increment();
    }

    public void incrementPurchaseCreated() {
        purchaseCreatedCounter.increment();
    }

    public void incrementDocumentUploaded() {
        documentUploadedCounter.increment();
    }

    public void incrementDeadlineCompleted() {
        deadlineCompletedCounter.increment();
    }

    public void recordDeterministicCalculation(String strategy) {
        Counter.builder("buywise_deterministic_calculations_total")
                .tag("strategy", strategy != null ? strategy : "UNKNOWN")
                .description("Total number of deterministic comparison calculations executed")
                .register(meterRegistry)
                .increment();
    }

    public void recordAiRequest(String operation, String status, long durationMillis) {
        Counter.builder("buywise_ai_requests_total")
                .tag("operation", operation)
                .tag("status", status)
                .description("Total number of AI requests dispatched")
                .register(meterRegistry)
                .increment();

        Timer.builder("buywise_ai_duration_seconds")
                .tag("operation", operation)
                .description("Execution duration of AI calls")
                .register(meterRegistry)
                .record(durationMillis, TimeUnit.MILLISECONDS);
    }

    public void recordFactVerified(String verificationStatus) {
        Counter.builder("buywise_facts_verified_total")
                .tag("status", verificationStatus)
                .description("Total number of document facts verified or modified by users")
                .register(meterRegistry)
                .increment();
    }
}
