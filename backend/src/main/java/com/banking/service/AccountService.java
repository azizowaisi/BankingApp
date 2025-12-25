package com.banking.service;

import com.banking.dto.AccountDto;
import com.banking.dto.CreateAccountRequest;
import com.banking.entity.Account;
import com.banking.entity.AuditLog;
import com.banking.entity.User;
import com.banking.repository.AccountRepository;
import com.banking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {
    
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    
    @Transactional
    public AccountDto createAccount(CreateAccountRequest request) {
        User currentUser = getCurrentUser();
        
        if (request.getRole() == User.Role.ADMIN && 
            currentUser.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Only admins can create accounts for other users");
        }
        
        User accountOwner = request.getUserId() != null 
            ? userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
            : currentUser;
        
        String iban = generateIban();
        while (accountRepository.existsByIban(iban)) {
            iban = generateIban();
        }
        
        Account account = Account.builder()
            .iban(iban)
            .balance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO)
            .status(Account.AccountStatus.ACTIVE)
            .user(accountOwner)
            .build();
        
        Account savedAccount = accountRepository.save(account);
        
        auditService.logAction(currentUser, AuditLog.AuditAction.ACCOUNT_CREATED,
            "Account created: " + savedAccount.getIban(), null);
        
        return toDto(savedAccount);
    }
    
    @Transactional(readOnly = true)
    public List<AccountDto> getUserAccounts() {
        User currentUser = getCurrentUser();
        return accountRepository.findByUser(currentUser)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AccountDto> getAllAccounts() {
        if (getCurrentUser().getRole() != User.Role.ADMIN) {
            throw new SecurityException("Only admins can view all accounts");
        }
        
        return accountRepository.findAll()
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public AccountDto getAccountByIban(String iban) {
        Account account = accountRepository.findByIban(iban)
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != User.Role.ADMIN && 
            !account.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Access denied");
        }
        
        return toDto(account);
    }
    
    @Transactional
    public AccountDto updateAccountStatus(UUID accountId, Account.AccountStatus status) {
        if (getCurrentUser().getRole() != User.Role.ADMIN) {
            throw new SecurityException("Only admins can update account status");
        }
        
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        Account.AccountStatus oldStatus = account.getStatus();
        account.setStatus(status);
        Account savedAccount = accountRepository.save(account);
        
        AuditLog.AuditAction action = status == Account.AccountStatus.FROZEN 
            ? AuditLog.AuditAction.ACCOUNT_FROZEN 
            : AuditLog.AuditAction.ACCOUNT_UNFROZEN;
        
        auditService.logAction(getCurrentUser(), action,
            String.format("Account %s status changed from %s to %s", 
                account.getIban(), oldStatus, status), null);
        
        return toDto(savedAccount);
    }
    
    private String generateIban() {
        return "SE" + String.format("%02d", (int)(Math.random() * 100)) + 
               String.format("%016d", (long)(Math.random() * 10000000000000000L));
    }
    
    private AccountDto toDto(Account account) {
        return AccountDto.builder()
            .id(account.getId())
            .iban(account.getIban())
            .balance(account.getBalance())
            .status(account.getStatus().name())
            .userId(account.getUser().getId())
            .userName(account.getUser().getUsername())
            .build();
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}

