# BuyWise Architecture & System Design Deep-Dive Guide

This guide is designed for systems architects, senior software engineers, and technical evaluators inspecting the **BuyWise** platform.

---

## 1. High-Level Architectural Decisions

### Q1: Why a Modular Monolith instead of Microservices?
**Answer:**
For a consumer application at this stage, a well-structured **modular monolith** offers maximum cohesion with zero distributed-system overhead. 
- **Consistency**: ACID transactions guarantee atomic consistency between offers, comparisons, purchases, and scheduled reminders without complex 2-phase commits (2PC) or saga orchestrators.
- **Developer Velocity & Testing**: Fast local boot times, end-to-end integration tests (`@SpringBootTest`) running against in-memory or Docker PostgreSQL, and zero network latency between domain boundaries.
- **Explicit Domain Boundaries**: Packages (`comparison`, `purchase`, `document`, `deadline`, `ai`) are strictly decoupled. If traffic demands independent scaling of document OCR or LLM orchestration in the future, these modules can be extracted into independent microservices with minimal refactoring.

### Q2: Why is deterministic arithmetic strictly separated from LLM generation?
**Answer:**
LLMs are probabilistic token predictors, not algebraic compute engines. Asking an LLM to compute price differences, currency conversions, delivery markups, or warranty month subtractions introduces:
1. **Hallucination Risk**: Floating point and decimal math errors that destroy consumer trust.
2. **Non-Determinism**: Different numbers generated on repeated requests for the same offers.
3. **Auditability Failure**: Financial and comparative metrics must be 100% auditable and reproducible.

In BuyWise:
- All cost metrics (total cost, difference amounts, savings percentages, warranty spans) are computed by [`ComparisonEngineService`](file:///c:/Users/HP/Collections/VsCode%20Projects/buyWise/backend/src/main/java/com/buywise/domain/comparison/service/ComparisonEngineService.java) using `BigDecimal` arithmetic.
- The LLM receives pre-computed, verified mathematical truths as input context. Its role is strictly semantic: explaining qualitative trade-offs, highlighting hidden exclusion clauses, identifying missing specifications, and generating personalized recommendations.

---

## 2. Design Patterns & SOLID Principles

### Q3: How is the Strategy Pattern utilized in the Comparison Engine?
**Answer:**
Different consumers prioritize different criteria when buying: some want the lowest initial price; others want extended warranty protection or best overall spec value.
We implemented the **Strategy Pattern** via [`ComparisonStrategy`](file:///c:/Users/HP/Collections/VsCode%20Projects/buyWise/backend/src/main/java/com/buywise/domain/comparison/strategy/ComparisonStrategy.java) with four concrete implementations:
1. `LowestPriceStrategy`: Ranks strictly by lowest total checkout price.
2. `BestWarrantyStrategy`: Prioritizes warranty length and coverage security.
3. `BestValueScoreStrategy`: Calculates a value-to-cost ratio evaluating hardware specifications per dirham/dollar spent.
4. `BalancedPerformanceStrategy`: Uses a multi-criteria weighted scoring algorithm balancing price, warranty, and seller reliability.

The [`ComparisonStrategyFactory`](file:///c:/Users/HP/Collections/VsCode%20Projects/buyWise/backend/src/main/java/com/buywise/domain/comparison/strategy/ComparisonStrategyFactory.java) dynamically resolves the desired strategy at runtime based on the client request or user preference. Adding a new strategy (e.g. `LowestCarbonFootprintStrategy`) requires zero changes to existing code (adhering strictly to the **Open/Closed Principle**).

### Q4: How is the Factory Pattern used elsewhere in the codebase?
**Answer:**
In [`DocumentProcessorFactory`](file:///c:/Users/HP/Collections/VsCode%20Projects/buyWise/backend/src/main/java/com/buywise/domain/document/processor/DocumentProcessorFactory.java), where incoming uploaded files are matched to their appropriate parser (`PdfDocumentProcessor`, `ImageDocumentProcessor`, or `TextDocumentProcessor`) based on MIME type detection, shielding the document service from concrete parser instantiation.

### Q5: Give a concrete example of Dependency Inversion (DIP) in the platform.
**Answer:**
The AI services (`OfferAnalysisAiService`, `DocumentUnderstandingAiService`) do not depend on the concrete `GroqLlmProvider`. They depend on the abstract [`LlmProvider`](file:///c:/Users/HP/Collections/VsCode%20Projects/buyWise/backend/src/main/java/com/buywise/ai/provider/LlmProvider.java) interface. Switching from Groq to an on-premise Ollama instance or Anthropic Claude requires implementing `LlmProvider` and updating the active Spring profile, with zero lines of business logic altered.

---

## 3. Reliability, Resilience & Grounding in LLM Integration

### Q6: How do you prevent hallucinations in AI offer comparisons?
**Answer:**
We implement three defense layers:
1. **Context Grounding**: System prompts explicitly constrain the model: *"You may ONLY base your reasoning on the provided offers and specs. If a specification is not provided, you must declare it as MISSING or UNVERIFIED. Never invent attributes or claim warranty coverage not explicitly stated."*
2. **Grammar & JSON Enforcement**: We use Groq's `response_format: { type: "json_object" }` combined with strict JSON schema instructions.
3. **Structured Validation**: The [`StructuredOutputValidator`](file:///c:/Users/HP/Collections/VsCode%20Projects/buyWise/backend/src/main/java/com/buywise/ai/validator/StructuredOutputValidator.java) deserializes and validates required keys (`summary`, `keyDifferences`, `strengths`, `weaknesses`, `missingInformation`, `risks`, `recommendation`). If the LLM generates an invalid or empty structure, the validator falls back to a deterministic semantic summary synthesized directly from verified database records.

### Q7: How do you handle LLM rate limits (HTTP 429) and outages (HTTP 503)?
**Answer:**
In [`GroqLlmProvider`](file:///c:/Users/HP/Collections/VsCode%20Projects/buyWise/backend/src/main/java/com/buywise/ai/provider/GroqLlmProvider.java):
1. **Exponential Backoff with Jitter**: When a 429 or 503 occurs, the client pauses exponentially with randomized jitter (`delay = baseDelay * 2^attempt + jitter`) up to 3 retries.
2. **Model Fallback**: If the primary high-throughput model (`openai/gpt-oss-20b`) encounters persistent rate limits or capacity constraints, the client automatically falls back to `openai/gpt-oss-120b`.
3. **Circuit Breaking & Timeout**: HTTP requests have a 35-second connect/read timeout to prevent thread starvation under slow upstream responses.

---

## 4. Human-in-the-Loop Document Verification

### Q8: Why don't you directly update the user's purchase data with AI extraction results?
**Answer:**
Document extraction from receipts, scanned invoices, and warranty cards can contain optical noise, unusual layouts, or unclear terms.
Turning unverified AI extraction directly into trusted application data violates the core principle of data integrity.
In BuyWise:
1. Extraction creates [`ExtractedFact`](file:///c:/Users/HP/Collections/VsCode%20Projects/buyWise/backend/src/main/java/com/buywise/domain/document/entity/ExtractedFact.java) rows with `status = DRAFT`.
2. Each fact stores the exact `source_excerpt` (verbatim text) and confidence score.
3. The user reviews these facts in the **Document Verification UI**, comparing the AI proposal directly against the highlighted excerpt.
4. Only upon explicit user confirmation (`VERIFIED`) is the purchase record updated with the true purchase price, warranty expiration, and return deadline.

---

## 5. Security & Multi-Tenancy

### Q9: How is multi-tenant user isolation enforced?
**Answer:**
1. Authentication is stateless via signed HMAC-SHA256 JWT tokens.
2. The `JwtAuthenticationFilter` populates the Spring Security `SecurityContextHolder`.
3. In every repository query and service method, the authenticated user's ID is explicitly verified:
   `comparisonRepository.findByIdAndUserId(comparisonId, currentUserId)`
4. URL tampering (e.g. User A requesting `/api/v1/comparisons/{user_b_id}`) yields an HTTP `404 Not Found` or `403 Forbidden`, preventing enumeration attacks.

### Q10: How are passwords and secrets stored?
**Answer:**
- Passwords are encrypted using BCrypt with standard cost factor 10.
- Secrets (`JWT_SECRET`, `GROQ_API_KEY`, database credentials) are injected exclusively via environment variables (`.env` or Docker secrets), never hardcoded in source repositories.

---

## 6. Scalability & Future Architecture Enhancements

### Q11: What would you change to scale this platform to 1,000,000 active users?
**Answer:**
1. **Database Read Replicas**: Configure Spring's `AbstractRoutingDataSource` to send `@Transactional(readOnly = true)` traffic to read replicas, reserving the primary node for write transactions.
2. **Redis Distributed Caching**: Cache category lists, verified seller catalogs, and LLM comparison summaries keyed by offer content hashes.
3. **Asynchronous OCR & Document Pipeline**: Offload OCR and heavy LLM extraction to an asynchronous job queue using Apache Kafka or RabbitMQ with Celery/Spring AMQP workers.
4. **Object Storage**: Replace local persistent volume `/app/uploads` with S3/MinIO compatible object storage with pre-signed upload URLs and server-side AES-256 encryption.
5. **Elasticsearch / OpenSearch**: Implement faceted search across owned products, receipts, and offer archives.
