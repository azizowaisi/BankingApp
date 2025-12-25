package com.banking.service;

import com.banking.dto.TransferRequest;
import com.banking.dto.TransactionDto;
import com.banking.entity.Account;
import com.banking.entity.AuditLog;
import com.banking.entity.Transaction;
import com.banking.entity.User;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for money transfer operations.
 * 
 * Implements secure, ACID-compliant money transfers with:
 * - Fraud detection (daily limits, rapid transfer detection)
 * - Balance validation
 * - Account status checks
 * - Complete audit trail
 * - Transaction rollback on any failure
 * 
 * All transfer operations are transactional to ensure data integrity.
 */
@Service
@RequiredArgsConstructor
public class TransferService {
    
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final FraudDetectionService fraudDetectionService;
    private final AuditService auditService;
    
    /**
     * Executes a money transfer between two accounts.
     * 
     * Process Flow:
     * 1. Fraud Detection: Checks rapid transfers and daily limits
     * 2. Security: Verifies user owns the sender account (or is ADMIN)
     * 3. Validation: Validates account status, balance, and transfer rules
     * 4. Execution: Atomically updates both account balances
     * 5. Recording: Creates transaction record and audit log
     * 
     * All steps are within a single transaction - any failure rolls back all changes.
     * 
     * @param request Transfer request with fromIban, toIban, amount, description
     * @return Transaction DTO with transfer details
     * @throws IllegalStateException if fraud checks fail or validation fails
     * @throws SecurityException if user doesn't own sender account
     * @throws IllegalArgumentException if accounts not found or invalid transfer
     */
    @Transactional
    public TransactionDto transfer(TransferRequest request) {
        User currentUser = getCurrentUser();
        
        // Step 1: Fraud detection - check for rapid transfer patterns
        if (!fraudDetectionService.checkRapidTransfers(currentUser)) {
            throw new IllegalStateException("Transfer rejected: Rapid transfer detected");
        }
        
        // Step 2: Fraud detection - check daily transfer limit
        if (!fraudDetectionService.checkDailyLimit(currentUser, request.getAmount())) {
            throw new IllegalStateException("Transfer rejected: Daily limit exceeded");
        }
        
        // Step 3: Load accounts from database
        Account senderAccount = accountRepository.findByIban(request.getFromIban())
            .orElseThrow(() -> new IllegalArgumentException("Sender account not found"));
        
        Account receiverAccount = accountRepository.findByIban(request.getToIban())
            .orElseThrow(() -> new IllegalArgumentException("Receiver account not found"));
        
        // Step 4: Security check - ensure user owns sender account (or is admin)
        if (!senderAccount.getUser().getId().equals(currentUser.getId()) &&
            currentUser.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Access denied: You can only transfer from your own accounts");
        }
        
        // Step 5: Validate transfer (balance, status, rules)
        validateTransfer(senderAccount, receiverAccount, request.getAmount());
        
        // Step 6: Execute transfer - update balances atomically
        senderAccount.setBalance(senderAccount.getBalance().subtract(request.getAmount()));
        receiverAccount.setBalance(receiverAccount.getBalance().add(request.getAmount()));
        
        accountRepository.save(senderAccount);
        accountRepository.save(receiverAccount);
        
        // Step 7: Create immutable transaction record
        Transaction transaction = Transaction.builder()
            .senderAccount(senderAccount)
            .receiverAccount(receiverAccount)
            .amount(request.getAmount())
            .timestamp(LocalDateTime.now())
            .status(Transaction.TransactionStatus.COMPLETED)
            .description(request.getDescription())
            .build();
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Step 8: Audit log for compliance and security
        auditService.logAction(currentUser, AuditLog.AuditAction.TRANSFER,
            String.format("Transfer: %s from %s to %s", 
                request.getAmount(), request.getFromIban(), request.getToIban()),
            null);
        
        return toDto(savedTransaction);
    }
    
    /**
     * Retrieves transaction history for a specific account.
     * 
     * Security: Only account owner or ADMIN can view transaction history.
     * 
     * @param accountId UUID of the account
     * @return List of transactions ordered by timestamp (newest first)
     * @throws SecurityException if user doesn't own the account and is not ADMIN
     */
    @Transactional(readOnly = true)
    public List<TransactionDto> getTransactionHistory(UUID accountId) {
        User currentUser = getCurrentUser();
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        // Security check: user must own account or be admin
        if (!account.getUser().getId().equals(currentUser.getId()) &&
            currentUser.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Access denied");
        }
        
        // Return all transactions where account is sender or receiver
        return transactionRepository
            .findBySenderAccountOrReceiverAccountOrderByTimestampDesc(account, account)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Retrieves all transactions in the system.
     * Admin-only operation for system monitoring and reporting.
     * 
     * @return List of all transactions
     * @throws SecurityException if user is not ADMIN
     */
    @Transactional(readOnly = true)
    public List<TransactionDto> getAllTransactions() {
        if (getCurrentUser().getRole() != User.Role.ADMIN) {
            throw new SecurityException("Only admins can view all transactions");
        }
        
        return transactionRepository.findAll()
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Validates transfer request before execution.
     * 
     * Validation Rules:
     * - Cannot transfer to same account
     * - Both accounts must be ACTIVE
     * - Amount must be positive
     * - Sender must have sufficient balance
     * 
     * @param sender Sender account
     * @param receiver Receiver account
     * @param amount Transfer amount
     * @throws IllegalArgumentException if validation fails
     * @throws IllegalStateException if account status or balance invalid
     */
    private void validateTransfer(Account sender, Account receiver, BigDecimal amount) {
        // Prevent self-transfers
        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
        
        // Check sender account is active
        if (sender.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new IllegalStateException("Sender account is not active");
        }
        
        // Check receiver account is active
        if (receiver.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new IllegalStateException("Receiver account is not active");
        }
        
        // Amount must be positive
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        
        // Check sufficient balance
        if (sender.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }
    }
    
    /**
     * Converts Transaction entity to DTO for API response.
     * 
     * @param transaction Transaction entity
     * @return TransactionDto with transfer details
     */
    private TransactionDto toDto(Transaction transaction) {
        return TransactionDto.builder()
            .id(transaction.getId())
            .fromIban(transaction.getSenderAccount().getIban())
            .toIban(transaction.getReceiverAccount().getIban())
            .amount(transaction.getAmount())
            .timestamp(transaction.getTimestamp())
            .status(transaction.getStatus().name())
            .description(transaction.getDescription())
            .build();
    }
    
    /**
     * Retrieves the currently authenticated user from Spring Security context.
     * 
     * @return Authenticated User entity
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}

