package com.mortgage.mortgageportal.controller;

import com.mortgage.mortgageportal.dto.*;
import com.mortgage.mortgageportal.entities.Application;
import com.mortgage.mortgageportal.entities.Decision;
import com.mortgage.mortgageportal.entities.User;
import com.mortgage.mortgageportal.enums.ApplicationStatus;
import com.mortgage.mortgageportal.enums.UserRole;
import com.mortgage.mortgageportal.mapper.ApplicationMapper;
import com.mortgage.mortgageportal.mapper.DecisionMapper;
import com.mortgage.mortgageportal.service.ApplicationService;
import com.mortgage.mortgageportal.service.DecisionService;
import com.mortgage.mortgageportal.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService applicationService;
    private final DecisionService decisionService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('APPLICANT')")
    public ResponseEntity<ApplicationResponseDTO> createApplication(@Valid @RequestBody ApplicationRequestDTO request, Authentication auth) {
        User user = userService.getCurrentUser(auth);
        Application app = applicationService.createApplication(request, user);
        return ResponseEntity.ok(ApplicationMapper.toResponseDTO(app));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('APPLICANT','OFFICER')")
    public ResponseEntity<ApplicationResponseDTO> getApplication(@PathVariable UUID id, Authentication auth) {
        User user = userService.getCurrentUser(auth);
        Application app = applicationService.getApplicationById(id, user);
        return ResponseEntity.ok(ApplicationMapper.toResponseDTO(app));
    }

    @GetMapping
    @PreAuthorize("hasRole('OFFICER')")
    public ResponseEntity<List<ApplicationResponseDTO>> listApplications(
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @RequestParam(required = false) String nationalId) {
        List<Application> apps = applicationService.listApplications(status, createdFrom, createdTo, nationalId);
        List<ApplicationResponseDTO> dtos = apps.stream().map(ApplicationMapper::toResponseDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PatchMapping("/{id}/decision")
    @PreAuthorize("hasRole('OFFICER')")
    public ResponseEntity<DecisionResponseDTO> decideApplication(@PathVariable UUID id, @Valid @RequestBody DecisionRequestDTO request, Authentication auth) {
        User officer = userService.getCurrentUser(auth);
        Decision decision = decisionService.decideApplication(id, request, officer);
        return ResponseEntity.ok(DecisionMapper.toResponseDTO(decision));
    }
}