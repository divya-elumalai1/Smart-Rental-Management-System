package com.smartrental.security;

import com.smartrental.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Utility class for JWT token generation, validation, and parsing.
 * Uses HS256 algorithm with configurable secret key.
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.token-prefix}")
    private String tokenPrefix;

    private SecretKey key;

    @PostConstruct
    public void init() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                "JWT secret must be at least 32 characters. Set JWT_SECRET environment variable.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // ===========================================
    // Token Generation
    // ===========================================

    /**
     * Generate access token for a user.
     * @param user the authenticated user
     * @return JWT access token
     */
    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim("userId", user.getId().toString())
                .claim("role", user.getRole().name())
                .claim("firstName", user.getFirstName())
                .claim("lastName", user.getLastName())
                .claim("type", "access")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generate refresh token for a user.
     * @param user the authenticated user
     * @return JWT refresh token
     */
    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim("userId", user.getId().toString())
                .claim("type", "refresh")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generate email verification token.
     * @param user the user
     * @param expiryMinutes token expiry in minutes
     * @return JWT verification token
     */
    public String generateEmailVerificationToken(User user, int expiryMinutes) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryMinutes * 60 * 1000);

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim("userId", user.getId().toString())
                .claim("type", "email_verification")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generate password reset token.
     * @param user the user
     * @param expiryMinutes token expiry in minutes
     * @return JWT reset token
     */
    public String generatePasswordResetToken(User user, int expiryMinutes) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryMinutes * 60 * 1000);

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim("userId", user.getId().toString())
                .claim("type", "password_reset")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ===========================================
    // Token Validation & Parsing
    // ===========================================

    /**
     * Validate JWT token.
     * @param token the JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException e) {
            log.debug("Invalid JWT token format: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.debug("JWT token expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.debug("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.debug("JWT token compact handler exception: {}", e.getMessage());
        } catch (JwtException e) {
            log.debug("JWT token validation failed: {}", e.getMessage());
        }
        return false;
    }

    // ===========================================
    // Claims Extraction
    // ===========================================

    /**
     * Parse claims from token.
     * @param token the JWT token
     * @return Claims object
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Get email (subject) from token.
     * @param token the JWT token
     * @return email string
     */
    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Get token type from token.
     * @param token the JWT token
     * @return token type string
     */
    public String getTokenType(String token) {
        return parseClaims(token).get("type", String.class);
    }

    // ===========================================
    // Request Helpers
    // ===========================================

    /**
     * Extract token from Authorization header.
     * @param request HTTP request
     * @return token string or null
     */
    public String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(header);
        if (bearerToken != null && bearerToken.startsWith(tokenPrefix)) {
            return bearerToken.substring(tokenPrefix.length());
        }
        return null;
    }

    /**
     * Get access token expiration in milliseconds.
     * @return expiration ms
     */
    public long getExpiration() {
        return expiration;
    }

    /**
     * Get refresh token expiration in milliseconds.
     * @return refresh expiration ms
     */
    public long getRefreshExpiration() {
        return refreshExpiration;
    }
}