package com.banking.config;

import com.banking.entity.User;
import com.banking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data initializer that runs on application startup.
 * 
 * Creates default admin user if it doesn't exist in the database.
 * This ensures the system always has at least one admin account for initial access.
 * 
 * The admin user is created with:
 * - Username: admin
 * - Password: admin123 (should be changed in production)
 * - Role: ADMIN
 * 
 * @author Banking Platform Team
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Executes on application startup.
     * 
     * Creates default admin user if it doesn't already exist.
     * This is a one-time operation - subsequent startups will skip creation.
     * 
     * @param args Command line arguments (unused)
     */
    @Override
    public void run(String... args) {
        // Create default admin user if it doesn't exist
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123")) // BCrypt hashed password
                .email("admin@banking.com")
                .firstName("Admin")
                .lastName("User")
                .role(User.Role.ADMIN) // Admin role for full system access
                .enabled(true) // Account is active
                .build();
            userRepository.save(admin);
            System.out.println("Default admin user created: username=admin, password=admin123");
        }
    }
}

