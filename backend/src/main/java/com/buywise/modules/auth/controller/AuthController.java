package com.buywise.modules.auth.controller;

import com.buywise.modules.auth.dto.AuthResponse;
import com.buywise.modules.auth.dto.LoginRequest;
import com.buywise.modules.auth.dto.RegisterRequest;
import com.buywise.modules.auth.dto.UpdateProfileRequest;
import com.buywise.modules.auth.dto.UserProfileResponse;
import com.buywise.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller managing authentication, user registration, and profile lifecycles.
 * 
 * Demonstrates:
 * - Single Responsibility Principle (SRP): Focuses strictly on HTTP ingress, request validation, and status mapping.
 * - Stateless Authentication Boundary: Exposes public onboarding endpoints while profile methods require valid JWT.
 * - Inversion of Control (IoC): Constructor-based injection of {@link AuthService} via Lombok {@link RequiredArgsConstructor}.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, token generation, and profile management")
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user account with hashed credentials and returns an initial JWT authentication token.
     * 
     * @param request Validated registration payload containing email, password, and full name.
     * @return HTTP 201 CREATED with user identity and signed Bearer token.
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Enforces Bean Validation JSR 380 via @Valid before reaching the domain service
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates existing user credentials against BCrypt password hashes and emits a signed JWT.
     * 
     * @param request Login credentials (email and plaintext password).
     * @return HTTP 200 OK with authenticated user profile and access token.
     */
    @PostMapping("/login")
    @Operation(summary = "Authenticate user and receive JWT Bearer token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the profile of the currently authenticated principal extracted from the SecurityContext.
     * 
     * @return HTTP 200 OK with user profile details.
     */
    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user's profile")
    public ResponseEntity<UserProfileResponse> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUserProfile());
    }

    /**
     * Updates profile settings (e.g. display name, default currency preferences) for the authenticated principal.
     * 
     * @param request Validated profile update payload.
     * @return HTTP 200 OK with updated profile state.
     */
    @PutMapping("/me")
    @Operation(summary = "Update current authenticated user's profile")
    public ResponseEntity<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(authService.updateProfile(request));
    }
}
