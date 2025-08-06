package com.mortgage.mortgageportal.dto;

import com.mortgage.mortgageportal.enums.DecisionStatus;
import lombok.Data;

@Data
public class DecisionRequestDTO {
    private DecisionStatus status;
    private String approverName;
    private String comments;
}