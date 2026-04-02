package com.steve.fraud_service.repository;

import com.steve.fraud_service.model.FraudActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FraudRepository extends JpaRepository<FraudActivity, Long> {

    // Find all fraud activities for a specific user
    List<FraudActivity> findByUserId(String userId);

    // Find all fraud activities for a specific transaction
    List<FraudActivity> findByTransactionId(String transactionId);

    // Optional: Find all fraud activities with amount greater than a threshold
    List<FraudActivity> findByAmountGreaterThan(BigDecimal amount);
}