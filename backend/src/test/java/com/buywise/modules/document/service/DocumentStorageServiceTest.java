package com.buywise.modules.document.service;

import com.buywise.common.exception.UnauthorizedException;
import com.buywise.common.security.UserPrincipal;
import com.buywise.modules.ai.service.DocumentUnderstandingAiService;
import com.buywise.modules.document.dto.ExtractedFactDto;
import com.buywise.modules.document.dto.FactVerificationRequest;
import com.buywise.modules.document.factory.DocumentProcessorFactory;
import com.buywise.modules.document.model.Document;
import com.buywise.modules.document.model.ExtractedFact;
import com.buywise.modules.document.model.VerificationStatus;
import com.buywise.modules.document.repository.DocumentRepository;
import com.buywise.modules.document.repository.ExtractedFactRepository;
import com.buywise.modules.purchase.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentStorageServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private ExtractedFactRepository factRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private DocumentProcessorFactory processorFactory;

    @Mock
    private DocumentUnderstandingAiService aiService;

    @InjectMocks
    private DocumentStorageService documentStorageService;

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
    @DisplayName("Should verify fact and update its verification status")
    void shouldVerifyFact() {
        Document doc = Document.builder()
                .id(10L)
                .userId(1L)
                .fileName("receipt.pdf")
                .build();

        ExtractedFact fact = ExtractedFact.builder()
                .id(100L)
                .document(doc)
                .factKey("PURCHASE_PRICE")
                .extractedValue("4999.00 MAD")
                .verificationStatus(VerificationStatus.PENDING_VERIFICATION)
                .build();

        when(factRepository.findById(100L)).thenReturn(Optional.of(fact));
        when(factRepository.save(any(ExtractedFact.class))).thenAnswer(inv -> inv.getArgument(0));

        FactVerificationRequest request = FactVerificationRequest.builder()
                .verificationStatus(VerificationStatus.VERIFIED)
                .verifiedValue("4999.00")
                .build();

        ExtractedFactDto result = documentStorageService.verifyFact(100L, request);

        assertThat(result.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
        assertThat(result.getVerifiedValue()).isEqualTo("4999.00");
        verify(factRepository).save(fact);
    }

    @Test
    @DisplayName("Should reject verification if user does not own document")
    void shouldRejectVerificationWhenNotOwner() {
        Document doc = Document.builder()
                .id(10L)
                .userId(999L) // Different user
                .fileName("receipt.pdf")
                .build();

        ExtractedFact fact = ExtractedFact.builder()
                .id(100L)
                .document(doc)
                .factKey("PURCHASE_PRICE")
                .extractedValue("4999.00 MAD")
                .build();

        when(factRepository.findById(100L)).thenReturn(Optional.of(fact));

        FactVerificationRequest request = FactVerificationRequest.builder()
                .verificationStatus(VerificationStatus.VERIFIED)
                .build();

        assertThatThrownBy(() -> documentStorageService.verifyFact(100L, request))
                .isInstanceOf(UnauthorizedException.class);
    }
}
