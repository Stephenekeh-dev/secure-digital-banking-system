package com.steve.transaction_service.repository;


import com.steve.transaction_service.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserEmailOrderByCreatedAtDesc(String userEmail);
    List<Transaction> findByAccountNumberOrderByCreatedAtDesc(String accountNumber);
}