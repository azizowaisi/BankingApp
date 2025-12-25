package com.banking.service;

import com.banking.entity.AuditLog;
import com.banking.entity.User;
import com.banking.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {
    
    private final AuditLogRepository auditLogRepository;
    
    @Async
    @Transactional
    public void logAction(User user, AuditLog.AuditAction action, String details, String ipAddress) {
        AuditLog auditLog = AuditLog.builder()
            .user(user)
            .action(action)
            .timestamp(LocalDateTime.now())
            .details(details)
            .ipAddress(ipAddress)
            .build();
        
        auditLogRepository.save(auditLog);
    }
    
    @Async
    @Transactional
    public void logAction(AuditLog.AuditAction action, String details, String ipAddress) {
        AuditLog auditLog = AuditLog.builder()
            .action(action)
            .timestamp(LocalDateTime.now())
            .details(details)
            .ipAddress(ipAddress)
            .build();
        
        auditLogRepository.save(auditLog);
    }
}

