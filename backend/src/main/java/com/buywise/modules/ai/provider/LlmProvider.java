package com.buywise.modules.ai.provider;

/**
 * ==============================================================================================
 * LLM PROVIDER ABSTRACTION — PORT & ADAPTER (HEXAGONAL ARCHITECTURE)
 * ==============================================================================================
 * 
 * DESIGN PATTERNS & SOLID PRINCIPLES:
 * 1. Dependency Inversion Principle (DIP):
 *    - High-level business intelligence services (e.g. `OfferAnalysisAiService`,
 *      `DocumentUnderstandingAiService`) depend exclusively on this abstract interface, NEVER on
 *      a concrete vendor SDK (Groq, OpenAI, Anthropic, or Ollama).
 *    - Low-level network adapters implement this interface.
 *
 * 2. Ports and Adapters (Hexagonal Architecture):
 *    - `LlmProvider` acts as the Outbound Port.
 *    - {@link GroqLlmProvider} acts as the Secondary (Driven) Adapter.
 *
 * 3. Open/Closed Principle (OCP) & Vendor Lock-in Elimination:
 *    - Switching from Groq to an on-premise Ollama instance or Anthropic Claude requires zero lines
 *      of business logic changes. One simply introduces a new class implementing `LlmProvider`
 *      and activates it via Spring profiles (`@Profile("ollama")`).
 * ==============================================================================================
 */
public interface LlmProvider {

    /**
     * Unique identifier for the provider adapter (e.g. "Groq", "OpenAI", "Ollama").
     */
    String getProviderName();

    /**
     * Current active model name running inference (e.g. "llama-3.3-70b-versatile").
     */
    String getActiveModel();

    /**
     * Generates free-form textual completion.
     */
    String generateCompletion(String systemPrompt, String userPrompt);

    /**
     * Generates raw JSON string output guaranteed to conform to JSON syntax.
     */
    String generateJsonCompletion(String systemPrompt, String userPrompt);

    /**
     * Generates and automatically deserializes structured JSON into a strongly typed target POJO/DTO.
     *
     * @param systemPrompt System instructions specifying persona and constraints
     * @param userPrompt User context containing verified facts and offer specifications
     * @param responseType Target class for JSON deserialization
     * @param <T> Generic DTO type parameter
     * @return Deserialized DTO instance
     */
    <T> T generateStructuredOutput(String systemPrompt, String userPrompt, Class<T> responseType);
}
