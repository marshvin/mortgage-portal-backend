package com.mortgage.mortgageportal.dto;

import com.mortgage.mortgageportal.enums.ApplicationStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ApplicationResponseDTO {
    private UUID id;
    private UUID userId;
    private ApplicationStatus status;
    private BigDecimal amount;
    private Integer loanTermMonths;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}