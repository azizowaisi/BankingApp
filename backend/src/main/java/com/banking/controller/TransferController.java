package com.banking.controller;

import com.banking.dto.TransferRequest;
import com.banking.dto.TransactionDto;
import com.banking.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {
    
    private final TransferService transferService;
    
    @PostMapping
    public ResponseEntity<TransactionDto> transfer(@Valid @RequestBody TransferRequest request) {
        TransactionDto transaction = transferService.transfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }
    
    @GetMapping("/history/{accountId}")
    public ResponseEntity<List<TransactionDto>> getTransactionHistory(@PathVariable UUID accountId) {
        return ResponseEntity.ok(transferService.getTransactionHistory(accountId));
    }
}

