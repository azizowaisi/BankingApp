package com.banking.repository;

import com.banking.entity.Account;
import com.banking.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findBySenderAccountOrReceiverAccountOrderByTimestampDesc(
        Account senderAccount, Account receiverAccount);
    List<Transaction> findBySenderAccountIdOrReceiverAccountIdOrderByTimestampDesc(
        UUID senderAccountId, UUID receiverAccountId);
    List<Transaction> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    long countBySenderAccountAndTimestampAfter(Account account, LocalDateTime timestamp);
}

