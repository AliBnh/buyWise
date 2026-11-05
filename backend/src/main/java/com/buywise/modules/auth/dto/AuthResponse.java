package com.buywise.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication response payload returning the signed JWT access token and user identity profile.
 * 
 * Demonstrates:
 * - RFC 6750 Bearer Token Standard: Sets tokenType to "Bearer" for HTTP Authorization headers.
 * - Client Session Bootstrap: Packages user preferences (preferredCurrency) to initialize client frontend state.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long id;
    private String email;
    private String fullName;
    private String role;
    private String preferredCurrency;
}
