package com.mortgage.mortgageportal.dto;

import com.mortgage.mortgageportal.enums.DecisionStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DecisionResponseDTO {
    private UUID id;
    private UUID applicationId;
    private UUID officerId;
    private DecisionStatus status;
    private String comments;
    private LocalDateTime decidedAt;
}