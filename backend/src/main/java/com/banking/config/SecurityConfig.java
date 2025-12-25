package com.banking.config;

import com.banking.security.JwtAuthenticationEntryPoint;
import com.banking.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security configuration for the banking application.
 * 
 * Security Features:
 * - JWT-based stateless authentication
 * - BCrypt password encoding
 * - Role-based access control (RBAC)
 * - CORS configuration for frontend
 * - Public endpoints for authentication and health checks
 * - Admin-only endpoints protection
 * 
 * @author Banking Platform Team
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    /**
     * Configures BCrypt password encoder for secure password hashing.
     * 
     * BCrypt automatically handles salt generation and hashing.
     * 
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Configures authentication provider for user authentication.
     * 
     * Uses DAO (Data Access Object) authentication with:
     * - UserDetailsService for loading user data
     * - BCrypt password encoder for password verification
     * 
     * @return DaoAuthenticationProvider instance
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
    
    /**
     * Configures authentication manager for handling authentication requests.
     * 
     * @param config AuthenticationConfiguration from Spring
     * @return AuthenticationManager instance
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    /**
     * Configures the main security filter chain.
     * 
     * Security Configuration:
     * - CSRF disabled (using JWT tokens instead)
     * - CORS enabled for frontend communication
     * - Stateless session management (JWT-based)
     * - Public endpoints: /api/auth/**, /swagger-ui/**, /actuator/health
     * - Admin endpoints: /api/admin/** (requires ADMIN role)
     * - All other endpoints require authentication
     * - JWT authentication filter applied before username/password filter
     * 
     * @param http HttpSecurity builder
     * @return Configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (using JWT tokens for stateless authentication)
            .csrf(csrf -> csrf.disable())
            
            // Enable CORS for frontend communication
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configure exception handling for unauthorized requests
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint))
            
            // Stateless session management (no server-side sessions)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure endpoint authorization
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (no authentication required)
                .requestMatchers("/api/auth/**", "/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**").permitAll()
                // Health check endpoints (for Docker health checks)
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // Admin endpoints (require ADMIN role)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // All other endpoints require authentication
                .anyRequest().authenticated())
            
            // Set authentication provider
            .authenticationProvider(authenticationProvider())
            
            // Add JWT filter before username/password authentication filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    /**
     * Configures CORS (Cross-Origin Resource Sharing) for frontend communication.
     * 
     * Allows:
     * - Frontend origin: http://localhost:4200
     * - HTTP methods: GET, POST, PUT, DELETE, OPTIONS
     * - All headers
     * - Credentials (cookies, authorization headers)
     * 
     * @return CorsConfigurationSource with CORS settings
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

