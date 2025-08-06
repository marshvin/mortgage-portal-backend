package com.mortgage.mortgageportal.service.impl;

import com.mortgage.mortgageportal.dto.ApplicationRequestDTO;
import com.mortgage.mortgageportal.entities.Application;
import com.mortgage.mortgageportal.entities.User;
import com.mortgage.mortgageportal.enums.ApplicationStatus;
import com.mortgage.mortgageportal.enums.UserRole;
import com.mortgage.mortgageportal.mapper.ApplicationMapper;
import com.mortgage.mortgageportal.service.ApplicationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ApplicationServiceImpl implements ApplicationService {
    
    // For demo purposes, using in-memory storage
    // In production, this would be a JPA repository
    private final List<Application> applications = new ArrayList<>();
    
    @Override
    public Application createApplication(ApplicationRequestDTO request, User user) {
        Application app = ApplicationMapper.toEntity(request, user);
        app.setStatus(ApplicationStatus.PENDING);
        applications.add(app);
        return app;
    }
    
    @Override
    public Application getApplicationById(UUID id, User requester) {
        Application app = applications.stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Application not found"));
        
        // Check access control
        if (requester.getRole() == UserRole.APPLICANT && !app.getUser().getId().equals(requester.getId())) {
            throw new RuntimeException("Access denied: You can only view your own applications");
        }
        
        return app;
    }
    
    @Override
    public List<Application> listApplications(ApplicationStatus status, LocalDateTime createdFrom, LocalDateTime createdTo, String nationalId) {
        return applications.stream()
                .filter(app -> status == null || app.getStatus() == status)
                .filter(app -> createdFrom == null || app.getCreatedAt().isAfter(createdFrom))
                .filter(app -> createdTo == null || app.getCreatedAt().isBefore(createdTo))
                .filter(app -> nationalId == null || app.getUser().getNationalId().equals(nationalId))
                .toList();
    }
} 