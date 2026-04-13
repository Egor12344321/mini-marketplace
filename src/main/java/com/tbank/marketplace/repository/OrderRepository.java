package com.tbank.marketplace.repository;

import com.tbank.marketplace.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("SELECT COUNT(o) > 0 FROM Order o WHERE o.userId = :userId AND o.status IN ('CREATED', 'PAYMENT_PENDING')")
    boolean hasActiveOrders(@Param("userId") UUID userId);

    List<Order> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Order> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.status IN :statuses ORDER BY o.createdAt DESC LIMIT 1")
    Optional<Order> findActiveOrderByUserId(@Param("userId") UUID userId, @Param("statuses") List<Order.OrderStatus> statuses);

    long countByUserIdAndCreatedAtAfter(UUID userId, LocalDateTime since);
}
