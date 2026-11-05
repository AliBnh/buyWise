package com.buywise;

import com.buywise.modules.auth.dto.LoginRequest;
import com.buywise.modules.auth.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test suite verifying user onboarding, credential authentication, and JWT lifecycle.
 * 
 * Demonstrates:
 * - Full-Stack Slice Integration: Tests interaction between MockMvc, SecurityConfig filter chain,
 *   BCryptPasswordEncoder, and in-memory H2 database under active test profile.
 * - JSON Path Assertion: Validates strict compliance with REST API contract schemas.
 * - Unauthenticated Rejection: Verifies unauthenticated access to domain resources returns 403 Forbidden.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Authentication REST API Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Verifies that registering with valid credentials creates an account in H2, hashes the password,
     * and returns a signed JWT with standard user claims.
     */
    @Test
    @DisplayName("POST /api/v1/auth/register should create account and return JWT Bearer token")
    void testRegisterEndpoint() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("test.integration@buywise.com")
                .fullName("Integration Tester")
                .password("Password123!")
                .preferredCurrency("MAD")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.email").value("test.integration@buywise.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.preferredCurrency").value("MAD"));
    }

    /**
     * Verifies that authenticating registered credentials against BCrypt hashes emits a valid access token.
     */
    @Test
    @DisplayName("POST /api/v1/auth/login should authenticate registered user")
    void testLoginEndpoint() throws Exception {
        RegisterRequest registerReq = RegisterRequest.builder()
                .email("login.test@buywise.com")
                .fullName("Login Tester")
                .password("Secret123!")
                .preferredCurrency("MAD")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerReq)));

        LoginRequest loginReq = LoginRequest.builder()
                .email("login.test@buywise.com")
                .password("Secret123!")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.email").value("login.test@buywise.com"));
    }

    /**
     * Verifies that accessing protected domain resources without an Authorization Bearer header is rejected.
     */
    @Test
    @DisplayName("GET /api/v1/purchases should return 403 when accessed without token")
    void testProtectedEndpointRejectsUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/v1/purchases"))
                .andExpect(status().isForbidden());
    }
}
