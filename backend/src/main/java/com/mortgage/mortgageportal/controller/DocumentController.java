package com.mortgage.mortgageportal.controller;

import com.mortgage.mortgageportal.dto.CreateDocumentRequest;
import com.mortgage.mortgageportal.dto.DocumentResponse;
import com.mortgage.mortgageportal.entities.Document;
import com.mortgage.mortgageportal.entities.User;
import com.mortgage.mortgageportal.mapper.DocumentMapper;
import com.mortgage.mortgageportal.service.DocumentService;
import com.mortgage.mortgageportal.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {
    
    private final DocumentService documentService;
    private final UserService userService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('APPLICANT','OFFICER')")
    public ResponseEntity<DocumentResponse> createDocument(@Valid @RequestBody CreateDocumentRequest request, Authentication auth) {
        User user = userService.getCurrentUser(auth);
        Document document = documentService.createDocument(request, user);
        return ResponseEntity.ok(DocumentMapper.toResponseDTO(document));
    }
    
    @GetMapping("/application/{applicationId}")
    @PreAuthorize("hasAnyRole('APPLICANT','OFFICER')")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByApplication(@PathVariable UUID applicationId, Authentication auth) {
        User user = userService.getCurrentUser(auth);
        List<Document> documents = documentService.getDocumentsByApplicationId(applicationId, user);
        List<DocumentResponse> responses = documents.stream()
                .map(DocumentMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('APPLICANT','OFFICER')")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable UUID id, Authentication auth) {
        User user = userService.getCurrentUser(auth);
        Document document = documentService.getDocumentById(id, user);
        return ResponseEntity.ok(DocumentMapper.toResponseDTO(document));
    }
}
