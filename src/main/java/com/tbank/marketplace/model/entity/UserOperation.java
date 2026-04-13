package com.tbank.marketplace.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_operations")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    private OperationType operationType;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum OperationType {
        CREATE_ORDER, UPDATE_ORDER
    }

    public UserOperation(UUID userId, OperationType operationType) {
        this.userId = userId;
        this.operationType = operationType;
    }
}