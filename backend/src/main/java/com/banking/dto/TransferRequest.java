package com.banking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {
    @NotBlank
    private String fromIban;
    
    @NotBlank
    private String toIban;
    
    @DecimalMin(value = "0.01")
    private BigDecimal amount;
    
    private String description;
}

