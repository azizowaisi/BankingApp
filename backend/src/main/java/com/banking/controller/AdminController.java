package com.banking.controller;

import com.banking.dto.AccountDto;
import com.banking.dto.AuditLogDto;
import com.banking.dto.FraudEventDto;
import com.banking.dto.TransactionDto;
import com.banking.repository.AuditLogRepository;
import com.banking.repository.FraudEventRepository;
import com.banking.service.AccountService;
import com.banking.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final AccountService accountService;
    private final TransferService transferService;
    private final AuditLogRepository auditLogRepository;
    private final FraudEventRepository fraudEventRepository;
    
    @GetMapping("/accounts")
    public ResponseEntity<List<AccountDto>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }
    
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDto>> getAllTransactions() {
        return ResponseEntity.ok(transferService.getAllTransactions());
    }
    
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLogDto>> getAuditLogs() {
        List<AuditLogDto> logs = auditLogRepository.findAllByOrderByTimestampDesc()
            .stream()
            .map(log -> AuditLogDto.builder()
                .id(log.getId())
                .action(log.getAction().name())
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .username(log.getUser() != null ? log.getUser().getUsername() : null)
                .timestamp(log.getTimestamp())
                .details(log.getDetails())
                .ipAddress(log.getIpAddress())
                .build())
            .collect(Collectors.toList());
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/fraud-events")
    public ResponseEntity<List<FraudEventDto>> getFraudEvents() {
        List<FraudEventDto> events = fraudEventRepository.findAllByOrderByTimestampDesc()
            .stream()
            .map(event -> FraudEventDto.builder()
                .id(event.getId())
                .userId(event.getUser().getId())
                .username(event.getUser().getUsername())
                .type(event.getType().name())
                .timestamp(event.getTimestamp())
                .description(event.getDescription())
                .amount(event.getAmount())
                .severity(event.getSeverity().name())
                .build())
            .collect(Collectors.toList());
        return ResponseEntity.ok(events);
    }
}

