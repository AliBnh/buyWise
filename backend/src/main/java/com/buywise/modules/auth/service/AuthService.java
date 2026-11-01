package com.buywise.modules.auth.service;

import com.buywise.common.exception.BusinessException;
import com.buywise.common.exception.ResourceNotFoundException;
import com.buywise.common.security.JwtTokenProvider;
import com.buywise.common.security.SecurityUtils;
import com.buywise.modules.auth.dto.AuthResponse;
import com.buywise.modules.auth.dto.LoginRequest;
import com.buywise.modules.auth.dto.RegisterRequest;
import com.buywise.modules.auth.dto.UpdateProfileRequest;
import com.buywise.modules.auth.dto.UserProfileResponse;
import com.buywise.modules.auth.model.Role;
import com.buywise.modules.auth.model.User;
import com.buywise.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service orchestrating user authentication, registration, and profile management.
 * 
 * Demonstrates:
 * - Single Responsibility Principle (SRP): Concentrates business rules around onboarding and credentials.
 * - Dependency Injection: All required dependencies injected via constructor.
 * - Transaction Management: Read-only transactions for queries; atomic read-write transactions for registration.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new BusinessException("An account with email '" + request.getEmail() + "' already exists.");
        }

        String currency = (request.getPreferredCurrency() != null && !request.getPreferredCurrency().isBlank())
                ? request.getPreferredCurrency().toUpperCase().trim()
                : "MAD";

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .fullName(request.getFullName().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .preferredCurrency(currency)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Registered new user with id: {}", savedUser.getId());

        String token = tokenProvider.generateTokenFromUserId(savedUser.getId(), savedUser.getEmail());

        return AuthResponse.builder()
                .token(token)
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole().name())
                .preferredCurrency(savedUser.getPreferredCurrency())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        String token = tokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .preferredCurrency(user.getPreferredCurrency())
                .build();
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .preferredCurrency(user.getPreferredCurrency())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional
    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }

        if (request.getPreferredCurrency() != null && !request.getPreferredCurrency().isBlank()) {
            user.setPreferredCurrency(request.getPreferredCurrency().toUpperCase().trim());
        }

        User updated = userRepository.save(user);
        log.info("Updated profile for user id: {}", updated.getId());

        return UserProfileResponse.builder()
                .id(updated.getId())
                .email(updated.getEmail())
                .fullName(updated.getFullName())
                .role(updated.getRole().name())
                .preferredCurrency(updated.getPreferredCurrency())
                .createdAt(updated.getCreatedAt())
                .build();
    }
}
