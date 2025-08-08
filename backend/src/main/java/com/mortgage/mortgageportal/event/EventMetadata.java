package com.mortgage.mortgageportal.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventMetadata {
    @Builder.Default
    private String traceId = UUID.randomUUID().toString();
    
    @Builder.Default
    private String version = "v1";
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Builder.Default
    private Instant timestamp = Instant.now();
    
    @Builder.Default
    private String source = "mortgage-portal";
    private String eventType;
} 