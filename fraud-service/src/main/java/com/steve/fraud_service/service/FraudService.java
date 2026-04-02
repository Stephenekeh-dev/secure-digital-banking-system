package com.steve.fraud_service.service;

import com.steve.fraud_service.model.FraudActivity;
import com.steve.fraud_service.repository.FraudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

// FIX: Original used @Autowired field injection — replaced with constructor injection via @RequiredArgsConstructor
@Slf4j
@Service
@RequiredArgsConstructor
public class FraudService {

    private final FraudRepository fraudRepository;

    @Transactional
    public FraudActivity flagActivity(FraudActivity request) {
        FraudActivity activity = FraudActivity.builder()
                .transactionId(request.getTransactionId())
                .userId(request.getUserId())
                .reason(request.getReason())
                .amount(request.getAmount())
                .build();

        FraudActivity saved = fraudRepository.save(activity);
        log.warn("Fraud activity flagged: transactionId={}, userId={}, reason={}",
                request.getTransactionId(), request.getUserId(), request.getReason());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<FraudActivity> getAllFraudActivities() {
        return fraudRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<FraudActivity> getFraudActivitiesByUser(String userId) {
        return fraudRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<FraudActivity> getFraudActivitiesByTransaction(String transactionId) {
        return fraudRepository.findByTransactionId(transactionId);
    }

    @Transactional(readOnly = true)
    public List<FraudActivity> getFraudActivitiesAboveAmount(BigDecimal threshold) {
        return fraudRepository.findByAmountGreaterThan(threshold);
    }

    @Transactional(readOnly = true)
    public Optional<FraudActivity> getFraudActivityById(Long id) {
        return fraudRepository.findById(id);
    }

    @Transactional
    public void deleteFraudActivity(Long id) {
        if (!fraudRepository.existsById(id)) {
            throw new NoSuchElementException("Fraud activity not found: " + id);
        }
        fraudRepository.deleteById(id);
        log.info("Fraud activity deleted: id={}", id);
    }
}