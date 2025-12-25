package com.banking.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Token Provider for authentication.
 * 
 * Handles JWT token generation, validation, and parsing using HMAC-SHA algorithm.
 * Tokens are signed with a secret key and include expiration time.
 * 
 * Security Features:
 * - HMAC-SHA signing for token integrity
 * - Configurable expiration time
 * - Token validation on each request
 * 
 * @author Banking Platform Team
 */
@Component
public class JwtTokenProvider {
    
    /** Secret key for signing JWT tokens (configured in application.yml) */
    @Value("${spring.security.jwt.secret}")
    private String secret;
    
    /** Token expiration time in milliseconds (default: 24 hours) */
    @Value("${spring.security.jwt.expiration}")
    private Long expiration;
    
    /**
     * Generates HMAC-SHA signing key from secret string.
     * 
     * @return SecretKey for JWT signing
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    /**
     * Generates a JWT token for the authenticated user.
     * 
     * @param userDetails User details from Spring Security
     * @return JWT token string
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }
    
    /**
     * Creates a JWT token with specified claims and subject.
     * 
     * Token includes:
     * - Subject (username)
     * - Issued at timestamp
     * - Expiration timestamp
     * - Custom claims (if provided)
     * - HMAC-SHA signature
     * 
     * @param claims Custom claims to include in token
     * @param subject Token subject (username)
     * @return Signed JWT token string
     */
    private String createToken(Map<String, Object> claims, String subject) {
        var builder = Jwts.builder()
            .subject(subject)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + expiration));
        
        // Add custom claims if any
        claims.forEach(builder::claim);
        
        // Sign token with HMAC-SHA key and compact to string
        return builder
            .signWith(getSigningKey())
            .compact();
    }
    
    /**
     * Extracts username from JWT token.
     * 
     * @param token JWT token string
     * @return Username (subject)
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }
    
    /**
     * Extracts expiration date from JWT token.
     * 
     * @param token JWT token string
     * @return Expiration date
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
    
    /**
     * Generic method to extract a claim from JWT token.
     * 
     * @param token JWT token string
     * @param claimsResolver Function to extract specific claim
     * @return Claim value
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Parses and validates JWT token, returning all claims.
     * 
     * Validates token signature using the signing key.
     * 
     * @param token JWT token string
     * @return Claims object with all token claims
     * @throws io.jsonwebtoken.JwtException if token is invalid or signature doesn't match
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
    
    /**
     * Validates JWT token.
     * 
     * Checks:
     * - Token is not expired
     * - Token signature is valid
     * 
     * @param token JWT token string
     * @return true if token is valid, false otherwise
     */
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            // Token is invalid (malformed, expired, or signature mismatch)
            return false;
        }
    }
    
    /**
     * Checks if JWT token has expired.
     * 
     * @param token JWT token string
     * @return true if token is expired, false otherwise
     */
    private Boolean isTokenExpired(String token) {
        final Date tokenExpiration = getExpirationDateFromToken(token);
        return tokenExpiration.before(new Date());
    }
}

