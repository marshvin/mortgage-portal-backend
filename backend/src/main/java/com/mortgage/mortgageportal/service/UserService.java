package com.mortgage.mortgageportal.service;

import com.mortgage.mortgageportal.entities.User;
import org.springframework.security.core.Authentication;

public interface UserService {
    User getCurrentUser(Authentication authentication);
}