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

/**
 * Fraud Detection Service for monitoring and preventing suspicious transactions.
 * 
 * Implements two main fraud detection mechanisms:
 * 1. Daily Transfer Limit: Prevents excessive transfers within 24 hours
 * 2. Rapid Transfer Detection: Detects multiple transfers within a time window
 * 
 * All fraud events are logged for audit and monitoring purposes.
 * 
 * @author Banking Platform Team
 */
@Service
@RequiredArgsConstructor
public class FraudDetectionService {
    
    private final FraudEventRepository fraudEventRepository;
    private final TransactionRepository transactionRepository;
    
    /** Maximum total amount a user can transfer per day (configurable) */
    @Value("${banking.fraud.daily-transfer-limit}")
    private BigDecimal dailyTransferLimit;
    
    /** Maximum number of transfers allowed within the time window */
    @Value("${banking.fraud.rapid-transfer-threshold}")
    private int rapidTransferThreshold;
    
    /** Time window in minutes for rapid transfer detection */
    @Value("${banking.fraud.rapid-transfer-window-minutes}")
    private int rapidTransferWindowMinutes;
    
    /**
     * Checks if the transfer amount would exceed the daily transfer limit.
     * 
     * Calculates total transfers made by user today (since midnight) and adds
     * the current transfer amount. If projected total exceeds limit, logs fraud event.
     * 
     * @param user User attempting the transfer
     * @param amount Transfer amount to check
     * @return true if transfer is within daily limit, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean checkDailyLimit(User user, BigDecimal amount) {
        // Calculate start of current day (00:00:00)
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime now = LocalDateTime.now();
        
        // Sum all completed transfers by this user today
        BigDecimal dailyTotal = transactionRepository
            .findByTimestampBetween(startOfDay, now)
            .stream()
            .filter(t -> t.getSenderAccount().getUser().getId().equals(user.getId()))
            .filter(t -> t.getStatus() == Transaction.TransactionStatus.COMPLETED)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate projected total including current transfer
        BigDecimal projectedTotal = dailyTotal.add(amount);
        
        // Check if projected total exceeds daily limit
        if (projectedTotal.compareTo(dailyTransferLimit) > 0) {
            // Log fraud event with HIGH severity
            logFraudEvent(user, FraudEvent.FraudType.DAILY_LIMIT_EXCEEDED,
                String.format("Daily limit exceeded. Daily total: %s, Attempted: %s", 
                    dailyTotal, amount),
                amount, FraudEvent.FraudSeverity.HIGH);
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks for rapid transfer patterns (multiple transfers within time window).
     * 
     * Counts transfers made by user within the configured time window.
     * If count exceeds threshold, logs fraud event.
     * 
     * @param user User attempting the transfer
     * @return true if transfer count is within threshold, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean checkRapidTransfers(User user) {
        // Calculate time window start
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(rapidTransferWindowMinutes);
        LocalDateTime now = LocalDateTime.now();
        
        // Count transfers by this user within the time window
        long transferCount = transactionRepository
            .findByTimestampBetween(windowStart, now)
            .stream()
            .filter(t -> t.getSenderAccount().getUser().getId().equals(user.getId()))
            .count();
        
        // Check if transfer count exceeds threshold
        if (transferCount >= rapidTransferThreshold) {
            // Log fraud event with MEDIUM severity
            logFraudEvent(user, FraudEvent.FraudType.RAPID_TRANSFERS,
                String.format("Rapid transfer detected. Count: %d in last %d minutes", 
                    transferCount, rapidTransferWindowMinutes),
                null, FraudEvent.FraudSeverity.MEDIUM);
            return false;
        }
        
        return true;
    }
    
    /**
     * Logs a fraud event to the database for audit and monitoring.
     * 
     * Fraud events are used for:
     * - Security monitoring
     * - Compliance reporting
     * - Pattern analysis
     * - Admin notifications
     * 
     * @param user User associated with the fraud event
     * @param type Type of fraud detected
     * @param description Detailed description of the event
     * @param amount Transfer amount (if applicable)
     * @param severity Severity level (LOW, MEDIUM, HIGH, CRITICAL)
     */
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

