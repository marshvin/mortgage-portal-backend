package com.mortgage.mortgageportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDocumentRequest {
    
    @NotNull(message = "Application ID is required")
    private UUID applicationId;
    
    @NotBlank(message = "File name is required")
    @Size(max = 255, message = "File name must not exceed 255 characters")
    private String fileName;
    
    @NotBlank(message = "File type is required")
    @Size(max = 50, message = "File type must not exceed 50 characters")
    private String fileType;
    
    @NotNull(message = "File size is required")
    @Positive(message = "File size must be positive")
    private Long fileSize;
    
    @NotBlank(message = "Presigned URL is required")
    @Size(max = 1000, message = "Presigned URL must not exceed 1000 characters")
    private String presignedUrl;
}
