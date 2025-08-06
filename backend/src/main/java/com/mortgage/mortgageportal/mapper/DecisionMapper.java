package com.mortgage.mortgageportal.mapper;

import com.mortgage.mortgageportal.dto.DecisionRequestDTO;
import com.mortgage.mortgageportal.dto.DecisionResponseDTO;
import com.mortgage.mortgageportal.entities.Application;
import com.mortgage.mortgageportal.entities.Decision;
import com.mortgage.mortgageportal.entities.User;

public class DecisionMapper {
    public static Decision toEntity(DecisionRequestDTO dto, Application application, User officer) {
        return Decision.builder()
                .application(application)
                .officer(officer)
                .status(dto.getStatus())
                .comments(dto.getComments())
                .build();
    }

    public static DecisionResponseDTO toResponseDTO(Decision decision) {
        DecisionResponseDTO dto = new DecisionResponseDTO();
        dto.setId(decision.getId());
        dto.setApplicationId(decision.getApplication().getId());
        dto.setOfficerId(decision.getOfficer().getId());
        dto.setStatus(decision.getStatus());
        dto.setComments(decision.getComments());
        dto.setDecidedAt(decision.getDecidedAt());
        return dto;
    }
}