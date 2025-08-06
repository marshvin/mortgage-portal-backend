package com.mortgage.mortgageportal.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ApplicationRequestDTO {
    private BigDecimal amount;
    private Integer loanTermMonths;
}