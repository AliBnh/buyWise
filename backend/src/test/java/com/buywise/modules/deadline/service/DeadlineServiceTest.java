package com.buywise.modules.deadline.service;

import com.buywise.common.security.UserPrincipal;
import com.buywise.modules.deadline.dto.DeadlineDto;
import com.buywise.modules.deadline.model.Deadline;
import com.buywise.modules.deadline.model.DeadlineType;
import com.buywise.modules.deadline.model.Reminder;
import com.buywise.modules.deadline.repository.DeadlineRepository;
import com.buywise.modules.deadline.repository.ReminderRepository;
import com.buywise.modules.purchase.model.Purchase;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeadlineServiceTest {

    @Mock
    private DeadlineRepository deadlineRepository;

    @Mock
    private ReminderRepository reminderRepository;

    @InjectMocks
    private DeadlineService deadlineService;

    @BeforeEach
    void setUp() {
        UserPrincipal testUserPrincipal = new UserPrincipal(
                1L, "test@buywise.com", "pass", "Test User", "MAD",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                testUserPrincipal, null, testUserPrincipal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Should register purchase deadlines for warranty and return window")
    void shouldRegisterPurchaseDeadlines() {
        LocalDate today = LocalDate.now();
        Purchase purchase = Purchase.builder()
                .id(10L)
                .userId(1L)
                .productName("Smart TV 55 inch")
                .purchaseDate(today.minusDays(5))
                .returnDeadline(today.plusDays(9))
                .warrantyExpirationDate(today.plusYears(2))
                .pricePaid(new BigDecimal("6999.00"))
                .currency("MAD")
                .build();

        when(deadlineRepository.existsByPurchaseIdAndDeadlineType(10L, DeadlineType.RETURN_WINDOW)).thenReturn(false);
        when(deadlineRepository.existsByPurchaseIdAndDeadlineType(10L, DeadlineType.WARRANTY_EXPIRY)).thenReturn(false);
        when(deadlineRepository.save(any(Deadline.class))).thenAnswer(inv -> {
            Deadline d = inv.getArgument(0);
            d.setId(99L);
            return d;
        });

        deadlineService.registerPurchaseDeadlines(purchase);

        verify(deadlineRepository, times(2)).save(any(Deadline.class));
        verify(reminderRepository, atLeastOnce()).save(any(Reminder.class));
    }

    @Test
    @DisplayName("Should mark deadline as completed")
    void shouldMarkDeadlineAsCompleted() {
        Deadline deadline = Deadline.builder()
                .id(50L)
                .userId(1L)
                .title("Submit Rebate Form")
                .deadlineType(DeadlineType.CUSTOM)
                .dueDate(LocalDate.now().plusDays(3))
                .isCompleted(false)
                .build();

        when(deadlineRepository.findByIdAndUserId(50L, 1L)).thenReturn(Optional.of(deadline));
        when(deadlineRepository.save(any(Deadline.class))).thenAnswer(inv -> inv.getArgument(0));

        DeadlineDto result = deadlineService.markCompleted(50L);

        assertThat(result.getIsCompleted()).isTrue();
        verify(deadlineRepository).save(deadline);
    }
}
