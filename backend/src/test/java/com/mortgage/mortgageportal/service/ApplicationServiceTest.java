package com.mortgage.mortgageportal.service;

import com.mortgage.mortgageportal.dto.ApplicationRequestDTO;
import com.mortgage.mortgageportal.entities.Application;
import com.mortgage.mortgageportal.entities.User;
import com.mortgage.mortgageportal.enums.ApplicationStatus;
import com.mortgage.mortgageportal.enums.UserRole;
import com.mortgage.mortgageportal.service.impl.ApplicationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private EventPublisherService eventPublisherService;

    private ApplicationServiceImpl applicationService;

    private User testUser;
    private ApplicationRequestDTO testRequest;

    @BeforeEach
    void setUp() {
        applicationService = new ApplicationServiceImpl(eventPublisherService);
        
        testUser = User.builder()
                .id(UUID.randomUUID())
                .fullName("John Doe")
                .email("john@example.com")
                .nationalId("123456789")
                .role(UserRole.APPLICANT)
                .build();

        testRequest = ApplicationRequestDTO.builder()
                .amount(new BigDecimal("100000"))
                .loanTermMonths(240)
                .build();
    }

    @Test
    void createApplication_ShouldCreateApplicationSuccessfully() {
        // When
        Application result = applicationService.createApplication(testRequest, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getAmount()).isEqualTo(testRequest.getAmount());
        assertThat(result.getLoanTermMonths()).isEqualTo(testRequest.getLoanTermMonths());
        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.PENDING);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();

        verify(eventPublisherService).publishApplicationEvent(
                eq("loan.applications"),
                eq(result.getId().toString()),
                any()
        );
    }

    @Test
    void getApplicationById_ShouldReturnApplication_WhenExists() {
        // Given
        Application createdApp = applicationService.createApplication(testRequest, testUser);

        // When
        Application result = applicationService.getApplicationById(createdApp.getId(), testUser);

        // Then
        assertThat(result).isEqualTo(createdApp);
    }

    @Test
    void getApplicationById_ShouldThrowException_WhenApplicationNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        assertThatThrownBy(() -> applicationService.getApplicationById(nonExistentId, testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Application not found");
    }

    @Test
    void getApplicationById_ShouldThrowException_WhenApplicantAccessesOtherUserApplication() {
        // Given
        User otherUser = User.builder()
                .id(UUID.randomUUID())
                .fullName("Jane Doe")
                .email("jane@example.com")
                .nationalId("987654321")
                .role(UserRole.APPLICANT)
                .build();

        Application createdApp = applicationService.createApplication(testRequest, testUser);

        // When & Then
        assertThatThrownBy(() -> applicationService.getApplicationById(createdApp.getId(), otherUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Access denied: You can only view your own applications");
    }

    @Test
    void getApplicationById_ShouldAllowOfficerToAccessAnyApplication() {
        // Given
        User officer = User.builder()
                .id(UUID.randomUUID())
                .fullName("Officer Smith")
                .email("officer@example.com")
                .nationalId("111222333")
                .role(UserRole.OFFICER)
                .build();

        Application createdApp = applicationService.createApplication(testRequest, testUser);

        // When
        Application result = applicationService.getApplicationById(createdApp.getId(), officer);

        // Then
        assertThat(result).isEqualTo(createdApp);
    }

    @Test
    void listApplications_ShouldReturnAllApplications_WhenNoFilters() {
        // Given
        Application app1 = applicationService.createApplication(testRequest, testUser);
        
        ApplicationRequestDTO request2 = ApplicationRequestDTO.builder()
                .amount(new BigDecimal("200000"))
                .loanTermMonths(360)
                .build();
        Application app2 = applicationService.createApplication(request2, testUser);

        // When
        List<Application> results = applicationService.listApplications(null, null, null, null);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).contains(app1, app2);
    }

    @Test
    void listApplications_ShouldFilterByStatus() {
        // Given
        applicationService.createApplication(testRequest, testUser);

        // When
        List<Application> results = applicationService.listApplications(ApplicationStatus.PENDING, null, null, null);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    void listApplications_ShouldFilterByDateRange() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Application app = applicationService.createApplication(testRequest, testUser);

        // When
        List<Application> results = applicationService.listApplications(
                null, 
                now.minusMinutes(1), 
                now.plusMinutes(1), 
                null
        );

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo(app);
    }

    @Test
    void listApplications_ShouldFilterByNationalId() {
        // Given
        applicationService.createApplication(testRequest, testUser);

        // When
        List<Application> results = applicationService.listApplications(null, null, null, testUser.getNationalId());

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUser().getNationalId()).isEqualTo(testUser.getNationalId());
    }

    @Test
    void updateApplication_ShouldUpdateApplicationSuccessfully() {
        // Given
        Application originalApp = applicationService.createApplication(testRequest, testUser);
        
        ApplicationRequestDTO updateRequest = ApplicationRequestDTO.builder()
                .amount(new BigDecimal("150000"))
                .loanTermMonths(300)
                .build();

        // When
        Application result = applicationService.updateApplication(originalApp.getId(), updateRequest, testUser);

        // Then
        assertThat(result.getAmount()).isEqualTo(updateRequest.getAmount());
        assertThat(result.getLoanTermMonths()).isEqualTo(updateRequest.getLoanTermMonths());
        assertThat(result.getUpdatedAt()).isAfterOrEqualTo(originalApp.getUpdatedAt());

        verify(eventPublisherService, times(2)).publishApplicationEvent(
                eq("loan.applications"),
                eq(originalApp.getId().toString()),
                any()
        );
    }

    @Test
    void deleteApplication_ShouldDeleteApplicationSuccessfully() {
        // Given
        Application app = applicationService.createApplication(testRequest, testUser);

        // When
        applicationService.deleteApplication(app.getId(), testUser);

        // Then
        assertThatThrownBy(() -> applicationService.getApplicationById(app.getId(), testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Application not found");

        verify(eventPublisherService, times(2)).publishApplicationEvent(
                eq("loan.applications"),
                eq(app.getId().toString()),
                any()
        );
    }
}
