package com.mortgage.mortgageportal.service;

import com.mortgage.mortgageportal.dto.ApplicationRequestDTO;
import com.mortgage.mortgageportal.entities.Application;
import com.mortgage.mortgageportal.entities.User;
import com.mortgage.mortgageportal.enums.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ApplicationService {
    Application createApplication(ApplicationRequestDTO request, User user);
    Application getApplicationById(UUID id, User requester);
    List<Application> listApplications(ApplicationStatus status, LocalDateTime createdFrom, LocalDateTime createdTo, String nationalId);
}