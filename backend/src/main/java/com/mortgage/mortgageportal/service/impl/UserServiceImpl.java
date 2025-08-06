package com.mortgage.mortgageportal.service.impl;

import com.mortgage.mortgageportal.entities.User;
import com.mortgage.mortgageportal.enums.UserRole;
import com.mortgage.mortgageportal.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    
    @Override
    public User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        
        String username = authentication.getName();
        
        // For demo purposes, create a user based on JWT claims
        // In production, this would fetch from database
        User user = new User();
        user.setId(UUID.randomUUID()); // In real app, get from DB
        user.setEmail(username);
        user.setFullName("Demo User");
        user.setNationalId("123456789");
        
        // Determine role from authorities
        UserRole role = UserRole.APPLICANT; // default
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority.getAuthority().equals("ROLE_OFFICER")) {
                role = UserRole.OFFICER;
                break;
            }
        }
        user.setRole(role);
        
        return user;
    }
} 