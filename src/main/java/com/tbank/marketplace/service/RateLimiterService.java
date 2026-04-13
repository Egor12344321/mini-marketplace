package com.tbank.marketplace.service;

import com.tbank.marketplace.model.entity.UserOperation;
import com.tbank.marketplace.repository.UserOperationsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimiterService {

    private final UserOperationsRepository userOperationRepository;

    @Value("${rate-limit.order-create.minutes:5}")
    private long limitMinutes;

    public boolean canCreateOrder(UUID userId) {
        var lastOperationTime = userOperationRepository.findLastOperationTimeByUserIdAndType(userId, UserOperation.OperationType.CREATE_ORDER);

        if (lastOperationTime.isEmpty()) {
            log.debug("Пользователь еще не создавал заказов: {}", userId);
            return true;
        }

        LocalDateTime lastTime = lastOperationTime.get();
        LocalDateTime now = LocalDateTime.now();

        long minutesPassed = ChronoUnit.MINUTES.between(lastTime, now);

        if (minutesPassed >= limitMinutes) {
            log.debug("Количество запросов для пользователя {} для создания заказа не превышено, с прошлого создания прошло: {}", userId, minutesPassed);
            return true;
        }

        long remainingMinutes = limitMinutes - minutesPassed;
        log.warn("Превышен лимит запросов на создание заказа для пользователя: {}. Может сделать следующий запрос через {} мин", userId, remainingMinutes);
        return false;
    }

    public long getRemainingMinutes(UUID userId) {
        var lastOperationTime = userOperationRepository.findLastOperationTimeByUserIdAndType(userId, UserOperation.OperationType.CREATE_ORDER);

        if (lastOperationTime.isEmpty()) {
            return 0;
        }

        long minutesPassed = ChronoUnit.MINUTES.between(lastOperationTime.get(), LocalDateTime.now());
        return Math.max(0, limitMinutes - minutesPassed);
    }
}