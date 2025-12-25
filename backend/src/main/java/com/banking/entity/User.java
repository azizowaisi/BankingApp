package com.banking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * User entity representing a system user (customer or admin).
 * 
 * Implements Spring Security's UserDetails interface for authentication.
 * 
 * Roles:
 * - CUSTOMER: Standard banking customer with account management capabilities
 * - ADMIN: System administrator with full access to all accounts and audit logs
 * 
 * @author Banking Platform Team
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    
    /** Unique identifier for the user (UUID) */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    /** Unique username for login */
    @Column(nullable = false, unique = true)
    private String username;
    
    /** BCrypt-hashed password */
    @Column(nullable = false)
    private String password;
    
    /** User email address */
    @Column(nullable = false)
    private String email;
    
    /** User's first name */
    @Column(nullable = false)
    private String firstName;
    
    /** User's last name */
    @Column(nullable = false)
    private String lastName;
    
    /** User role (CUSTOMER or ADMIN) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    /** Account enabled status (false = account locked) */
    @Column(nullable = false)
    private boolean enabled = true;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    public enum Role {
        CUSTOMER, ADMIN
    }
}

