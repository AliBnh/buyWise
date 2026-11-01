package com.buywise.modules.ai.provider;

import com.buywise.common.exception.BusinessException;
import com.buywise.common.exception.RateLimitException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

/**
 * Production implementation of {@link LlmProvider} communicating with Groq API.
 * 
 * Demonstrates:
 * - Exponential Backoff with Jitter: Adheres to Groq rate-limiting guidelines on HTTP 429 and 503.
 * - Robust Output Validation: Deserializes strict JSON objects into typed domain DTOs.
 * - Controlled Concurrency & Timeouts: Guarded with strict HTTP client timeouts.
 * - Fault Tolerance: Attempts primary model, falling back to secondary model if unavailable.
 */
@Slf4j
@Component
@Primary
public class GroqLlmProvider implements LlmProvider {

    private final String apiKey;
    private final String primaryModel;
    private final String fallbackModel;
    private final String baseUrl;
    private final int timeoutSeconds;
    private final int maxRetries;
    private final long retryDelayMs;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    public GroqLlmProvider(
            @Value("${buywise.groq.api-key}") String apiKey,
            @Value("${buywise.groq.model:openai/gpt-oss-20b}") String primaryModel,
            @Value("${buywise.groq.fallback-model:openai/gpt-oss-120b}") String fallbackModel,
            @Value("${buywise.groq.base-url:https://api.groq.com/openai/v1}") String baseUrl,
            @Value("${buywise.groq.timeout-seconds:35}") int timeoutSeconds,
            @Value("${buywise.groq.max-retries:3}") int maxRetries,
            @Value("${buywise.groq.retry-delay-ms:1000}") long retryDelayMs,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.primaryModel = primaryModel;
        this.fallbackModel = fallbackModel;
        this.baseUrl = baseUrl;
        this.timeoutSeconds = timeoutSeconds;
        this.maxRetries = maxRetries;
        this.retryDelayMs = retryDelayMs;
        this.objectMapper = objectMapper;

        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    @Override
    public String getProviderName() {
        return "Groq";
    }

    @Override
    public String getActiveModel() {
        return primaryModel;
    }

    @Override
    public String generateCompletion(String systemPrompt, String userPrompt) {
        return callGroqApi(systemPrompt, userPrompt, false, primaryModel);
    }

    @Override
    public String generateJsonCompletion(String systemPrompt, String userPrompt) {
        return callGroqApi(systemPrompt, userPrompt, true, primaryModel);
    }

    @Override
    public <T> T generateStructuredOutput(String systemPrompt, String userPrompt, Class<T> responseType) {
        String json = generateJsonCompletion(systemPrompt, userPrompt);
        try {
            return objectMapper.readValue(json, responseType);
        } catch (Exception ex) {
            log.error("Failed to parse structured model response into {}. Raw: {}", responseType.getSimpleName(), json, ex);
            throw new BusinessException("LLM produced invalid structured output format: " + ex.getMessage());
        }
    }

    private String callGroqApi(String systemPrompt, String userPrompt, boolean jsonMode, String model) {
        if (apiKey.isBlank() || apiKey.equals("mock-key")) {
            log.warn("Groq API key is not configured; using fallback mock structure.");
            return generateMockFallback(jsonMode);
        }

        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("model", model);

        List<Map<String, String>> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(Map.of("role", "system", "content", systemPrompt));
        }
        messages.add(Map.of("role", "user", "content", userPrompt));
        requestPayload.put("messages", messages);

        if (jsonMode) {
            requestPayload.put("response_format", Map.of("type", "json_object"));
        }

        int attempt = 0;
        String currentModel = model;

        while (attempt <= maxRetries) {
            attempt++;
            try {
                String requestJson = objectMapper.writeValueAsString(requestPayload);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + "/chat/completions"))
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .timeout(Duration.ofSeconds(timeoutSeconds))
                        .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int statusCode = response.statusCode();

                if (statusCode == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    return root.path("choices").get(0).path("message").path("content").asText();
                }

                log.warn("Groq API returned HTTP {} (Attempt {}/{}): {}", statusCode, attempt, maxRetries + 1, response.body());

                if (statusCode == 429) {
                    if (attempt > maxRetries) {
                        throw new RateLimitException("Groq API rate limit exceeded after " + maxRetries + " retry attempts.");
                    }
                    applyExponentialBackoffWithJitter(attempt);
                    continue;
                }

                if (statusCode == 404 && !currentModel.equals(fallbackModel)) {
                    log.warn("Model {} not found. Falling back to {}.", currentModel, fallbackModel);
                    currentModel = fallbackModel;
                    requestPayload.put("model", currentModel);
                    continue;
                }

                if (statusCode >= 500 && attempt <= maxRetries) {
                    applyExponentialBackoffWithJitter(attempt);
                    continue;
                }

                throw new BusinessException("Groq API request failed with status " + statusCode + ": " + response.body());

            } catch (RateLimitException rle) {
                throw rle;
            } catch (Exception ex) {
                if (attempt > maxRetries) {
                    log.error("Groq API call permanently failed after {} attempts", maxRetries, ex);
                    throw new BusinessException("Unable to contact AI inference provider: " + ex.getMessage(), ex);
                }
                applyExponentialBackoffWithJitter(attempt);
            }
        }

        throw new BusinessException("Exhausted all retries attempting to contact Groq API.");
    }

    private void applyExponentialBackoffWithJitter(int attempt) {
        long exponentialDelay = retryDelayMs * (1L << (attempt - 1));
        long jitter = random.nextInt((int) Math.min(exponentialDelay / 2, 1000));
        long totalDelay = exponentialDelay + jitter;
        try {
            log.info("Applying rate-limit backoff: sleeping for {} ms before retry attempt {}", totalDelay, attempt + 1);
            Thread.sleep(totalDelay);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new BusinessException("AI request thread was interrupted during backoff", ie);
        }
    }

    private String generateMockFallback(boolean jsonMode) {
        if (jsonMode) {
            return "{\"status\":\"success\",\"summary\":\"Simulated evaluation\",\"recommendations\":[\"Verify seller return terms\"],\"risks\":[],\"confidence_score\":0.9}";
        }
        return "Simulated AI evaluation fallback response.";
    }
}
