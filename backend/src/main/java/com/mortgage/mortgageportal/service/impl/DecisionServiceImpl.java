package com.mortgage.mortgageportal.service.impl;

import com.mortgage.mortgageportal.dto.DecisionRequestDTO;
import com.mortgage.mortgageportal.entities.Application;
import com.mortgage.mortgageportal.entities.Decision;
import com.mortgage.mortgageportal.entities.User;
import com.mortgage.mortgageportal.enums.ApplicationStatus;
import com.mortgage.mortgageportal.mapper.DecisionMapper;
import com.mortgage.mortgageportal.service.ApplicationService;
import com.mortgage.mortgageportal.service.DecisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DecisionServiceImpl implements DecisionService {
    
    private final ApplicationService applicationService;
    
    // For demo purposes, using in-memory storage
    // In production, this would be a JPA repository
    private final List<Decision> decisions = new ArrayList<>();
    
    @Override
    public Decision decideApplication(UUID applicationId, DecisionRequestDTO request, User officer) {
        // Get the application
        Application application = applicationService.getApplicationById(applicationId, officer);
        
        // Check if application is already decided
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new RuntimeException("Application is already decided");
        }
        
        // Create decision
        Decision decision = DecisionMapper.toEntity(request, application, officer);
        decisions.add(decision);
        
        // Update application status
        application.setStatus(request.getStatus() == com.mortgage.mortgageportal.enums.DecisionStatus.APPROVED 
                ? ApplicationStatus.APPROVED 
                : ApplicationStatus.REJECTED);
        
        return decision;
    }
} 