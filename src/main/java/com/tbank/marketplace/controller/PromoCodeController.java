package com.tbank.marketplace.controller;

import com.tbank.marketplace.api.PromoCodesApi;
import com.tbank.marketplace.model.PromoCodeCreate;
import org.springframework.http.ResponseEntity;
import com.tbank.marketplace.api.PromoCodesApi;
import com.tbank.marketplace.model.PromoCodeCreate;
import com.tbank.marketplace.model.entity.User;
import com.tbank.marketplace.service.PromoCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PromoCodeController implements PromoCodesApi {

    private final PromoCodeService promoCodeService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    @Override
    public ResponseEntity<Void> createPromoCode(PromoCodeCreate promoCodeCreate) {
        User currentUser = getCurrentUser();
        log.debug("Stared creating promo: {} by {}", promoCodeCreate.getCode(), currentUser.getId());

        promoCodeService.createPromoCode(promoCodeCreate, currentUser);

        log.info("Создан новый промокод {} пользователем {}", promoCodeCreate.getCode(), currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}