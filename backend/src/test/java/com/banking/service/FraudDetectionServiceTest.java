package com.banking.service;

import com.banking.entity.User;
import com.banking.repository.FraudEventRepository;
import com.banking.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudDetectionServiceTest {
    
    @Mock
    private FraudEventRepository fraudEventRepository;
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @InjectMocks
    private FraudDetectionService fraudDetectionService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(java.util.UUID.randomUUID())
            .username("testuser")
            .build();
        
        ReflectionTestUtils.setField(fraudDetectionService, "dailyTransferLimit", new BigDecimal("10000.00"));
        ReflectionTestUtils.setField(fraudDetectionService, "rapidTransferThreshold", 5);
        ReflectionTestUtils.setField(fraudDetectionService, "rapidTransferWindowMinutes", 60);
    }
    
    @Test
    void testCheckDailyLimit_WithinLimit() {
        when(transactionRepository.findByTimestampBetween(any(), any()))
            .thenReturn(Collections.emptyList());
        
        boolean result = fraudDetectionService.checkDailyLimit(testUser, new BigDecimal("100.00"));
        assertTrue(result);
    }
    
    @Test
    void testCheckDailyLimit_ExceedsLimit() {
        when(transactionRepository.findByTimestampBetween(any(), any()))
            .thenReturn(Collections.emptyList());
        
        boolean result = fraudDetectionService.checkDailyLimit(testUser, new BigDecimal("15000.00"));
        assertFalse(result);
        verify(fraudEventRepository, times(1)).save(any());
    }
}

