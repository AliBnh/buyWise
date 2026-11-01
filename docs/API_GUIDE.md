# BuyWise REST API Reference & Integration Guide

The BuyWise REST API is documented interactively via OpenAPI / Swagger UI at:
`http://localhost:8080/swagger-ui.html`

Base URL: `http://localhost:8080/api/v1` (or `http://localhost:3000/api/v1` through the Nginx reverse proxy).

All authenticated endpoints require an HTTP `Authorization` header:
```http
Authorization: Bearer <jwt_token>
```

---

## 1. Authentication & Onboarding

### 1.1 Register User
- **Endpoint**: `POST /api/v1/auth/register`
- **Auth**: Public
- **Request Body**:
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123!",
  "fullName": "Karim Alami",
  "preferredCurrency": "MAD"
}
```
- **Response** (`201 Created`):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "user": {
    "id": "c1f7a18b-59b1-4f1e-9df3-0bc69d2a1a44",
    "email": "user@example.com",
    "fullName": "Karim Alami",
    "preferredCurrency": "MAD",
    "role": "ROLE_USER"
  }
}
```

### 1.2 Login User
- **Endpoint**: `POST /api/v1/auth/login`
- **Auth**: Public
- **Request Body**:
```json
{
  "email": "demo@buywise.com",
  "password": "Demo123!"
}
```
- **Response** (`200 OK`):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "user": {
    "id": "11111111-1111-1111-1111-111111111111",
    "email": "demo@buywise.com",
    "fullName": "Demo Consumer",
    "preferredCurrency": "MAD",
    "role": "ROLE_USER"
  }
}
```

### 1.3 Quick Demo Login
- **Endpoint**: `POST /api/v1/demo/login`
- **Auth**: Public
- **Response** (`200 OK`): Logs into the pre-seeded demo user and returns an active JWT token.

### 1.4 Reset Demo Data
- **Endpoint**: `POST /api/v1/demo/reset`
- **Auth**: Public
- **Response** (`200 OK`): Resets the database to pristine state with all sample categories, verified sellers, laptops, appliances, and return deadlines.

---

## 2. Offer Comparison & Value Engine

### 2.1 Create Comparison
- **Endpoint**: `POST /api/v1/comparisons`
- **Auth**: Required (`ROLE_USER`)
- **Request Body**:
```json
{
  "title": "Lenovo IdeaPad vs Asus ZenBook Comparison",
  "categoryId": "22222222-2222-2222-2222-222222222221",
  "preferredStrategy": "BEST_VALUE",
  "offers": [
    {
      "productName": "Lenovo IdeaPad Slim 5",
      "modelNumber": "82XF004KFE",
      "sellerName": "Electroplanet Morocco",
      "price": 8499.00,
      "deliveryFee": 0.00,
      "additionalFees": 0.00,
      "discountAmount": 500.00,
      "currency": "MAD",
      "warrantyMonths": 12,
      "returnPolicySummary": "14 days free return unopened",
      "specifications": {
        "Processor": "AMD Ryzen 7 7730U",
        "RAM": "16 GB DDR4",
        "Storage": "512 GB SSD NVMe",
        "Display": "16 inch WUXGA IPS"
      }
    },
    {
      "productName": "Asus ZenBook 14 OLED",
      "modelNumber": "UX3402VA",
      "sellerName": "Marjane Mall",
      "price": 9199.00,
      "deliveryFee": 49.00,
      "additionalFees": 0.00,
      "discountAmount": 0.00,
      "currency": "MAD",
      "warrantyMonths": 24,
      "returnPolicySummary": "7 days exchange only",
      "specifications": {
        "Processor": "Intel Core i7-1360P",
        "RAM": "16 GB LPDDR5",
        "Storage": "1 TB SSD NVMe",
        "Display": "14 inch 2.8K 90Hz OLED"
      }
    }
  ]
}
```

### 2.2 Calculate Deterministic Metrics
- **Endpoint**: `POST /api/v1/comparisons/{id}/calculate?strategy=BEST_VALUE`
- **Auth**: Required
- **Response** (`200 OK`): Computes 100% deterministic metrics:
```json
{
  "strategyUsed": "BEST_VALUE",
  "recommendedOfferId": "...",
  "cheapestOfferId": "...",
  "offers": [
    {
      "offerId": "...",
      "productName": "Lenovo IdeaPad Slim 5",
      "basePrice": 8499.00,
      "totalCost": 7999.00,
      "costDifferenceFromLowest": 0.00,
      "percentageDifferenceFromLowest": 0.0,
      "warrantyMonths": 12,
      "score": 8.4
    },
    {
      "offerId": "...",
      "productName": "Asus ZenBook 14 OLED",
      "basePrice": 9199.00,
      "totalCost": 9248.00,
      "costDifferenceFromLowest": 1249.00,
      "percentageDifferenceFromLowest": 15.61,
      "warrantyMonths": 24,
      "score": 9.1
    }
  ]
}
```

---

## 3. Grounded AI Intelligence

### 3.1 Run AI Offer Analysis
- **Endpoint**: `POST /api/v1/ai/comparisons/{id}/analyze`
- **Auth**: Required
- **Response** (`200 OK`):
```json
{
  "summary": "Both offers present solid ultraportable laptops, but Offer B offers double the storage and warranty for a 15.6% higher net price.",
  "keyDifferences": [
    "Storage capacity: Offer A (512GB) vs Offer B (1TB SSD NVMe).",
    "Manufacturer warranty: Offer A provides 12 months, whereas Offer B includes 24 months coverage.",
    "Display technology: Offer B features a 2.8K OLED 90Hz panel vs Offer A IPS standard."
  ],
  "strengths": {
    "Offer A": ["Lower upfront cost (saves 1,249 MAD)", "Includes 500 MAD discount", "14-day return window"],
    "Offer B": ["Twice the storage (1 TB)", "24-month manufacturer warranty protection", "Premium OLED screen"]
  },
  "weaknesses": {
    "Offer A": ["Shorter 1-year warranty", "Limited 512 GB SSD storage for long-term use"],
    "Offer B": ["Additional 49 MAD delivery fee", "Restrictive 7-day return policy with exchange only"]
  },
  "missingInformation": [
    "Battery capacity (Wh) and fast charging compatibility not specified for Offer A.",
    "Warranty claim process for Offer B (on-site repair vs carry-in service center)."
  ],
  "risks": [
    "Offer B return policy restricts refunds to store exchanges only."
  ],
  "recommendation": "Choose Offer B if you intend to keep the laptop for 3+ years where the 2-year warranty and 1TB storage justify the 1,249 MAD difference. Choose Offer A if strict upfront budget is the main driver."
}
```

### 3.2 Context-Aware AI Purchase Assistant
- **Endpoint**: `POST /api/v1/ai/assistant/chat`
- **Auth**: Required
- **Request Body**:
```json
{
  "message": "Which of my purchases still have active warranties?",
  "conversationId": null
}
```
- **Response** (`200 OK`): The assistant checks the authenticated user's database records and returns a grounded answer summarizing active warranties, return deadlines, and expiration dates.

---

## 4. Document Processing & Human-in-the-Loop Review

### 4.1 Upload Document
- **Endpoint**: `POST /api/v1/documents/upload`
- **Auth**: Required
- **Content-Type**: `multipart/form-data`
- **Form Fields**: `file` (binary), `purchaseId` (optional UUID), `documentType` (`RECEIPT`, `WARRANTY`, `RETURN_POLICY`, `SPEC_SHEET`)

### 4.2 Extract Structured Facts
- **Endpoint**: `POST /api/v1/documents/{id}/extract`
- **Auth**: Required
- **Response** (`200 OK`): Returns extracted facts with `source_excerpt`, confidence score, and status `DRAFT`.

### 4.3 Verify or Correct Extracted Fact
- **Endpoint**: `PUT /api/v1/documents/facts/{factId}/verify`
- **Auth**: Required
- **Request Body**:
```json
{
  "fieldValue": "8499.00",
  "verificationStatus": "VERIFIED"
}
```

---

## 5. Deadlines & Ownership Management

### 5.1 List Deadlines
- **Endpoint**: `GET /api/v1/deadlines?status=UPCOMING`
- **Auth**: Required

### 5.2 Complete Deadline
- **Endpoint**: `PUT /api/v1/deadlines/{id}/complete`
- **Auth**: Required

### 5.3 Health Check Probe
- **Endpoint**: `GET /api/v1/health`
- **Auth**: Public
- **Response** (`200 OK`):
```json
{
  "status": "UP",
  "application": "BuyWise Intelligence Platform",
  "timestamp": "2026-11-01T17:15:00Z",
  "version": "1.0.0"
}
```
