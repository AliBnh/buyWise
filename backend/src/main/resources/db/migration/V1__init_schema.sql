-- ==============================================================================
-- BuyWise PostgreSQL Flyway Migration V1: Core Normalized Schema
-- Enforces 3NF, data integrity, user isolation, indexes, and audit timestamps.
-- ==============================================================================

-- 1. Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    preferred_currency VARCHAR(10) NOT NULL DEFAULT 'MAD',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);

-- 2. Product Categories
CREATE TABLE product_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    icon_name VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. Sellers Table
CREATE TABLE sellers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    website_url VARCHAR(500),
    reputation_score DECIMAL(3, 2) DEFAULT 4.00,
    return_policy_summary TEXT,
    is_verified BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. Comparisons Table
CREATE TABLE comparisons (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    category_name VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    notes TEXT,
    ai_analysis_json TEXT,
    ai_analyzed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_comparisons_user_id ON comparisons(user_id);
CREATE INDEX idx_comparisons_status ON comparisons(status);

-- 5. Offers Table
CREATE TABLE offers (
    id BIGSERIAL PRIMARY KEY,
    comparison_id BIGINT NOT NULL REFERENCES comparisons(id) ON DELETE CASCADE,
    seller_name VARCHAR(200) NOT NULL,
    seller_id BIGINT REFERENCES sellers(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    product_model VARCHAR(200),
    source_url VARCHAR(1000),
    currency VARCHAR(10) NOT NULL DEFAULT 'MAD',
    base_price DECIMAL(12, 2) NOT NULL,
    delivery_fee DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    additional_fees DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total_price DECIMAL(12, 2) NOT NULL,
    warranty_months INTEGER NOT NULL DEFAULT 12,
    return_window_days INTEGER NOT NULL DEFAULT 14,
    specifications_json TEXT,
    warranty_conditions TEXT,
    return_policy TEXT,
    is_recommended BOOLEAN DEFAULT FALSE,
    value_score DECIMAL(5, 2) DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_offer_base_price CHECK (base_price >= 0),
    CONSTRAINT chk_offer_total_price CHECK (total_price >= 0),
    CONSTRAINT chk_offer_warranty CHECK (warranty_months >= 0)
);

CREATE INDEX idx_offers_comparison_id ON offers(comparison_id);

-- 6. Purchases Table (Ownership Library)
CREATE TABLE purchases (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_name VARCHAR(255) NOT NULL,
    model_number VARCHAR(100),
    category VARCHAR(100),
    seller_name VARCHAR(200) NOT NULL,
    purchase_date DATE NOT NULL,
    price_paid DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'MAD',
    warranty_months INTEGER NOT NULL DEFAULT 12,
    warranty_expiration_date DATE NOT NULL,
    return_deadline DATE,
    serial_number VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_purchase_price CHECK (price_paid >= 0),
    CONSTRAINT chk_purchase_warranty CHECK (warranty_months >= 0)
);

CREATE INDEX idx_purchases_user_id ON purchases(user_id);
CREATE INDEX idx_purchases_warranty_exp ON purchases(warranty_expiration_date);

-- 7. Documents Table
CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    purchase_id BIGINT REFERENCES purchases(id) ON DELETE SET NULL,
    comparison_id BIGINT REFERENCES comparisons(id) ON DELETE SET NULL,
    file_name VARCHAR(255) NOT NULL,
    stored_file_path VARCHAR(500) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    raw_text_content TEXT,
    extraction_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_documents_user_id ON documents(user_id);
CREATE INDEX idx_documents_purchase_id ON documents(purchase_id);

-- 8. Extracted Facts Table (Grounding & Verification)
CREATE TABLE extracted_facts (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    fact_key VARCHAR(100) NOT NULL,
    extracted_value TEXT NOT NULL,
    verified_value TEXT,
    source_excerpt TEXT,
    page_number INTEGER DEFAULT 1,
    confidence_score DECIMAL(3, 2) NOT NULL DEFAULT 0.85,
    verification_status VARCHAR(50) NOT NULL DEFAULT 'PENDING_VERIFICATION',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_extracted_facts_document_id ON extracted_facts(document_id);
CREATE INDEX idx_extracted_facts_status ON extracted_facts(verification_status);

-- 9. Deadlines Table
CREATE TABLE deadlines (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    purchase_id BIGINT REFERENCES purchases(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    deadline_type VARCHAR(50) NOT NULL,
    due_date DATE NOT NULL,
    urgency VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    is_completed BOOLEAN DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_deadlines_user_id ON deadlines(user_id);
CREATE INDEX idx_deadlines_due_date ON deadlines(due_date);
CREATE INDEX idx_deadlines_is_completed ON deadlines(is_completed);

-- 10. Reminders Table
CREATE TABLE reminders (
    id BIGSERIAL PRIMARY KEY,
    deadline_id BIGINT NOT NULL REFERENCES deadlines(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    trigger_date DATE NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    triggered_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reminders_user_id ON reminders(user_id);
CREATE INDEX idx_reminders_trigger_date ON reminders(trigger_date);
CREATE INDEX idx_reminders_status ON reminders(status);

-- 11. AI Assistant Conversations
CREATE TABLE ai_conversations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL DEFAULT 'New Conversation',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ai_conversations_user ON ai_conversations(user_id);

-- 12. AI Assistant Messages
CREATE TABLE ai_messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES ai_conversations(id) ON DELETE CASCADE,
    sender_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    tool_calls_json TEXT,
    tool_results_json TEXT,
    cited_references_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ai_messages_conv ON ai_messages(conversation_id);
