package com.tbank.marketplace.mapper;

import com.tbank.marketplace.model.PromoCodeCreate;
import com.tbank.marketplace.model.entity.PromoCode;
import com.tbank.marketplace.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PromoCodeMapper {

    public PromoCode toEntity(PromoCodeCreate request, User currentUser) {
        return PromoCode.builder()
                .code(request.getCode())
                .discountType(mapDiscountType(request.getDiscountType()))
                .discountValue(request.getDiscountValue())
                .minOrderAmount(request.getMinOrderAmount())
                .maxUses(request.getMaxUses())
                .validFrom(request.getValidFrom().toLocalDateTime())
                .validUntil(request.getValidUntil().toLocalDateTime())
                .currentUses(0)
                .active(true)
                .build();
    }

    private PromoCode.DiscountType mapDiscountType(PromoCodeCreate.DiscountTypeEnum sourceType) {
        return PromoCode.DiscountType.valueOf(sourceType.name());
    }
}