package com.steve.account_service.service;



import com.steve.account_service.dto.AccountResponse;
import com.steve.account_service.dto.CreateAccountRequest;
import com.steve.account_service.dto.UpdateBalanceRequest;
import com.steve.account_service.entity.Account;
import com.steve.account_service.event.ApprovalEvent;
import com.steve.account_service.event.FraudCheckEvent;
import com.steve.account_service.event.TransactionEvent;
import com.steve.account_service.event.NotificationEvent;
import com.steve.account_service.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request, String userEmail) {

        // Validate accountType
        String accountType = request.getAccountType();
        if (accountType == null || accountType.isBlank()) {
            throw new IllegalArgumentException(
                    "Account type is required. Valid values: SAVINGS, CHECKING");
        }

        accountType = accountType.toUpperCase();
        if (!accountType.equals("SAVINGS") && !accountType.equals("CHECKING")) {
            throw new IllegalArgumentException(
                    "Invalid account type: " + accountType + ". Valid values: SAVINGS, CHECKING");
        }

        // Generate account number if not provided
        String accountNumber = request.getAccountNumber();
        if (accountNumber == null || accountNumber.isBlank()) {
            accountNumber = generateAccountNumber();
        }

        if (accountRepository.existsByAccountNumber(accountNumber)) {
            throw new IllegalArgumentException(
                    "Account number already exists: " + accountNumber);
        }

        // Default initial balance to zero if not provided
        BigDecimal initialBalance = request.getInitialBalance() != null
                ? request.getInitialBalance().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2);

        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }

        Account account = Account.builder()
                .userEmail(userEmail)
                .accountNumber(accountNumber)
                .accountType(accountType)
                .balance(initialBalance)
                .build();

        accountRepository.save(account);
        log.info("Account created: {} for user: {}", accountNumber, userEmail);
        return new AccountResponse(account);
    }
    @Transactional(readOnly = true)
    public AccountResponse getAccount(String accountNumber, String userEmail) {
        Account account = findAndVerifyOwnership(accountNumber, userEmail);
        return new AccountResponse(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByUser(String userEmail) {
        return accountRepository.findByUserEmail(userEmail)
                .stream()
                .map(AccountResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public AccountResponse updateBalance(UpdateBalanceRequest request, String userEmail) {
        Account account = findAndVerifyOwnership(request.getAccountNumber(), userEmail);
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        BigDecimal amount = request.getAmount().setScale(2, RoundingMode.HALF_UP);
        String operation = request.getOperation().toUpperCase();
        String eventType;

        switch (operation) {
            case "DEPOSIT" -> {
                account.setBalance(account.getBalance().add(amount));
                eventType = "DEPOSIT";
            }
            case "WITHDRAW" -> {
                if (account.getBalance().compareTo(amount) < 0) {
                    throw new IllegalStateException(
                            "Insufficient funds. Available: " + account.getBalance());
                }
                account.setBalance(account.getBalance().subtract(amount));
                eventType = "WITHDRAWAL";
            }
            default -> throw new IllegalArgumentException(
                    "Invalid operation: " + operation + ". Use DEPOSIT or WITHDRAW.");
        }

        accountRepository.save(account);

        // ── 1. Publish to transactions topic → transaction-service ────────────
        try {
            TransactionEvent transactionEvent = TransactionEvent.builder()
                    .accountNumber(account.getAccountNumber())
                    .amount(amount)
                    .type(eventType)
                    .userEmail(userEmail)
                    .build();
            kafkaTemplate.send("transactions", transactionEvent);
            log.info("Transaction event published: {} {} for account {}",
                    eventType, amount, account.getAccountNumber());
        } catch (Exception e) {
            log.warn("Could not publish transaction event: {}", e.getMessage());
        }

        // ── 2. Publish to notification-topic → notification-service ───────────
        try {
            NotificationEvent notificationEvent = NotificationEvent.builder()
                    .email(userEmail)
                    .subject("Banking Alert: " + eventType + " Successful")
                    .message("Dear customer,\n\n" +
                            "Your " + eventType.toLowerCase() + " of NGN " +
                            amount + " on account " +
                            account.getAccountNumber() + " was successful.\n\n" +
                            "New balance: NGN " + account.getBalance() +
                            "\n\nThank you for banking with us.")
                    .build();
            kafkaTemplate.send("notification-topic", notificationEvent);
            log.info("Notification event published to: {}", userEmail);
        } catch (Exception e) {
            log.warn("Could not publish notification event: {}", e.getMessage());
        }

        // ── 3. Publish to fraud-service ───────────────────────────────────────
        try {
            FraudCheckEvent fraudEvent = FraudCheckEvent.builder()
                    .transactionId(transactionId)
                    .userId(userEmail)           // ← userId maps to userEmail
                    .accountNumber(account.getAccountNumber())
                    .amount(amount)
                    .transactionType(eventType)  // "DEPOSIT" or "WITHDRAWAL" as String
                    .build();
            kafkaTemplate.send("fraud-check", fraudEvent);
        } catch (Exception e) {
            log.warn("Could not publish fraud check event: {}", e.getMessage());
        }

// ── 4. Publish to approval-service (only if amount > 10,000) ──────────
        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            try {
                ApprovalEvent approvalEvent = ApprovalEvent.builder()
                        .transactionId(UUID.randomUUID().toString())
                        .userEmail(userEmail)
                        .accountNumber(account.getAccountNumber())
                        .amount(amount)
                        .transactionType(eventType)
                        .build();
                kafkaTemplate.send("approval-events", approvalEvent);
                log.info("Approval event published for high-value transaction: {}",
                        amount);
            } catch (Exception e) {
                log.warn("Could not publish approval event: {}", e.getMessage());
            }
        }

        return new AccountResponse(account);
    }
    // ── Private Helpers ───────────────────────────────────────────────────────

    private Account findAndVerifyOwnership(String accountNumber, String userEmail) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NoSuchElementException("Account not found: " + accountNumber));

        if (!account.getUserEmail().equals(userEmail)) {
            throw new SecurityException("Access denied: you do not own this account");
        }
        return account;
    }

    private String generateAccountNumber() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}