package com.mortgage.mortgageportal.service.impl;

import com.mortgage.mortgageportal.dto.CreateDocumentRequest;
import com.mortgage.mortgageportal.entities.Application;
import com.mortgage.mortgageportal.entities.Document;
import com.mortgage.mortgageportal.entities.User;
import com.mortgage.mortgageportal.enums.UserRole;
import com.mortgage.mortgageportal.mapper.DocumentMapper;
import com.mortgage.mortgageportal.service.ApplicationService;
import com.mortgage.mortgageportal.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {
    
    private final ApplicationService applicationService;
    
    // For demo purposes, using in-memory storage
    // In production, this would be a JPA repository
    private final List<Document> documents = new ArrayList<>();
    
    @Override
    public Document createDocument(CreateDocumentRequest request, User user) {
        // Get the application and verify access
        Application application = applicationService.getApplicationById(request.getApplicationId(), user);
        
        // Create the document
        Document document = DocumentMapper.toEntity(request, application);
        document.setId(UUID.randomUUID());
        document.setUploadedAt(java.time.LocalDateTime.now());
        documents.add(document);
        
        log.info("Created document: {} for application: {}", document.getId(), application.getId());
        
        return document;
    }
    
    @Override
    public List<Document> getDocumentsByApplicationId(UUID applicationId, User user) {
        // Verify access to the application
        applicationService.getApplicationById(applicationId, user);
        
        return documents.stream()
                .filter(doc -> doc.getApplication().getId().equals(applicationId))
                .toList();
    }
    
    @Override
    public Document getDocumentById(UUID documentId, User user) {
        Document document = documents.stream()
                .filter(doc -> doc.getId().equals(documentId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        // Check access control
        if (user.getRole() == UserRole.APPLICANT && 
            !document.getApplication().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: You can only view documents for your own applications");
        }
        
        return document;
    }
}
