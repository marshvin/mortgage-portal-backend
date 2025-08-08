package com.mortgage.mortgageportal.mapper;

import com.mortgage.mortgageportal.dto.CreateDocumentRequest;
import com.mortgage.mortgageportal.dto.DocumentResponse;
import com.mortgage.mortgageportal.entities.Application;
import com.mortgage.mortgageportal.entities.Document;
import com.mortgage.mortgageportal.entities.User;
import com.mortgage.mortgageportal.enums.ApplicationStatus;
import com.mortgage.mortgageportal.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentMapperTest {

    private User testUser;
    private Application testApplication;
    private CreateDocumentRequest testRequest;
    private Document testDocument;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .fullName("John Doe")
                .email("john@example.com")
                .nationalId("123456789")
                .role(UserRole.APPLICANT)
                .build();

        testApplication = Application.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .amount(new BigDecimal("100000"))
                .loanTermMonths(240)
                .status(ApplicationStatus.PENDING)
                .build();

        testRequest = CreateDocumentRequest.builder()
                .applicationId(testApplication.getId())
                .fileName("bank_statement.pdf")
                .fileType("pdf")
                .fileSize(2048576L)
                .presignedUrl("https://s3.amazonaws.com/bucket/bank_statement.pdf?signature=abc123")
                .build();

        testDocument = Document.builder()
                .id(UUID.randomUUID())
                .application(testApplication)
                .fileName("bank_statement.pdf")
                .fileType("pdf")
                .fileSize(2048576L)
                .presignedUrl("https://s3.amazonaws.com/bucket/bank_statement.pdf?signature=abc123")
                .uploadedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void toEntity_ShouldMapRequestToEntity() {
        // When
        Document result = DocumentMapper.toEntity(testRequest, testApplication);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getApplication()).isEqualTo(testApplication);
        assertThat(result.getFileName()).isEqualTo(testRequest.getFileName());
        assertThat(result.getFileType()).isEqualTo(testRequest.getFileType());
        assertThat(result.getFileSize()).isEqualTo(testRequest.getFileSize());
        assertThat(result.getPresignedUrl()).isEqualTo(testRequest.getPresignedUrl());
    }

    @Test
    void toResponseDTO_ShouldMapEntityToResponse() {
        // When
        DocumentResponse result = DocumentMapper.toResponseDTO(testDocument);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testDocument.getId());
        assertThat(result.getApplicationId()).isEqualTo(testDocument.getApplication().getId());
        assertThat(result.getFileName()).isEqualTo(testDocument.getFileName());
        assertThat(result.getFileType()).isEqualTo(testDocument.getFileType());
        assertThat(result.getFileSize()).isEqualTo(testDocument.getFileSize());
        assertThat(result.getPresignedUrl()).isEqualTo(testDocument.getPresignedUrl());
        assertThat(result.getUploadedAt()).isEqualTo(testDocument.getUploadedAt());
    }

    @Test
    void toEntity_ShouldHandleNullValues() {
        // Given
        CreateDocumentRequest nullRequest = CreateDocumentRequest.builder()
                .applicationId(null)
                .fileName(null)
                .fileType(null)
                .fileSize(null)
                .presignedUrl(null)
                .build();

        // When
        Document result = DocumentMapper.toEntity(nullRequest, testApplication);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getApplication()).isEqualTo(testApplication);
        assertThat(result.getFileName()).isNull();
        assertThat(result.getFileType()).isNull();
        assertThat(result.getFileSize()).isNull();
        assertThat(result.getPresignedUrl()).isNull();
    }

    @Test
    void toResponseDTO_ShouldHandleNullValues() {
        // Given
        Document nullDocument = Document.builder()
                .id(UUID.randomUUID())
                .application(null)
                .fileName(null)
                .fileType(null)
                .fileSize(null)
                .presignedUrl(null)
                .uploadedAt(null)
                .build();

        // When
        DocumentResponse result = DocumentMapper.toResponseDTO(nullDocument);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(nullDocument.getId());
        assertThat(result.getApplicationId()).isNull();
        assertThat(result.getFileName()).isNull();
        assertThat(result.getFileType()).isNull();
        assertThat(result.getFileSize()).isNull();
        assertThat(result.getPresignedUrl()).isNull();
        assertThat(result.getUploadedAt()).isNull();
    }
}
