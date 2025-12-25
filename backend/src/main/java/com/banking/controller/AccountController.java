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

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    
    private final AccountService accountService;
    
    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountDto account = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }
    
    @GetMapping
    public ResponseEntity<List<AccountDto>> getUserAccounts() {
        return ResponseEntity.ok(accountService.getUserAccounts());
    }
    
    @GetMapping("/{iban}")
    public ResponseEntity<AccountDto> getAccountByIban(@PathVariable String iban) {
        return ResponseEntity.ok(accountService.getAccountByIban(iban));
    }
    
    @PutMapping("/{accountId}/status")
    public ResponseEntity<AccountDto> updateAccountStatus(
            @PathVariable UUID accountId,
            @RequestParam Account.AccountStatus status) {
        return ResponseEntity.ok(accountService.updateAccountStatus(accountId, status));
    }
}

