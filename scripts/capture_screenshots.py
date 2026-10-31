import json
import urllib.request
import time
from playwright.sync_api import sync_playwright

def get_demo_token():
    req = urllib.request.Request("http://localhost:3001/api/v1/demo/login", method="POST")
    with urllib.request.urlopen(req) as resp:
        data = json.loads(resp.read().decode())
        return data["token"], data

def capture():
    token, user_data = get_demo_token()
    print(f"Obtained token for {user_data.get('email')}")

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True, channel="msedge")
        context = browser.new_context(viewport={"width": 1440, "height": 900})
        page = context.new_page()

        # 1. Landing Page
        print("Capturing Landing Page...")
        page.goto("http://localhost:3001/", wait_until="networkidle")
        page.wait_for_timeout(1000)
        page.screenshot(path="docs/screenshots/landing-page.png")

        # Set Auth Tokens in LocalStorage
        print("Authenticating session in browser...")
        user_obj = {
            "id": user_data["id"],
            "email": user_data["email"],
            "fullName": user_data["fullName"],
            "role": user_data["role"],
            "preferredCurrency": user_data.get("preferredCurrency", "MAD")
        }
        page.evaluate("""([token, user]) => {
            localStorage.setItem('buywise_token', token);
            localStorage.setItem('buywise_user', JSON.stringify(user));
        }""", [token, user_obj])

        # 2. Application Dashboard
        print("Capturing App Dashboard...")
        page.goto("http://localhost:3001/dashboard", wait_until="networkidle")
        page.wait_for_timeout(1500)
        page.screenshot(path="docs/screenshots/dashboard-overview.png")

        # 3. Comparisons List
        print("Capturing Comparisons List...")
        page.goto("http://localhost:3001/comparisons", wait_until="networkidle")
        page.wait_for_timeout(1000)
        page.screenshot(path="docs/screenshots/comparisons-list.png")

        # 4. Comparisons Details (Lenovo IdeaPad with Deterministic Engine & AI Analysis)
        print("Capturing Comparison Details...")
        page.goto("http://localhost:3001/comparisons/2", wait_until="networkidle")
        page.wait_for_timeout(1500)
        page.screenshot(path="docs/screenshots/comparison-details.png")

        # 5. Ownership & Purchases
        print("Capturing Purchases Library...")
        page.goto("http://localhost:3001/purchases", wait_until="networkidle")
        page.wait_for_timeout(1000)
        page.screenshot(path="docs/screenshots/purchases-library.png")

        # 6. Deadlines & Reminders
        print("Capturing Deadlines Timeline...")
        page.goto("http://localhost:3001/deadlines", wait_until="networkidle")
        page.wait_for_timeout(1000)
        page.screenshot(path="docs/screenshots/deadlines-timeline.png")

        # 7. AI Assistant
        print("Capturing AI Assistant...")
        page.goto("http://localhost:3001/assistant", wait_until="networkidle")
        page.wait_for_timeout(1500)
        page.screenshot(path="docs/screenshots/ai-assistant.png")

        # 8. Swagger OpenAPI
        print("Capturing Swagger OpenAPI Docs...")
        page.goto("http://localhost:8081/swagger-ui/index.html", wait_until="networkidle")
        page.wait_for_timeout(2000)
        page.screenshot(path="docs/screenshots/swagger-openapi.png")

        # 9. Grafana Metrics Dashboard
        print("Capturing Grafana Metrics Dashboard...")
        page.goto("http://localhost:3002/d/buywise-overview?orgId=1&kiosk", wait_until="networkidle")
        page.wait_for_timeout(3500)
        page.screenshot(path="docs/screenshots/grafana-metrics-dashboard.png")

        browser.close()
        print("All 9 screenshots successfully captured!")

if __name__ == "__main__":
    capture()
