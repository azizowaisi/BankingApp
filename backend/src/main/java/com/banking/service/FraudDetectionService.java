package com.banking.service;

import com.banking.entity.Account;
import com.banking.entity.FraudEvent;
import com.banking.entity.Transaction;
import com.banking.entity.User;
import com.banking.repository.FraudEventRepository;
import com.banking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FraudDetectionService {
    
    private final FraudEventRepository fraudEventRepository;
    private final TransactionRepository transactionRepository;
    
    @Value("${banking.fraud.daily-transfer-limit}")
    private BigDecimal dailyTransferLimit;
    
    @Value("${banking.fraud.rapid-transfer-threshold}")
    private int rapidTransferThreshold;
    
    @Value("${banking.fraud.rapid-transfer-window-minutes}")
    private int rapidTransferWindowMinutes;
    
    @Transactional(readOnly = true)
    public boolean checkDailyLimit(User user, BigDecimal amount) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime now = LocalDateTime.now();
        
        BigDecimal dailyTotal = transactionRepository
            .findByTimestampBetween(startOfDay, now)
            .stream()
            .filter(t -> t.getSenderAccount().getUser().getId().equals(user.getId()))
            .filter(t -> t.getStatus() == Transaction.TransactionStatus.COMPLETED)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal projectedTotal = dailyTotal.add(amount);
        
        if (projectedTotal.compareTo(dailyTransferLimit) > 0) {
            logFraudEvent(user, FraudEvent.FraudType.DAILY_LIMIT_EXCEEDED,
                String.format("Daily limit exceeded. Daily total: %s, Attempted: %s", 
                    dailyTotal, amount),
                amount, FraudEvent.FraudSeverity.HIGH);
            return false;
        }
        
        return true;
    }
    
    @Transactional(readOnly = true)
    public boolean checkRapidTransfers(User user) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(rapidTransferWindowMinutes);
        LocalDateTime now = LocalDateTime.now();
        
        long transferCount = transactionRepository
            .findByTimestampBetween(windowStart, now)
            .stream()
            .filter(t -> t.getSenderAccount().getUser().getId().equals(user.getId()))
            .count();
        
        if (transferCount >= rapidTransferThreshold) {
            logFraudEvent(user, FraudEvent.FraudType.RAPID_TRANSFERS,
                String.format("Rapid transfer detected. Count: %d in last %d minutes", 
                    transferCount, rapidTransferWindowMinutes),
                null, FraudEvent.FraudSeverity.MEDIUM);
            return false;
        }
        
        return true;
    }
    
    @Transactional
    public void logFraudEvent(User user, FraudEvent.FraudType type, String description,
                             BigDecimal amount, FraudEvent.FraudSeverity severity) {
        FraudEvent fraudEvent = FraudEvent.builder()
            .user(user)
            .type(type)
            .timestamp(LocalDateTime.now())
            .description(description)
            .amount(amount)
            .severity(severity)
            .build();
        
        fraudEventRepository.save(fraudEvent);
    }
}

