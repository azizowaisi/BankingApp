package com.banking.dto;

import com.banking.entity.User;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateAccountRequest {
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal initialBalance;
    
    private UUID userId;
    private User.Role role;
}

