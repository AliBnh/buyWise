package com.buywise.common.security;

import com.buywise.modules.auth.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * =================================================================================================
 * ARCHITECTURAL DESIGN NOTE: ADAPTER PATTERN (GoF) & SINGLE RESPONSIBILITY PRINCIPLE (SRP)
 * =================================================================================================
 * Design Patterns & Principles:
 * 1. Adapter Pattern (Structural GoF):
 *    - Target Interface: {@link UserDetails} (Spring Security's required authentication contract).
 *    - Adaptee: {@link User} (Our clean JPA database domain entity).
 *    - Adapter: {@link UserPrincipal} (Implements target UserDetails while wrapping/adapting domain User).
 *
 * 2. Single Responsibility Principle (SRP) & Architectural Purity:
 *    - Domain entities (such as {@link User}) should model business state and database persistence,
 *      free of third-party framework details (authorities, account expiration flags, credentials status).
 *    - By introducing {@link UserPrincipal} as an Adapter, Spring Security logic is cleanly isolated
 *      from the core business model. If we migrate from Spring Security to another framework or protocol,
 *      the User entity remains 100% untouched.
 *
 * 3. Static Factory Method Pattern:
 *    - {@link #create(User)} encapsulates mapping domain user state (roles, email, ID) into authorities
 *      and principal properties in a clean, immutable manner.
 *
 * Technical Interview Q&A Crib Sheet:
 * Q: "Why not simply make the User JPA entity implement UserDetails directly?"
 * A: Making JPA entities implement framework interfaces creates tight architectural coupling,
 *    violates SRP, and can cause subtle serialization/JSON bugs or lazy-loading session leaks
 *    when Spring Security serializes UserDetails into the SecurityContext or HTTP sessions.
 * =================================================================================================
 */
@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String fullName;
    private final String preferredCurrency;
    private final Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(User user) {
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getFullName(),
                user.getPreferredCurrency(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
