package com.tbank.marketplace.service;

import com.tbank.marketplace.exceptions.promo_exceptions.PromoCodeAlreadyExistsException;
import com.tbank.marketplace.mapper.PromoCodeMapper;
import com.tbank.marketplace.model.PromoCodeCreate;
import com.tbank.marketplace.model.entity.PromoCode;
import com.tbank.marketplace.model.entity.User;
import com.tbank.marketplace.repository.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class PromoCodeService {
    private final PromoCodeRepository promoCodeRepository;
    private final PromoCodeMapper promoCodeMapper;

    @Transactional
    public void createPromoCode(PromoCodeCreate request, User currentUser) {
        if (promoCodeRepository.existsByCode(request.getCode())) {
            log.warn("Промокод уже существует: {}", request.getCode());
            throw new PromoCodeAlreadyExistsException("Промокод с кодом " + request.getCode() + " уже существует");
        }

        PromoCode promoCode = promoCodeMapper.toEntity(request, currentUser);

        PromoCode saved = promoCodeRepository.save(promoCode);
        log.info("Промокод создан: {} пользователем: {}", saved.getCode(), currentUser.getId());
    }
}
