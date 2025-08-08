package com.mortgage.mortgageportal.event;

import com.mortgage.mortgageportal.dto.ApplicationResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationEventPayload {
    private ApplicationResponseDTO application;
    private EventMetadata metadata;
    private String operation; // CREATE, UPDATE, DELETE
} 