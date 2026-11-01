# BuyWise System Architecture Documentation

This document describes the end-to-end architecture of **BuyWise**, a consumer-facing purchase intelligence and ownership management platform.

---

## 1. High-Level Architecture (C4 Container View)

```mermaid
flowchart TB
    Consumer([Consumer / User])
    
    subgraph Browser["Client Browser (Port 3000)"]
        SPA["React 19 + TypeScript SPA\nTailwind CSS v4 + Lucide Icons\nCurrency Context (MAD, EUR, USD)\nAuth Context (JWT State)"]
    end

    subgraph ReverseProxy["Nginx Web Server"]
        Nginx["Reverse Proxy & Static Asset Server\n(Port 80 -> 3000)\nAPI Forwarding to /api/*\nGzip & SPA Fallback"]
    end

    subgraph AppServer["Backend Application Server (Port 8080)"]
        SpringCore["Spring Boot 3.3.5 / Java 21"]
        Security["Spring Security (Stateless JWT Filter)"]
        
        subgraph DomainModules["Domain Modules"]
            CompEngine["Comparison Engine\n(Deterministic Math + Strategy Pattern)"]
            DocProc["Document Understanding Pipeline\n(PDFBox, OCR, Fact Extraction)"]
            Ownership["Ownership & Purchase Library"]
            Scheduler["Idempotent Reminder Scheduler"]
            Assistant["Context-Grounded AI Assistant"]
        end

        subgraph AiLayer["AI Gateway Layer"]
            GroqClient["Groq LLM Client\n(HTTP/2, Backoff Jitter, Fallback Model)"]
            OutputValidator["Structured JSON Validator & Sanitizer"]
        end
    end

    subgraph DataStore["Persistence & External Services"]
        Postgres[("PostgreSQL 16 Database\n(3NF Schema, Flyway Migrations\nUser-Isolated Row Access)")]
        DiskStorage[("Persistent File Volume\n(/app/uploads)")]
        GroqApi["Groq Cloud LLM API\n(gpt-oss-20b / gpt-oss-120b)"]
    end

    Consumer -->|HTTPS/HTTP| Nginx
    Nginx -->|Static Assets| SPA
    SPA -->|RESTful JSON API Requests| Nginx
    Nginx -->|Proxy /api/*| SpringCore
    SpringCore --> Security
    Security --> DomainModules
    CompEngine --> Postgres
    Ownership --> Postgres
    Ownership --> DiskStorage
    DocProc --> DiskStorage
    DocProc --> Postgres
    Scheduler --> Postgres
    DomainModules --> AiLayer
    GroqClient -->|Encrypted HTTPS API| GroqApi
    AiLayer --> DomainModules
    DomainModules --> Postgres
```

---

## 2. Component Architecture

### 2.1 Backend (Spring Boot 3.3.5 / Java 21)
The backend follows a layered, hexagonal-inspired architecture with explicit package boundaries:

- `com.buywise.common`: Shared infrastructure, security filters, cross-cutting configurations, exception handlers, and security context utilities.
- `com.buywise.domain.auth`: User registration, authentication, JWT token issuance, and password encoding.
- `com.buywise.domain.user`: User aggregate, roles (`ROLE_USER`, `ROLE_ADMIN`), profile updates, and currency preferences.
- `com.buywise.domain.comparison`: Offer comparison aggregates, offer specifications, deterministic math engine, and ranking strategies (`LowestPriceStrategy`, `BestWarrantyStrategy`, `BestValueScoreStrategy`, `BalancedPerformanceStrategy`).
- `com.buywise.domain.purchase`: Ownership library, purchase recording, warranty tracking, and invoice linkages.
- `com.buywise.domain.document`: Multi-format document ingestion, storage, text extraction, and extracted fact entities.
- `com.buywise.domain.deadline`: Deadlines, scheduled reminders, and background scheduler service.
- `com.buywise.domain.catalog`: Product categories and verified retailer catalog.
- `com.buywise.ai.provider`: LLM gateway abstraction (`LlmProvider`) and Groq implementation with retry/fallback logic.
- `com.buywise.ai.service`: High-level AI orchestrators (`OfferAnalysisAiService`, `DocumentUnderstandingAiService`, `AiAssistantService`).
- `com.buywise.ai.validator`: Structured JSON parsing, schema validation, and guardrails against ungrounded hallucinations.

### 2.2 Frontend (React 19 / TypeScript / Vite / Tailwind CSS v4)
The frontend is a single-page application built for consumer clarity:

- **State Management & Contexts**:
  - `AuthContext`: Manages active JWT tokens, user profiles, login, registration, and one-click demo login.
  - `CurrencyContext`: Handles real-time currency conversion and formatting across Moroccan Dirham (MAD), Euros (EUR), and US Dollars (USD).
- **Navigation & Views**:
  - `LandingPage`: Consumer-facing hero, feature showcase, and interactive demo triggers.
  - `DashboardPage`: Overview metrics, recent comparisons, upcoming deadlines, and quick action cards.
  - `ComparisonsListPage` & `NewComparisonPage`: Multi-offer builder with manual inputs, specification attributes, and comparison strategies.
  - `ComparisonDetailsPage`: Visual side-by-side spec comparison, deterministic cost breakdown, AI semantic analysis, risk warnings, and grounded recommendations.
  - `PurchaseLibraryPage`: Ownership registry displaying active warranties, return deadlines, and attached purchase receipts.
  - `DocumentLibraryPage` & `DocumentVerificationPage`: Human-in-the-Loop fact review interface with verbatim document source text excerpts and verification toggles.
  - `DeadlinesPage`: Visual timeline of upcoming return deadlines, warranty expirations, and idempotent reminders.
  - `AiAssistantPage`: Conversational AI purchase advisor grounded strictly in the authenticated user's comparisons and purchases.
  - `SettingsPage`: System profile and demo database reset triggers.

---

## 3. Database Schema & Entity Relationship Diagram (ERD)

The database schema is managed via Flyway (`V1__init_schema.sql` and `V2__seed_demo_data.sql`) in Third Normal Form (3NF) with strict foreign key constraints, cascade rules, and indexes:

```mermaid
erDiagram
    USERS ||--o{ COMPARISONS : owns
    USERS ||--o{ PURCHASES : owns
    USERS ||--o{ DOCUMENTS : owns
    USERS ||--o{ DEADLINES : owns
    USERS ||--o{ AI_CONVERSATIONS : conducts

    COMPARISONS ||--|{ OFFERS : contains
    OFFERS ||--o{ OFFER_SPECIFICATIONS : has
    OFFERS }o--|| SELLERS : "sold by"
    COMPARISONS }o--|| PRODUCT_CATEGORIES : classifies

    PURCHASES ||--o{ DOCUMENTS : "linked to"
    PURCHASES }o--|| SELLERS : "bought from"
    PURCHASES }o--|| PRODUCT_CATEGORIES : classifies

    DOCUMENTS ||--o{ EXTRACTED_FACTS : extracts
    
    PURCHASES ||--o{ DEADLINES : triggers
    DEADLINES ||--o{ REMINDERS : schedules
    
    AI_CONVERSATIONS ||--|{ AI_MESSAGES : records

    USERS {
        UUID id PK
        VARCHAR email UK
        VARCHAR password_hash
        VARCHAR full_name
        VARCHAR preferred_currency
        VARCHAR role
        TIMESTAMP created_at
    }

    COMPARISONS {
        UUID id PK
        UUID user_id FK
        VARCHAR title
        UUID category_id FK
        VARCHAR preferred_strategy
        VARCHAR status
        JSONB ai_analysis_cache
        TIMESTAMP created_at
    }

    OFFERS {
        UUID id PK
        UUID comparison_id FK
        VARCHAR product_name
        VARCHAR model_number
        UUID seller_id FK
        DECIMAL price
        DECIMAL delivery_fee
        DECIMAL additional_fees
        DECIMAL discount_amount
        VARCHAR currency
        INT warranty_months
        VARCHAR return_policy_summary
        TEXT raw_text_notes
        BOOLEAN is_recommended
    }

    PURCHASES {
        UUID id PK
        UUID user_id FK
        VARCHAR product_name
        VARCHAR model_number
        UUID seller_id FK
        DECIMAL purchase_price
        VARCHAR currency
        DATE purchase_date
        DATE warranty_expiration_date
        DATE return_deadline
        VARCHAR serial_number
        VARCHAR status
    }

    DOCUMENTS {
        UUID id PK
        UUID user_id FK
        UUID purchase_id FK
        VARCHAR file_name
        VARCHAR file_path
        VARCHAR mime_type
        BIGINT file_size
        VARCHAR document_type
        VARCHAR status
        TEXT extracted_raw_text
    }

    EXTRACTED_FACTS {
        UUID id PK
        UUID document_id FK
        VARCHAR field_name
        VARCHAR field_value
        DECIMAL confidence_score
        VARCHAR source_excerpt
        INT page_number
        VARCHAR verification_status
    }

    DEADLINES {
        UUID id PK
        UUID user_id FK
        UUID purchase_id FK
        VARCHAR title
        DATE target_date
        VARCHAR deadline_type
        VARCHAR status
    }

    REMINDERS {
        UUID id PK
        UUID deadline_id FK
        DATE scheduled_date
        VARCHAR status
        TIMESTAMP dispatched_at
    }
```

---

## 4. Multi-Tenant User Data Isolation

BuyWise implements strict multi-tenancy at the software and query levels:

1. **Authentication Token**: The stateless JWT token contains the user's UUID in the `sub` claim.
2. **Context Resolution**: Every request extracts `userId` via `SecurityUtils.getCurrentUserId()`.
3. **Repository Guardrails**: Every query mutating or querying sensitive consumer data explicitly filters by `userId`:
   ```java
   @Query("SELECT c FROM Comparison c WHERE c.id = :id AND c.user.id = :userId")
   Optional<Comparison> findByIdAndUserId(UUID id, UUID userId);
   ```
4. **AI Context Isolation**: The AI assistant only receives context from purchases and comparisons matching the authenticated `userId`. Under no circumstances can user A inspect or ask about user B's documents or purchases.

---

## 5. Reliable AI Integration Architecture

### 5.1 Provider Gateway & Fault Tolerance
- **Model Selection**: Uses Groq's high-throughput `openai/gpt-oss-20b` for sub-second structured JSON reasoning, with seamless fallback to `openai/gpt-oss-120b` if capacity limits occur.
- **Resilience**: Implements exponential backoff with randomized jitter on HTTP `429 (Too Many Requests)` and `503 (Service Unavailable)`.
- **Stateless Prompts**: Every prompt is deterministic and fully grounded in user-provided entities.

### 5.2 Structured Output Guardrails
- LLM outputs are forced into strict JSON syntax via Groq's `response_format: { type: "json_object" }`.
- The [`StructuredOutputValidator`](file:///c:/Users/HP/Collections/VsCode%20Projects/buyWise/backend/src/main/java/com/buywise/modules/ai/service/StructuredOutputValidator.java) performs schema conformity checks, stripping accidental markdown code blocks and validating required fields.

### 5.3 Human-in-the-Loop Document Verification
When a user uploads a document:
1. Text is extracted deterministically (PDFBox / text parser).
2. AI extracts key facts (purchase price, warranty duration, return period, exclusions).
3. Every fact is saved with status `DRAFT`, storing the exact `source_excerpt` verbatim from the document.
4. The user reviews the facts in the `DocumentVerificationPage`. Only when confirmed or edited by the user does the fact update the verified `Purchase` record.
