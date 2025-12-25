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

/**
 * REST Controller for money transfer operations.
 * Handles transfer execution and transaction history retrieval.
 * 
 * All endpoints require authentication via JWT token.
 * 
 * @author Banking Platform Team
 */
@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {
    
    private final TransferService transferService;
    
    /**
     * Executes a money transfer between two accounts.
     * 
     * Endpoint: POST /api/transfers
     * 
     * @param request Transfer request with fromIban, toIban, amount, description
     * @return Created transaction with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<TransactionDto> transfer(@Valid @RequestBody TransferRequest request) {
        TransactionDto transaction = transferService.transfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }
    
    /**
     * Retrieves transaction history for a specific account.
     * 
     * Endpoint: GET /api/transfers/history/{accountId}
     * 
     * Security: Only account owner or ADMIN can access.
     * 
     * @param accountId UUID of the account
     * @return List of transactions for the account
     */
    @GetMapping("/history/{accountId}")
    public ResponseEntity<List<TransactionDto>> getTransactionHistory(@PathVariable UUID accountId) {
        return ResponseEntity.ok(transferService.getTransactionHistory(accountId));
    }
}

