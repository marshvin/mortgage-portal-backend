package com.mortgage.mortgageportal.service;

import com.mortgage.mortgageportal.dto.CreateDocumentRequest;
import com.mortgage.mortgageportal.entities.Application;
import com.mortgage.mortgageportal.entities.Document;
import com.mortgage.mortgageportal.entities.User;
import com.mortgage.mortgageportal.enums.ApplicationStatus;
import com.mortgage.mortgageportal.enums.UserRole;
import com.mortgage.mortgageportal.service.impl.DocumentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private ApplicationService applicationService;

    private DocumentServiceImpl documentService;

    private User testUser;
    private Application testApplication;
    private CreateDocumentRequest testRequest;

    @BeforeEach
    void setUp() {
        documentService = new DocumentServiceImpl(applicationService);
        
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
    }

    @Test
    void createDocument_ShouldCreateDocumentSuccessfully() {
        // Given
        when(applicationService.getApplicationById(testApplication.getId(), testUser))
                .thenReturn(testApplication);

        // When
        Document result = documentService.createDocument(testRequest, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getApplication()).isEqualTo(testApplication);
        assertThat(result.getFileName()).isEqualTo(testRequest.getFileName());
        assertThat(result.getFileType()).isEqualTo(testRequest.getFileType());
        assertThat(result.getFileSize()).isEqualTo(testRequest.getFileSize());
        assertThat(result.getPresignedUrl()).isEqualTo(testRequest.getPresignedUrl());
        assertThat(result.getUploadedAt()).isNotNull();

        verify(applicationService).getApplicationById(testApplication.getId(), testUser);
    }

    @Test
    void createDocument_ShouldThrowException_WhenApplicationNotFound() {
        // Given
        when(applicationService.getApplicationById(testApplication.getId(), testUser))
                .thenThrow(new RuntimeException("Application not found"));

        // When & Then
        assertThatThrownBy(() -> documentService.createDocument(testRequest, testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Application not found");

        verify(applicationService).getApplicationById(testApplication.getId(), testUser);
    }

    @Test
    void getDocumentsByApplicationId_ShouldReturnDocuments_WhenApplicationExists() {
        // Given
        when(applicationService.getApplicationById(testApplication.getId(), testUser))
                .thenReturn(testApplication);

        Document doc1 = documentService.createDocument(testRequest, testUser);
        
        CreateDocumentRequest request2 = CreateDocumentRequest.builder()
                .applicationId(testApplication.getId())
                .fileName("pay_stub.pdf")
                .fileType("pdf")
                .fileSize(1048576L)
                .presignedUrl("https://s3.amazonaws.com/bucket/pay_stub.pdf?signature=def456")
                .build();
        Document doc2 = documentService.createDocument(request2, testUser);

        // When
        List<Document> results = documentService.getDocumentsByApplicationId(testApplication.getId(), testUser);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).contains(doc1, doc2);
        assertThat(results).allMatch(doc -> doc.getApplication().getId().equals(testApplication.getId()));
    }

    @Test
    void getDocumentsByApplicationId_ShouldThrowException_WhenApplicationNotFound() {
        // Given
        when(applicationService.getApplicationById(testApplication.getId(), testUser))
                .thenThrow(new RuntimeException("Application not found"));

        // When & Then
        assertThatThrownBy(() -> documentService.getDocumentsByApplicationId(testApplication.getId(), testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Application not found");

        verify(applicationService).getApplicationById(testApplication.getId(), testUser);
    }

    @Test
    void getDocumentById_ShouldReturnDocument_WhenExists() {
        // Given
        when(applicationService.getApplicationById(testApplication.getId(), testUser))
                .thenReturn(testApplication);

        Document createdDoc = documentService.createDocument(testRequest, testUser);

        // When
        Document result = documentService.getDocumentById(createdDoc.getId(), testUser);

        // Then
        assertThat(result).isEqualTo(createdDoc);
    }

    @Test
    void getDocumentById_ShouldThrowException_WhenDocumentNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        assertThatThrownBy(() -> documentService.getDocumentById(nonExistentId, testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Document not found");
    }

    @Test
    void getDocumentById_ShouldThrowException_WhenApplicantAccessesOtherUserDocument() {
        // Given
        User otherUser = User.builder()
                .id(UUID.randomUUID())
                .fullName("Jane Doe")
                .email("jane@example.com")
                .nationalId("987654321")
                .role(UserRole.APPLICANT)
                .build();

        when(applicationService.getApplicationById(testApplication.getId(), testUser))
                .thenReturn(testApplication);

        Document createdDoc = documentService.createDocument(testRequest, testUser);

        // When & Then
        assertThatThrownBy(() -> documentService.getDocumentById(createdDoc.getId(), otherUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Access denied: You can only view documents for your own applications");
    }

    @Test
    void getDocumentById_ShouldAllowOfficerToAccessAnyDocument() {
        // Given
        User officer = User.builder()
                .id(UUID.randomUUID())
                .fullName("Officer Smith")
                .email("officer@example.com")
                .nationalId("111222333")
                .role(UserRole.OFFICER)
                .build();

        when(applicationService.getApplicationById(testApplication.getId(), testUser))
                .thenReturn(testApplication);

        Document createdDoc = documentService.createDocument(testRequest, testUser);

        // When
        Document result = documentService.getDocumentById(createdDoc.getId(), officer);

        // Then
        assertThat(result).isEqualTo(createdDoc);
    }

    @Test
    void getDocumentsByApplicationId_ShouldReturnEmptyList_WhenNoDocumentsExist() {
        // Given
        when(applicationService.getApplicationById(testApplication.getId(), testUser))
                .thenReturn(testApplication);

        // When
        List<Document> results = documentService.getDocumentsByApplicationId(testApplication.getId(), testUser);

        // Then
        assertThat(results).isEmpty();
    }
}
