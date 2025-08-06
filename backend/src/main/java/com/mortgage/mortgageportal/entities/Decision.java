package com.mortgage.mortgageportal.entities;

import com.mortgage.mortgageportal.enums.DecisionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "decisions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Decision {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private Application application;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officer_id", nullable = false)
    private User officer;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DecisionStatus status;
    
    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;
    
    @CreationTimestamp
    @Column(name = "decided_at", nullable = false, updatable = false)
    private LocalDateTime decidedAt;
} 