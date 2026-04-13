package com.tbank.marketplace.repository;

import com.tbank.marketplace.model.entity.UserOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface UserOperationsRepository extends JpaRepository<UserOperation, UUID> {

    @Query("SELECT uo.createdAt FROM UserOperation uo " +
            "WHERE uo.userId = :userId AND uo.operationType = :operationType " +
            "ORDER BY uo.createdAt DESC LIMIT 1")
    Optional<LocalDateTime> findLastOperationTimeByUserIdAndType(
            @Param("userId") UUID userId,
            @Param("operationType") UserOperation.OperationType operationType
    );
}
