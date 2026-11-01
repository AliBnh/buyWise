package com.buywise.modules.ai.provider;

/**
 * Replaceable abstraction for Large Language Model (LLM) inference providers.
 * 
 * Demonstrates:
 * - Dependency Inversion Principle (DIP): High-level business analysis services depend on this abstraction,
 *   never directly on a specific vendor's SDK (Groq, OpenAI, Anthropic, Ollama).
 * - Pluggable Architecture: Enables seamless switching between Groq, local models, or test doubles.
 */
public interface LlmProvider {

    String getProviderName();

    String getActiveModel();

    /**
     * Generates standard text completion.
     */
    String generateCompletion(String systemPrompt, String userPrompt);

    /**
     * Generates structured JSON completion guaranteed to follow JSON syntax.
     */
    String generateJsonCompletion(String systemPrompt, String userPrompt);

    /**
     * Generates and automatically deserializes structured output into a typed DTO.
     */
    <T> T generateStructuredOutput(String systemPrompt, String userPrompt, Class<T> responseType);
}
