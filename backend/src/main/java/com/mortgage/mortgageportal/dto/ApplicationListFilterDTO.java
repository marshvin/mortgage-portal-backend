package com.mortgage.mortgageportal.dto;

import com.mortgage.mortgageportal.enums.ApplicationStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApplicationListFilterDTO {
    private ApplicationStatus status;
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
    private String nationalId;
}