package com.steve.transaction_service;



import com.steve.transaction_service.dto.CreateTransactionRequest;
import com.steve.transaction_service.dto.TransactionResponse;
import com.steve.transaction_service.entity.Transaction;
import com.steve.transaction_service.entity.TransactionType;
import com.steve.transaction_service.repository.TransactionRepository;
import com.steve.transaction_service.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @InjectMocks private TransactionService transactionService;

    private static final String USER_EMAIL = "user@bank.com";
    private static final String ACCOUNT_NUM = "ACC123456789";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createTransaction_deposit_succeeds() {
        when(transactionRepository.save(any())).thenAnswer(i -> {
            Transaction t = i.getArgument(0);
            t = Transaction.builder()
                    .id(1L)
                    .accountNumber(t.getAccountNumber())
                    .userEmail(t.getUserEmail())
                    .amount(t.getAmount())
                    .type(t.getType())
                    .build();
            return t;
        });

        CreateTransactionRequest req = CreateTransactionRequest.builder()
                .accountNumber(ACCOUNT_NUM)
                .amount(BigDecimal.valueOf(500))
                .type("DEPOSIT")
                .build();

        TransactionResponse response = transactionService.createTransaction(req, USER_EMAIL);

        assertThat(response.getType()).isEqualTo("DEPOSIT");
        assertThat(response.getAmount()).isEqualByComparingTo("500");
        assertThat(response.getUserEmail()).isEqualTo(USER_EMAIL);
    }

    @Test
    void createTransaction_transfer_requiresTargetAccount() {
        CreateTransactionRequest req = CreateTransactionRequest.builder()
                .accountNumber(ACCOUNT_NUM)
                .amount(BigDecimal.valueOf(200))
                .type("TRANSFER")
                .build(); // no targetAccount

        assertThatThrownBy(() -> transactionService.createTransaction(req, USER_EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("targetAccount is required");
    }

    @Test
    void createTransaction_invalidType_throws() {
        CreateTransactionRequest req = CreateTransactionRequest.builder()
                .accountNumber(ACCOUNT_NUM)
                .amount(BigDecimal.TEN)
                .type("REFUND")
                .build();

        assertThatThrownBy(() -> transactionService.createTransaction(req, USER_EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid transaction type");
    }

    @Test
    void createTransaction_transfer_withTargetAccount_succeeds() {
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CreateTransactionRequest req = CreateTransactionRequest.builder()
                .accountNumber(ACCOUNT_NUM)
                .amount(BigDecimal.valueOf(150))
                .type("TRANSFER")
                .targetAccount("TARGET987654")
                .build();

        TransactionResponse response = transactionService.createTransaction(req, USER_EMAIL);

        assertThat(response.getType()).isEqualTo("TRANSFER");
        assertThat(response.getTargetAccount()).isEqualTo("TARGET987654");
    }

    @Test
    void getUserTransactions_returnsListOrderedByDate() {
        List<Transaction> transactions = List.of(
                Transaction.builder()
                        .id(1L)
                        .accountNumber(ACCOUNT_NUM)
                        .userEmail(USER_EMAIL)
                        .amount(BigDecimal.valueOf(100))
                        .type(TransactionType.DEPOSIT)
                        .createdAt(LocalDateTime.now())          // ← add this
                        .build(),
                Transaction.builder()
                        .id(2L)
                        .accountNumber(ACCOUNT_NUM)
                        .userEmail(USER_EMAIL)
                        .amount(BigDecimal.valueOf(200))
                        .type(TransactionType.WITHDRAWAL)
                        .createdAt(LocalDateTime.now().minusHours(1))  // ← add this
                        .build()
        );

        when(transactionRepository.findByUserEmailOrderByCreatedAtDesc(USER_EMAIL))
                .thenReturn(transactions);

        List<TransactionResponse> result = transactionService.getUserTransactions(USER_EMAIL);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getType()).isEqualTo("DEPOSIT");
    }
}