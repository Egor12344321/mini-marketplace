package com.tbank.marketplace.service;

import com.tbank.marketplace.model.entity.UserOperation;
import com.tbank.marketplace.repository.UserOperationsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimiterServiceTest {

    @Mock
    private UserOperationsRepository userOperationRepository;

    private RateLimiterService rateLimiterService;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        rateLimiterService = new RateLimiterService(userOperationRepository);
        ReflectionTestUtils.setField(rateLimiterService, "createLimitMinutes", 5L);
        ReflectionTestUtils.setField(rateLimiterService, "updateLimitMinutes", 1L);
    }

    @Test
    void canCreateOrder_noHistory_returnsTrue() {
        when(userOperationRepository.findLastOperationTimeByUserIdAndType(any(), any()))
                .thenReturn(Optional.empty());

        assertThat(rateLimiterService.canCreateOrder(userId)).isTrue();
    }

    @Test
    void canCreateOrder_recentOperation_returnsFalse() {
        when(userOperationRepository.findLastOperationTimeByUserIdAndType(any(), any()))
                .thenReturn(Optional.of(LocalDateTime.now().minusMinutes(2)));

        assertThat(rateLimiterService.canCreateOrder(userId)).isFalse();
    }

    @Test
    void canCreateOrder_afterWindow_returnsTrue() {
        when(userOperationRepository.findLastOperationTimeByUserIdAndType(any(), any()))
                .thenReturn(Optional.of(LocalDateTime.now().minusMinutes(10)));

        assertThat(rateLimiterService.canCreateOrder(userId)).isTrue();
    }

    @Test
    void canUpdateOrder_recentOperation_returnsFalse() {
        when(userOperationRepository.findLastOperationTimeByUserIdAndType(any(), any()))
                .thenReturn(Optional.of(LocalDateTime.now().minusSeconds(30)));

        assertThat(rateLimiterService.canUpdateOrder(userId)).isFalse();
    }

    @Test
    void canUpdateOrder_afterWindow_returnsTrue() {
        when(userOperationRepository.findLastOperationTimeByUserIdAndType(any(), any()))
                .thenReturn(Optional.of(LocalDateTime.now().minusMinutes(2)));

        assertThat(rateLimiterService.canUpdateOrder(userId)).isTrue();
    }

    @Test
    void getRemainingMinutesForCreate_returnsDifference() {
        when(userOperationRepository.findLastOperationTimeByUserIdAndType(any(), any()))
                .thenReturn(Optional.of(LocalDateTime.now().minusMinutes(3)));

        assertThat(rateLimiterService.getRemainingMinutesForCreate(userId)).isEqualTo(2);
    }

    @Test
    void getRemainingMinutesForCreate_noHistory_returnsZero() {
        when(userOperationRepository.findLastOperationTimeByUserIdAndType(any(), any()))
                .thenReturn(Optional.empty());

        assertThat(rateLimiterService.getRemainingMinutesForCreate(userId)).isEqualTo(0);
    }

    @Test
    void getRemainingMinutesForUpdate_returnsWholeWindowWhenFresh() {
        when(userOperationRepository.findLastOperationTimeByUserIdAndType(any(), any()))
                .thenReturn(Optional.of(LocalDateTime.now()));

        assertThat(rateLimiterService.getRemainingMinutesForUpdate(userId)).isEqualTo(1);
    }
}