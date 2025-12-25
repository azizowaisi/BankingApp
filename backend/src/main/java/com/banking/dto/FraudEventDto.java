package com.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudEventDto {
    private UUID id;
    private UUID userId;
    private String username;
    private String type;
    private LocalDateTime timestamp;
    private String description;
    private BigDecimal amount;
    private String severity;
}

