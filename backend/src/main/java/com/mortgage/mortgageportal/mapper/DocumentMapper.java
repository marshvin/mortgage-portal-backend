package com.mortgage.mortgageportal.mapper;

import com.mortgage.mortgageportal.dto.CreateDocumentRequest;
import com.mortgage.mortgageportal.dto.DocumentResponse;
import com.mortgage.mortgageportal.entities.Application;
import com.mortgage.mortgageportal.entities.Document;

public class DocumentMapper {
    
    public static Document toEntity(CreateDocumentRequest request, Application application) {
        return Document.builder()
                .application(application)
                .fileName(request.getFileName())
                .fileType(request.getFileType())
                .fileSize(request.getFileSize())
                .presignedUrl(request.getPresignedUrl())
                .build();
    }
    
    public static DocumentResponse toResponseDTO(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .applicationId(document.getApplication() != null ? document.getApplication().getId() : null)
                .fileName(document.getFileName())
                .fileType(document.getFileType())
                .fileSize(document.getFileSize())
                .presignedUrl(document.getPresignedUrl())
                .uploadedAt(document.getUploadedAt())
                .build();
    }
}
