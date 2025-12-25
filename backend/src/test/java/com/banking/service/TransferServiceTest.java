package com.banking.service;

import com.banking.dto.TransferRequest;
import com.banking.entity.Account;
import com.banking.entity.User;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {
    
    @Mock
    private AccountRepository accountRepository;
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private FraudDetectionService fraudDetectionService;
    
    @Mock
    private AuditService auditService;
    
    @Mock
    private SecurityContext securityContext;
    
    @Mock
    private Authentication authentication;
    
    @InjectMocks
    private TransferService transferService;
    
    private User testUser;
    private Account senderAccount;
    private Account receiverAccount;
    
    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(java.util.UUID.randomUUID())
            .username("testuser")
            .role(User.Role.CUSTOMER)
            .build();
        
        senderAccount = Account.builder()
            .id(java.util.UUID.randomUUID())
            .iban("SE1234567890123456789012")
            .balance(new BigDecimal("1000.00"))
            .status(Account.AccountStatus.ACTIVE)
            .user(testUser)
            .build();
        
        receiverAccount = Account.builder()
            .id(java.util.UUID.randomUUID())
            .iban("SE9876543210987654321098")
            .balance(new BigDecimal("500.00"))
            .status(Account.AccountStatus.ACTIVE)
            .user(testUser)
            .build();
        
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
    }
    
    @Test
    void testTransfer_Success() {
        TransferRequest request = new TransferRequest();
        request.setFromIban("SE1234567890123456789012");
        request.setToIban("SE9876543210987654321098");
        request.setAmount(new BigDecimal("100.00"));
        
        when(accountRepository.findByIban("SE1234567890123456789012"))
            .thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByIban("SE9876543210987654321098"))
            .thenReturn(Optional.of(receiverAccount));
        when(fraudDetectionService.checkRapidTransfers(testUser)).thenReturn(true);
        when(fraudDetectionService.checkDailyLimit(testUser, request.getAmount())).thenReturn(true);
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        
        assertDoesNotThrow(() -> transferService.transfer(request));
        verify(accountRepository, times(2)).save(any());
        verify(transactionRepository, times(1)).save(any());
    }
    
    @Test
    void testTransfer_InsufficientBalance() {
        TransferRequest request = new TransferRequest();
        request.setFromIban("SE1234567890123456789012");
        request.setToIban("SE9876543210987654321098");
        request.setAmount(new BigDecimal("2000.00"));
        
        when(accountRepository.findByIban("SE1234567890123456789012"))
            .thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByIban("SE9876543210987654321098"))
            .thenReturn(Optional.of(receiverAccount));
        when(fraudDetectionService.checkRapidTransfers(testUser)).thenReturn(true);
        when(fraudDetectionService.checkDailyLimit(testUser, request.getAmount())).thenReturn(true);
        
        assertThrows(IllegalStateException.class, () -> transferService.transfer(request));
    }
}

