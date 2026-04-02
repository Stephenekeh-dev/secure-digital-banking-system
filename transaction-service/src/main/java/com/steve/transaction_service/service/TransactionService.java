package com.steve.transaction_service.service;

import com.steve.transaction_service.dto.CreateTransactionRequest;
import com.steve.transaction_service.dto.TransactionResponse;
import com.steve.transaction_service.entity.Transaction;
import com.steve.transaction_service.entity.TransactionType;
import com.steve.transaction_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.steve.transaction_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request, String userEmail) {
        TransactionType type;
        try {
            type = TransactionType.valueOf(request.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid transaction type: " + request.getType()
                    + ". Valid values: DEPOSIT, WITHDRAWAL, TRANSFER");
        }

        if (type == TransactionType.TRANSFER && (request.getTargetAccount() == null
                || request.getTargetAccount().isBlank())) {
            throw new IllegalArgumentException("targetAccount is required for TRANSFER transactions");
        }

        Transaction tx = Transaction.builder()
                .accountNumber(request.getAccountNumber())
                .amount(request.getAmount())
                .type(type)
                .targetAccount(request.getTargetAccount())
                .userEmail(userEmail)
                .build();

        transactionRepository.save(tx);
        log.info("Transaction created: id={} type={} amount={}", tx.getId(), type, request.getAmount());
        return new TransactionResponse(tx);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getUserTransactions(String userEmail) {
        return transactionRepository.findByUserEmailOrderByCreatedAtDesc(userEmail)
                .stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getAccountTransactions(String accountNumber) {
        return transactionRepository.findByAccountNumberOrderByCreatedAtDesc(accountNumber)
                .stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
    }
}
