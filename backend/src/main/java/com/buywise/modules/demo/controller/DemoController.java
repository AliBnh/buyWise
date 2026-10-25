package com.buywise.modules.demo.controller;

import com.buywise.modules.auth.dto.AuthResponse;
import com.buywise.modules.auth.dto.LoginRequest;
import com.buywise.modules.auth.service.AuthService;
import com.buywise.modules.demo.service.DemoDataSeederService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/demo")
@RequiredArgsConstructor
@Tag(name = "Demo Operations", description = "Endpoints for inspecting demo scenario credentials and resetting demo data")
public class DemoController {

    private final DemoDataSeederService demoDataSeederService;
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Convenience one-click login as demo user")
    public ResponseEntity<AuthResponse> loginAsDemo() {
        return ResponseEntity.ok(authService.login(new LoginRequest("demo@buywise.com", "Demo123!")));
    }

    @GetMapping("/info")
    @Operation(summary = "Get demo credentials and scenario description")
    public ResponseEntity<Map<String, Object>> getDemoInfo() {
        return ResponseEntity.ok(Map.of(
                "application", "BuyWise — Consumer Purchase Intelligence Platform",
                "defaultCurrency", "MAD",
                "demoUser", Map.of(
                        "email", "demo@buywise.com",
                        "password", "Demo123!",
                        "fullName", "Karim Bennani",
                        "role", "ROLE_USER"
                ),
                "adminUser", Map.of(
                        "email", "admin@buywise.com",
                        "password", "Admin123!",
                        "role", "ROLE_ADMIN"
                ),
                "scenario", "Demo user has an active laptop comparison (Lenovo IdeaPad Slim 5 variants) and two registered household purchases (Samsung EcoBubble Washing Machine and LG InstaView Refrigerator with active return window closing in 4 days)."
        ));
    }

    @PostMapping("/reset")
    @Operation(summary = "Reset demo user records to the pristine baseline state without affecting other users")
    public ResponseEntity<Map<String, String>> resetDemoData() {
        demoDataSeederService.resetDemoData();
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Demo user data has been successfully reset to baseline scenario."
        ));
    }
}
