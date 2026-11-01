package com.buywise.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * =================================================================================================
 * ARCHITECTURAL DESIGN NOTE: STATELESS JWT AUTHENTICATION & CRYPTOGRAPHIC INTEGRITY
 * =================================================================================================
 * Architectural Principles:
 * 1. Stateless Authentication & Horizontal Scalability:
 *    - Traditional session-based authentication requires server-side state (sticky sessions or Redis session store).
 *    - JWT enables 100% stateless authentication: the token contains signed claims (subject userId, email, expiration).
 *    - Any microservice or backend container replica can verify authenticity independently using the shared secret key,
 *      allowing seamless horizontal scaling behind round-robin load balancers.
 *
 * 2. Cryptographic Security (HMAC-SHA256):
 *    - Tokens are digitally signed with an HMAC-SHA256 key (`Keys.hmacShaKeyFor`).
 *    - Any tampering with claims (e.g. attempting to change userId from 2 to 1) invalidates the signature,
 *      causing automatic rejection by the parser.
 * =================================================================================================
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long jwtExpirationMs;

    public JwtTokenProvider(
            @Value("${buywise.jwt.secret}") String jwtSecret,
            @Value("${buywise.jwt.expiration-ms:86400000}") long jwtExpirationMs) {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return generateTokenFromUserId(userPrincipal.getId(), userPrincipal.getEmail());
    }

    public String generateTokenFromUserId(Long userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Invalid JWT token: {}", ex.getMessage());
        }
        return false;
    }
}
