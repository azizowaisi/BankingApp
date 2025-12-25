package com.banking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Account entity representing a bank account.
 * 
 * Each account has:
 * - Unique IBAN (International Bank Account Number)
 * - Balance stored as DECIMAL(19,2) for precise monetary calculations
 * - Status (ACTIVE, FROZEN, CLOSED)
 * - Owner (User entity)
 * 
 * Indexes:
 * - IBAN index for fast lookups during transfers
 * - User ID index for efficient account queries per user
 * 
 * @author Banking Platform Team
 */
@Entity
@Table(name = "accounts", indexes = {
    @Index(name = "idx_iban", columnList = "iban", unique = true),
    @Index(name = "idx_user_id", columnList = "user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    
    /** Unique identifier for the account (UUID) */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    /** International Bank Account Number (IBAN) - unique identifier for transfers */
    @Column(nullable = false, unique = true, length = 34)
    private String iban;
    
    /** Account balance in DECIMAL(19,2) format for precise monetary calculations */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;
    
    /** Account status - determines if account can send/receive transfers */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;
    
    /** Account owner - lazy loaded to optimize queries */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * Account status enumeration.
     * 
     * ACTIVE: Account can send and receive transfers
     * FROZEN: Account temporarily frozen (admin action, fraud detection)
     * CLOSED: Account permanently closed
     */
    public enum AccountStatus {
        ACTIVE, FROZEN, CLOSED
    }
}

