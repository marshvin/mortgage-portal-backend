package com.mortgage.mortgageportal.mapper;

import com.mortgage.mortgageportal.dto.ApplicationRequestDTO;
import com.mortgage.mortgageportal.dto.ApplicationResponseDTO;
import com.mortgage.mortgageportal.entities.Application;
import com.mortgage.mortgageportal.entities.User;

public class ApplicationMapper {
    public static Application toEntity(ApplicationRequestDTO dto, User user) {
        return Application.builder()
                .user(user)
                .amount(dto.getAmount())
                .loanTermMonths(dto.getLoanTermMonths())
                .build();
    }

    public static ApplicationResponseDTO toResponseDTO(Application app) {
        ApplicationResponseDTO dto = new ApplicationResponseDTO();
        dto.setId(app.getId());
        dto.setUserId(app.getUser().getId());
        dto.setStatus(app.getStatus());
        dto.setAmount(app.getAmount());
        dto.setLoanTermMonths(app.getLoanTermMonths());
        dto.setCreatedAt(app.getCreatedAt());
        dto.setUpdatedAt(app.getUpdatedAt());
        return dto;
    }
}