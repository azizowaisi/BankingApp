package com.banking.service;

import com.banking.dto.LoginRequest;
import com.banking.dto.LoginResponse;
import com.banking.dto.RegisterRequest;
import com.banking.entity.AuditLog;
import com.banking.entity.User;
import com.banking.repository.UserRepository;
import com.banking.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for authentication operations.
 * Handles user registration, login, and JWT token generation.
 * All operations are transactional and include audit logging.
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditService auditService;
    
    /**
     * Registers a new user account in the system.
     * 
     * Business Logic:
     * 1. Validates username and email uniqueness
     * 2. Encrypts password using BCrypt
     * 3. Creates user with CUSTOMER role by default
     * 4. Logs account creation in audit trail
     * 
     * @param request Registration request with user details
     * @return Saved user entity
     * @throws IllegalArgumentException if username or email already exists
     */
    @Transactional
    public User register(RegisterRequest request) {
        // Check for duplicate username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Build and save new user with encrypted password
        User user = User.builder()
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword())) // BCrypt hashing
            .email(request.getEmail())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .role(User.Role.CUSTOMER) // Default role for new registrations
            .enabled(true) // Account is active by default
            .build();
        
        User savedUser = userRepository.save(user);
        
        // Audit log for account creation
        auditService.logAction(savedUser, AuditLog.AuditAction.ACCOUNT_CREATED, 
            "User registered: " + savedUser.getUsername(), null);
        
        return savedUser;
    }
    
    /**
     * Authenticates a user and generates a JWT token.
     * 
     * Business Logic:
     * 1. Validates credentials using Spring Security AuthenticationManager
     * 2. Generates JWT token with user details
     * 3. Logs login event with IP address for security auditing
     * 
     * @param request Login request with username and password
     * @param ipAddress Client IP address for audit logging
     * @return Login response containing JWT token and user details
     * @throws org.springframework.security.core.AuthenticationException if authentication fails
     */
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress) {
        // Authenticate user credentials
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        
        // Extract authenticated user
        User user = (User) authentication.getPrincipal();
        
        // Generate JWT token with user claims
        String token = jwtTokenProvider.generateToken(user);
        
        // Log login event with IP address for security monitoring
        auditService.logAction(user, AuditLog.AuditAction.LOGIN, 
            "User logged in", ipAddress);
        
        // Build and return login response
        return LoginResponse.builder()
            .token(token)
            .username(user.getUsername())
            .role(user.getRole().name())
            .build();
    }
}

