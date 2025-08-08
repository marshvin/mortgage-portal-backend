package com.mortgage.mortgageportal.service;

import com.mortgage.mortgageportal.dto.CreateDocumentRequest;
import com.mortgage.mortgageportal.entities.Document;
import com.mortgage.mortgageportal.entities.User;

import java.util.List;
import java.util.UUID;

public interface DocumentService {
    
    Document createDocument(CreateDocumentRequest request, User user);
    
    List<Document> getDocumentsByApplicationId(UUID applicationId, User user);
    
    Document getDocumentById(UUID documentId, User user);
}
