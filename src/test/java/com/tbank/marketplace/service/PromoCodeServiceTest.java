package com.tbank.marketplace.service;

import com.tbank.marketplace.exceptions.promo_exceptions.PromoCodeAlreadyExistsException;
import com.tbank.marketplace.mapper.PromoCodeMapper;
import com.tbank.marketplace.model.PromoCodeCreate;
import com.tbank.marketplace.model.entity.PromoCode;
import com.tbank.marketplace.model.entity.User;
import com.tbank.marketplace.repository.PromoCodeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromoCodeServiceTest {

    @Mock
    private PromoCodeRepository promoCodeRepository;
    @Mock
    private PromoCodeMapper promoCodeMapper;

    @InjectMocks
    private PromoCodeService promoCodeService;

    @Test
    void createPromoCode_savesNewCode() {
        when(promoCodeRepository.existsByCode("SAVE10")).thenReturn(false);
        PromoCode promoCode = PromoCode.builder().code("SAVE10").build();
        when(promoCodeMapper.toEntity(any(PromoCodeCreate.class), any(User.class))).thenReturn(promoCode);
        when(promoCodeRepository.save(promoCode)).thenReturn(promoCode);

        promoCodeService.createPromoCode(promoCodeCreate(), user());

        verify(promoCodeRepository).save(promoCode);
    }

    @Test
    void createPromoCode_duplicate_throwsAlreadyExists() {
        when(promoCodeRepository.existsByCode("SAVE10")).thenReturn(true);

        assertThatThrownBy(() -> promoCodeService.createPromoCode(promoCodeCreate(), user()))
                .isInstanceOf(PromoCodeAlreadyExistsException.class);

        verify(promoCodeRepository, never()).save(any(PromoCode.class));
    }

    private User user() {
        return User.builder()
                .id(UUID.randomUUID())
                .email("admin@example.com")
                .role(User.UserRole.ADMIN)
                .build();
    }

    private PromoCodeCreate promoCodeCreate() {
        PromoCodeCreate request = new PromoCodeCreate();
        request.setCode("SAVE10");
        return request;
    }
}