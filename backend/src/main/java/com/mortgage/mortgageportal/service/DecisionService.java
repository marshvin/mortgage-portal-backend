package com.mortgage.mortgageportal.service;

import com.mortgage.mortgageportal.dto.DecisionRequestDTO;
import com.mortgage.mortgageportal.entities.Decision;
import com.mortgage.mortgageportal.entities.User;

import java.util.UUID;

public interface DecisionService {
    Decision decideApplication(UUID applicationId, DecisionRequestDTO request, User officer);
}