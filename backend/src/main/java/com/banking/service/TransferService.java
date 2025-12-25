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

@Service
@RequiredArgsConstructor
public class TransferService {
    
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final FraudDetectionService fraudDetectionService;
    private final AuditService auditService;
    
    @Transactional
    public TransactionDto transfer(TransferRequest request) {
        User currentUser = getCurrentUser();
        
        // Fraud checks
        if (!fraudDetectionService.checkRapidTransfers(currentUser)) {
            throw new IllegalStateException("Transfer rejected: Rapid transfer detected");
        }
        
        if (!fraudDetectionService.checkDailyLimit(currentUser, request.getAmount())) {
            throw new IllegalStateException("Transfer rejected: Daily limit exceeded");
        }
        
        Account senderAccount = accountRepository.findByIban(request.getFromIban())
            .orElseThrow(() -> new IllegalArgumentException("Sender account not found"));
        
        Account receiverAccount = accountRepository.findByIban(request.getToIban())
            .orElseThrow(() -> new IllegalArgumentException("Receiver account not found"));
        
        // Security check
        if (!senderAccount.getUser().getId().equals(currentUser.getId()) &&
            currentUser.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Access denied: You can only transfer from your own accounts");
        }
        
        // Validation
        validateTransfer(senderAccount, receiverAccount, request.getAmount());
        
        // Perform transfer
        senderAccount.setBalance(senderAccount.getBalance().subtract(request.getAmount()));
        receiverAccount.setBalance(receiverAccount.getBalance().add(request.getAmount()));
        
        accountRepository.save(senderAccount);
        accountRepository.save(receiverAccount);
        
        // Create transaction record
        Transaction transaction = Transaction.builder()
            .senderAccount(senderAccount)
            .receiverAccount(receiverAccount)
            .amount(request.getAmount())
            .timestamp(LocalDateTime.now())
            .status(Transaction.TransactionStatus.COMPLETED)
            .description(request.getDescription())
            .build();
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Audit log
        auditService.logAction(currentUser, AuditLog.AuditAction.TRANSFER,
            String.format("Transfer: %s from %s to %s", 
                request.getAmount(), request.getFromIban(), request.getToIban()),
            null);
        
        return toDto(savedTransaction);
    }
    
    @Transactional(readOnly = true)
    public List<TransactionDto> getTransactionHistory(UUID accountId) {
        User currentUser = getCurrentUser();
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        if (!account.getUser().getId().equals(currentUser.getId()) &&
            currentUser.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Access denied");
        }
        
        return transactionRepository
            .findBySenderAccountOrReceiverAccountOrderByTimestampDesc(account, account)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
    
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
    
    private void validateTransfer(Account sender, Account receiver, BigDecimal amount) {
        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
        
        if (sender.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new IllegalStateException("Sender account is not active");
        }
        
        if (receiver.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new IllegalStateException("Receiver account is not active");
        }
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        
        if (sender.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }
    }
    
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
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}

