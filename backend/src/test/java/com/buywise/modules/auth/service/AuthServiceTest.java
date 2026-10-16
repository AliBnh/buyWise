package com.buywise.modules.auth.service;

import com.buywise.common.exception.BusinessException;
import com.buywise.common.security.JwtTokenProvider;
import com.buywise.modules.auth.dto.AuthResponse;
import com.buywise.modules.auth.dto.LoginRequest;
import com.buywise.modules.auth.dto.RegisterRequest;
import com.buywise.modules.auth.model.Role;
import com.buywise.modules.auth.model.User;
import com.buywise.modules.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Service — User Onboarding & Authentication Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .email("consumer@buywise.com")
                .fullName("Test Consumer")
                .passwordHash("hashed_password")
                .role(Role.ROLE_USER)
                .preferredCurrency("MAD")
                .build();
    }

    @Test
    @DisplayName("Should register new user successfully with encrypted password and JWT token")
    void testRegisterSuccess() {
        RegisterRequest req = RegisterRequest.builder()
                .email("consumer@buywise.com")
                .fullName("Test Consumer")
                .password("Password123!")
                .preferredCurrency("MAD")
                .build();

        when(userRepository.existsByEmail("consumer@buywise.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(tokenProvider.generateTokenFromUserId(1L, "consumer@buywise.com")).thenReturn("mock.jwt.token");

        AuthResponse response = authService.register(req);

        assertNotNull(response);
        assertEquals("consumer@buywise.com", response.getEmail());
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("ROLE_USER", response.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when registering an already registered email")
    void testRegisterDuplicateEmail() {
        RegisterRequest req = RegisterRequest.builder()
                .email("existing@buywise.com")
                .fullName("Existing User")
                .password("Password123!")
                .build();

        when(userRepository.existsByEmail("existing@buywise.com")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.register(req));
        assertTrue(ex.getMessage().contains("already exists"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should authenticate user and return valid token on correct credentials")
    void testLoginSuccess() {
        LoginRequest req = LoginRequest.builder()
                .email("consumer@buywise.com")
                .password("Password123!")
                .build();

        Authentication authMock = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authMock);
        when(userRepository.findByEmail("consumer@buywise.com")).thenReturn(Optional.of(sampleUser));
        when(tokenProvider.generateToken(authMock)).thenReturn("valid.session.token");

        AuthResponse response = authService.login(req);

        assertNotNull(response);
        assertEquals("valid.session.token", response.getToken());
        assertEquals("consumer@buywise.com", response.getEmail());
    }
}
