package com.mortgage.mortgageportal.service.impl;

import com.mortgage.mortgageportal.dto.ApplicationRequestDTO;
import com.mortgage.mortgageportal.entities.Application;
import com.mortgage.mortgageportal.entities.User;
import com.mortgage.mortgageportal.enums.ApplicationStatus;
import com.mortgage.mortgageportal.enums.UserRole;
import com.mortgage.mortgageportal.event.ApplicationEventPayload;
import com.mortgage.mortgageportal.event.EventMetadata;
import com.mortgage.mortgageportal.mapper.ApplicationMapper;
import com.mortgage.mortgageportal.service.ApplicationService;
import com.mortgage.mortgageportal.service.EventPublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {
    
    private final EventPublisherService eventPublisherService;
    
    // For demo purposes, using in-memory storage
    // In production, this would be a JPA repository
    private final List<Application> applications = new ArrayList<>();
    
    private static final String APPLICATION_TOPIC = "loan.applications";
    
    @Override
    public Application createApplication(ApplicationRequestDTO request, User user) {
        Application app = ApplicationMapper.toEntity(request, user);
        app.setId(UUID.randomUUID());
        app.setStatus(ApplicationStatus.PENDING);
        app.setCreatedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDateTime.now());
        applications.add(app);
        
        // Publish CREATE event
        publishApplicationEvent(app, "CREATE");
        
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
    
    @Override
    public Application updateApplication(UUID id, ApplicationRequestDTO request, User user) {
        Application existingApp = getApplicationById(id, user);
        
        // Update fields
        existingApp.setAmount(request.getAmount());
        existingApp.setLoanTermMonths(request.getLoanTermMonths());
        existingApp.setUpdatedAt(LocalDateTime.now());
        
        // Publish UPDATE event
        publishApplicationEvent(existingApp, "UPDATE");
        
        return existingApp;
    }
    
    @Override
    public void deleteApplication(UUID id, User user) {
        Application app = getApplicationById(id, user);
        applications.remove(app);
        
        // Publish DELETE event
        publishApplicationEvent(app, "DELETE");
    }
    
    private void publishApplicationEvent(Application application, String operation) {
        try {
            ApplicationEventPayload payload = ApplicationEventPayload.builder()
                    .application(ApplicationMapper.toResponseDTO(application))
                    .metadata(EventMetadata.builder()
                            .eventType("APPLICATION_" + operation)
                            .build())
                    .operation(operation)
                    .build();
            
            String key = application.getId().toString();
            eventPublisherService.publishApplicationEvent(APPLICATION_TOPIC, key, payload);
            
            log.info("Published {} event for application: {}", operation, key);
            
        } catch (Exception e) {
            log.error("Failed to publish {} event for application: {}", operation, application.getId(), e);
            // Don't throw exception to avoid breaking the main business logic
        }
    }
} 