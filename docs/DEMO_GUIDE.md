# BuyWise Live Demonstration & Evaluator Walkthrough Guide

Welcome to the **BuyWise** demonstration walkthrough. This guide walks you through every requirement and core workflow of the platform.

---

## 1. Quick Launch & Authentication

### 1.1 Local Launch
Run the full stack via Docker Compose:
```bash
docker compose up --build
```
- **Web Frontend Application**: `http://localhost:3000`
- **Backend REST API**: `http://localhost:8080/api/v1`
- **Interactive Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **Application Health**: `http://localhost:8080/api/v1/health`

### 1.2 Demo Credentials
The database comes pre-seeded with rich consumer data:
| Account Type | Email | Password | Role |
|---|---|---|---|
| **Demo Consumer** | `demo@buywise.com` | `Demo123!` | Standard Consumer |
| **System Admin** | `admin@buywise.com` | `Admin123!` | Administrator |

> **Tip**: On the Login page (`/login`), click the green **"Quick Demo Login"** button for instant one-click authentication.

### 1.3 Multi-Currency Switcher
In the top-right navigation bar, switch between:
- **MAD (Moroccan Dirham)** *(Default demo currency)*
- **EUR (€)**
- **USD ($)**

All prices across comparisons, ownership records, and summaries update dynamically in real time without page reload.

---

## 2. Walkthrough of Core Workflows

### Workflow A & B: Compare Offers & Grounded AI Analysis
1. Navigate to **Comparisons** (`/comparisons`).
2. Notice the pre-seeded comparison: *"Lenovo IdeaPad Slim 5 vs Asus ZenBook 14 OLED"*.
3. Click on the comparison to view the **side-by-side comparison matrix**:
   - Compare CPU, RAM, SSD storage, and screen technology.
   - Observe deterministic cost breakdown: base price, delivery surcharge, discounts, and total checkout cost.
   - Click **"Run Grounded AI Analysis"**:
     - The AI analyzes trade-offs without performing arithmetic.
     - Explains the 512GB vs 1TB storage difference and 12 vs 24 month warranty value.
     - Highlights missing specs and potential purchasing risks (such as return policies limited to store credit).
     - Presents grounded recommendations for different user profiles.

### Workflow C: Deterministic Total Cost & Strategy Engine
1. On the comparison details page, test the **Strategy Selector**:
   - Select **"Lowest Total Price"**: Immediately ranks Offer A first based strictly on bottom-line cost.
   - Select **"Best Warranty"**: Elevates Offer B because of its 24-month manufacturer coverage.
   - Select **"Best Value Score"**: Evaluates hardware specs per Dirham spent.
   - Select **"Balanced Performance"**: Runs multi-criteria weighted scoring.
2. Notice that arithmetic calculations (cost delta, savings percentage, warranty duration) are strictly computed by the Spring Boot backend using `BigDecimal`.

### Workflow D: Purchase & Ownership Library
1. Navigate to **My Purchases** (`/purchases`).
2. Inspect the pre-registered appliances and electronics:
   - *Samsung Bespoke Double-Door Refrigerator* (Purchased at Cosmos Morocco, with 120-month digital inverter warranty).
   - *Sony Bravia 4K TV* (Purchased at Electroplanet Morocco, with 24-month panel guarantee).
3. Click **"Register New Purchase"** to add a newly purchased device:
   - Select category, retailer, purchase date, serial number, and warranty period.
   - Observe that items remain permanently in the user's library even after warranty expiration.

### Workflow E: Intelligent Document Understanding & Human-in-the-Loop Review
1. Navigate to **Documents** (`/documents`).
2. Click on the pre-loaded *Samsung Refrigerator Invoice*.
3. Navigate to **Verification View** (`/documents/{id}/verify`):
   - Notice the extracted facts: Product Name, Purchase Price (13,490 MAD), Purchase Date, and Warranty (120 Months).
   - Note the **Verbatim Source Text Excerpt** displaying the exact sentence found in the document.
   - The status starts as **Draft / Unverified**.
   - Click **"Confirm / Verify"** or edit a field manually before approving.
   - Notice how unverified AI extraction is **never** silently converted into trusted application data without consumer consent.

### Workflow F: Deterministic Deadlines & Idempotent Reminders
1. Navigate to **Deadlines** (`/deadlines`).
2. Review the timeline of active purchase deadlines:
   - *Upcoming Return Window*: 14-day return period ending soon for recent electronics.
   - *Active Warranty Expiration*: Approaching expiration markers.
3. Mark a deadline as complete; observe real-time status transitions.
4. The background scheduler runs idempotently; restarting the service does not create duplicate alerts.

### Workflow G: Context-Aware AI Purchase Assistant
1. Navigate to **AI Assistant** (`/assistant`).
2. Try asking grounded queries about your personal purchases:
   - *"Which laptop offer provides better value?"*
   - *"What is the return deadline for my refrigerator?"*
   - *"Which of my products still have active warranties?"*
   - *"What are the hidden exclusions in my TV warranty?"*
3. Notice that the assistant answers with exact, grounded facts extracted from your database records, refusing to hallucinate outside your data.

---

## 3. Database State Reset
To return the application to its pristine demo state at any point:
- Navigate to **Settings** (`/settings`) and click **"Reset Demo Database"**.
- Or execute:
```bash
curl -X POST http://localhost:8080/api/v1/demo/reset
```
This re-seeds all categories, Moroccan retailers, comparisons, purchases, and documents instantly.
