package com.tbank.marketplace.repository;

import com.tbank.marketplace.model.entity.PromoCode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PromoCodeRepository extends JpaRepository<PromoCode, UUID> {
    Optional<PromoCode> findByCode(String code);

    boolean existsByCode(@NotNull @Pattern(regexp = "^[A-Z0-9_]{4,20}$") String code);
}
