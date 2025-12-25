package com.banking.controller;

import com.banking.dto.AccountDto;
import com.banking.dto.CreateAccountRequest;
import com.banking.entity.Account;
import com.banking.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for account management operations.
 * Handles account creation, retrieval, and status updates.
 * 
 * All endpoints require authentication via JWT token.
 * Status updates require ADMIN role.
 * 
 * @author Banking Platform Team
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    
    private final AccountService accountService;
    
    /**
     * Creates a new bank account for the authenticated user.
     * 
     * Endpoint: POST /api/accounts
     * 
     * @param request Account creation request with optional initial balance
     * @return Created account with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountDto account = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }
    
    /**
     * Retrieves all accounts for the authenticated user.
     * 
     * Endpoint: GET /api/accounts
     * 
     * @return List of user's accounts
     */
    @GetMapping
    public ResponseEntity<List<AccountDto>> getUserAccounts() {
        return ResponseEntity.ok(accountService.getUserAccounts());
    }
    
    /**
     * Retrieves account details by IBAN.
     * 
     * Endpoint: GET /api/accounts/{iban}
     * 
     * Security: User must own the account or be ADMIN.
     * 
     * @param iban International Bank Account Number
     * @return Account details
     */
    @GetMapping("/{iban}")
    public ResponseEntity<AccountDto> getAccountByIban(@PathVariable String iban) {
        return ResponseEntity.ok(accountService.getAccountByIban(iban));
    }
    
    /**
     * Updates account status (ACTIVE, FROZEN, CLOSED).
     * 
     * Endpoint: PUT /api/accounts/{accountId}/status?status={status}
     * 
     * Security: Requires ADMIN role.
     * 
     * @param accountId UUID of the account
     * @param status New account status
     * @return Updated account details
     */
    @PutMapping("/{accountId}/status")
    public ResponseEntity<AccountDto> updateAccountStatus(
            @PathVariable UUID accountId,
            @RequestParam Account.AccountStatus status) {
        return ResponseEntity.ok(accountService.updateAccountStatus(accountId, status));
    }
}

