package com.tbank.marketplace.controller;

import com.tbank.marketplace.api.PromoCodesApi;
import com.tbank.marketplace.model.PromoCodeCreate;
import org.springframework.http.ResponseEntity;

public class PromoCodeController implements PromoCodesApi {
    @Override
    public ResponseEntity<Void> createPromoCode(PromoCodeCreate promoCodeCreate) {
        return null;
    }
}
