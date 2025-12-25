package com.banking.controller;

import com.banking.dto.LoginRequest;
import com.banking.dto.LoginResponse;
import com.banking.dto.RegisterRequest;
import com.banking.entity.User;
import com.banking.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication endpoints.
 * Handles user registration and login operations.
 * 
 * @author Banking Platform Team
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * Registers a new user account.
     * Validates input, checks for duplicate username/email, and creates a new customer account.
     * 
     * @param request Registration request containing username, password, email, firstName, lastName
     * @return Created user entity with HTTP 201 status
     * @throws IllegalArgumentException if username or email already exists
     */
    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    /**
     * Authenticates a user and returns a JWT token.
     * Validates credentials, generates JWT token, and logs the login event.
     * 
     * @param request Login request containing username and password
     * @param httpRequest HTTP request object to extract client IP address for audit logging
     * @return Login response containing JWT token, username, and role
     * @throws org.springframework.security.core.AuthenticationException if credentials are invalid
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        LoginResponse response = authService.login(request, ipAddress);
        return ResponseEntity.ok(response);
    }
}

