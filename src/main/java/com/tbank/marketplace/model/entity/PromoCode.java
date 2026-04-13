package com.tbank.marketplace.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "promo_codes")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "min_order_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    @Column(name = "max_uses", nullable = false)
    private Integer maxUses;

    @Column(name = "current_uses", nullable = false)
    private Integer currentUses = 0;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;

    @Column(nullable = false)
    private Boolean active = true;

    public enum DiscountType {
        PERCENTAGE, FIXED_AMOUNT
    }

    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return active && currentUses < maxUses &&
                now.isAfter(validFrom) && now.isBefore(validUntil);
    }

    public boolean meetsMinAmount(BigDecimal amount) {
        return amount.compareTo(minOrderAmount) >= 0;
    }

    public BigDecimal calculateDiscount(BigDecimal amount) {
        if (discountType == DiscountType.PERCENTAGE) {
            BigDecimal discount = amount.multiply(discountValue.divide(BigDecimal.valueOf(100)));
            BigDecimal maxDiscount = amount.multiply(BigDecimal.valueOf(0.7));
            return discount.min(maxDiscount);
        } else {
            return discountValue.min(amount);
        }
    }

    public void incrementUses() {
        this.currentUses++;
    }

    public void decrementUses() {
        if (this.currentUses > 0) {
            this.currentUses--;
        }
    }
}