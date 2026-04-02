package com.steve.fraud_service;

import com.steve.fraud_service.model.FraudActivity;
import com.steve.fraud_service.repository.FraudRepository;
import com.steve.fraud_service.service.FraudService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FraudServiceTest {

    @Mock private FraudRepository fraudRepository;
    @InjectMocks private FraudService fraudService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void flagActivity_savesAndReturns() {
        FraudActivity request = FraudActivity.builder()
                .transactionId("TX-001")
                .userId("user@bank.com")
                .reason("Suspicious large transfer")
                .amount(BigDecimal.valueOf(50_000))
                .build();

        FraudActivity saved = FraudActivity.builder()
                .id(1L)
                .transactionId("TX-001")
                .userId("user@bank.com")
                .reason("Suspicious large transfer")
                .amount(BigDecimal.valueOf(50_000))
                .timestamp(LocalDateTime.now())
                .build();

        when(fraudRepository.save(any())).thenReturn(saved);

        FraudActivity result = fraudService.flagActivity(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTransactionId()).isEqualTo("TX-001");
        assertThat(result.getAmount()).isEqualByComparingTo("50000");
        verify(fraudRepository, times(1)).save(any());
    }

    @Test
    void getFraudActivitiesByUser_returnsCorrectList() {
        List<FraudActivity> activities = List.of(
                FraudActivity.builder().id(1L).userId("user@bank.com")
                        .transactionId("TX-001").reason("Suspicious").amount(BigDecimal.TEN)
                        .timestamp(LocalDateTime.now()).build()
        );
        when(fraudRepository.findByUserId("user@bank.com")).thenReturn(activities);

        List<FraudActivity> result = fraudService.getFraudActivitiesByUser("user@bank.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo("user@bank.com");
    }

    @Test
    void getFraudActivitiesByTransaction_returnsCorrectList() {
        List<FraudActivity> activities = List.of(
                FraudActivity.builder().id(1L).transactionId("TX-999")
                        .userId("u1").reason("Duplicate").amount(BigDecimal.valueOf(200))
                        .timestamp(LocalDateTime.now()).build()
        );
        when(fraudRepository.findByTransactionId("TX-999")).thenReturn(activities);

        List<FraudActivity> result = fraudService.getFraudActivitiesByTransaction("TX-999");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTransactionId()).isEqualTo("TX-999");
    }

    @Test
    void deleteFraudActivity_deletesSuccessfully() {
        when(fraudRepository.existsById(1L)).thenReturn(true);
        doNothing().when(fraudRepository).deleteById(1L);

        assertThatCode(() -> fraudService.deleteFraudActivity(1L)).doesNotThrowAnyException();
        verify(fraudRepository).deleteById(1L);
    }

    @Test
    void deleteFraudActivity_throwsWhenNotFound() {
        when(fraudRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> fraudService.deleteFraudActivity(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Fraud activity not found");
    }
}