package com.mortgage.mortgageportal.mapper;

import com.mortgage.mortgageportal.dto.ApplicationRequestDTO;
import com.mortgage.mortgageportal.dto.ApplicationResponseDTO;
import com.mortgage.mortgageportal.entities.Application;
import com.mortgage.mortgageportal.entities.User;
import com.mortgage.mortgageportal.enums.ApplicationStatus;
import com.mortgage.mortgageportal.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationMapperTest {

    private User testUser;
    private ApplicationRequestDTO testRequest;
    private Application testApplication;

    @BeforeEach
    void setUp() {
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

        testApplication = Application.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .amount(new BigDecimal("100000"))
                .loanTermMonths(240)
                .status(ApplicationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void toEntity_ShouldMapRequestToEntity() {
        // When
        Application result = ApplicationMapper.toEntity(testRequest, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getAmount()).isEqualTo(testRequest.getAmount());
        assertThat(result.getLoanTermMonths()).isEqualTo(testRequest.getLoanTermMonths());
        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.PENDING);
        // Note: createdAt and updatedAt are set by the service layer, not the mapper
    }

    @Test
    void toResponseDTO_ShouldMapEntityToResponse() {
        // When
        ApplicationResponseDTO result = ApplicationMapper.toResponseDTO(testApplication);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testApplication.getId());
        assertThat(result.getUserId()).isEqualTo(testApplication.getUser().getId());
        assertThat(result.getAmount()).isEqualTo(testApplication.getAmount());
        assertThat(result.getLoanTermMonths()).isEqualTo(testApplication.getLoanTermMonths());
        assertThat(result.getStatus()).isEqualTo(testApplication.getStatus());
        assertThat(result.getCreatedAt()).isEqualTo(testApplication.getCreatedAt());
        assertThat(result.getUpdatedAt()).isEqualTo(testApplication.getUpdatedAt());
    }

    @Test
    void toEntity_ShouldHandleNullValues() {
        // Given
        ApplicationRequestDTO nullRequest = ApplicationRequestDTO.builder()
                .amount(null)
                .loanTermMonths(null)
                .build();

        // When
        Application result = ApplicationMapper.toEntity(nullRequest, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getAmount()).isNull();
        assertThat(result.getLoanTermMonths()).isNull();
    }

    @Test
    void toResponseDTO_ShouldHandleNullValues() {
        // Given
        Application nullApplication = Application.builder()
                .id(UUID.randomUUID())
                .user(null)
                .amount(null)
                .loanTermMonths(null)
                .status(null)
                .createdAt(null)
                .updatedAt(null)
                .build();

        // When
        ApplicationResponseDTO result = ApplicationMapper.toResponseDTO(nullApplication);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(nullApplication.getId());
        assertThat(result.getUserId()).isNull();
        assertThat(result.getAmount()).isNull();
        assertThat(result.getLoanTermMonths()).isNull();
        assertThat(result.getStatus()).isNull();
        assertThat(result.getCreatedAt()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
    }
}
