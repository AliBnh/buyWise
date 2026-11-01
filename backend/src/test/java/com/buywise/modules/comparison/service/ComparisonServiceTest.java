package com.buywise.modules.comparison.service;

import com.buywise.common.exception.ResourceNotFoundException;
import com.buywise.common.security.UserPrincipal;
import com.buywise.modules.comparison.dto.ComparisonResponse;
import com.buywise.modules.comparison.dto.CreateComparisonRequest;
import com.buywise.modules.comparison.dto.CreateOfferRequest;
import com.buywise.modules.comparison.model.Comparison;
import com.buywise.modules.comparison.model.ComparisonStatus;
import com.buywise.modules.comparison.repository.ComparisonRepository;
import com.buywise.modules.comparison.repository.OfferRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComparisonServiceTest {

    @Mock
    private ComparisonRepository comparisonRepository;

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private ComparisonEngineService comparisonEngineService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ComparisonService comparisonService;

    @BeforeEach
    void setUp() {
        UserPrincipal testUserPrincipal = new UserPrincipal(
                1L, "user@buywise.com", "pass", "Test User", "MAD",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                testUserPrincipal, null, testUserPrincipal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Should create comparison with initial offers")
    void shouldCreateComparisonWithInitialOffers() {
        CreateComparisonRequest request = CreateComparisonRequest.builder()
                .title("Smart TV Comparison")
                .categoryName("Televisions")
                .notes("Testing 4K models")
                .initialOffers(List.of(
                        CreateOfferRequest.builder()
                                .sellerName("ElectroPlanet")
                                .title("Samsung 55 Crystal UHD")
                                .currency("MAD")
                                .basePrice(new BigDecimal("5499.00"))
                                .deliveryFee(new BigDecimal("50.00"))
                                .warrantyMonths(24)
                                .returnWindowDays(14)
                                .build()
                ))
                .build();

        when(comparisonRepository.save(any(Comparison.class))).thenAnswer(inv -> {
            Comparison c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        ComparisonResponse response = comparisonService.createComparison(request);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Smart TV Comparison");
        assertThat(response.getOffers()).hasSize(1);
        verify(comparisonRepository).save(any(Comparison.class));
    }

    @Test
    @DisplayName("Should get comparison detail and throw ResourceNotFoundException when absent")
    void shouldThrowWhenComparisonNotFound() {
        when(comparisonRepository.findByIdAndUserIdWithOffers(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> comparisonService.getComparisonById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should list comparisons belonging to authenticated user")
    void shouldListUserComparisons() {
        Comparison c = Comparison.builder()
                .id(1L)
                .userId(1L)
                .title("Gaming Monitor")
                .categoryName("Displays")
                .status(ComparisonStatus.DRAFT)
                .build();

        when(comparisonRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(c));

        List<ComparisonResponse> results = comparisonService.getUserComparisons();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Gaming Monitor");
    }
}
