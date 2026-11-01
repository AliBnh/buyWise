package com.buywise.common.security;

import com.buywise.common.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility helper to safely extract authenticated user context.
 * 
 * Demonstrates:
 * - Defensive Security Programming: Derives authenticated user context strictly from the server-verified
 *   SecurityContext rather than trusting user IDs passed in request payloads or query parameters.
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // Utility class
    }

    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new UnauthorizedException("User is not authenticated");
        }
        return (UserPrincipal) authentication.getPrincipal();
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
