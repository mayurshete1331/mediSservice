// src/main/java/com/example/demo/service/JwtService.java
package com.example.demo.service;

import com.example.demo.model.Role; // Import your Role enum
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
// NEW IMPORTS FOR ROBUST ERROR HANDLING
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails; // For validateToken method
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service // This annotation is crucial!
public class JwtService {

    // NEW: Add a logger for better error messages
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    // UNCOMMENTED: These should be populated from application.properties
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    // --- Token Generation ---

    public String generateToken(String userName, Long userId, Set<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        // If 'roles' is already Set<String>, you don't need to map to Enum::name
        claims.put("roles", roles.stream().collect(Collectors.toList())); // Just collect the strings directly
        return createToken(claims, userName);
    }

    public String generateToken(String userName, Set<Role> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles.stream().map(Enum::name).collect(Collectors.toList()));
        return createToken(claims, userName);
    }

    private String createToken(Map<String, Object> claims, String userName) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userName)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                // UNCOMMENTED: JWT expiration is crucial for security
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // --- Key Management ---
    private Key getSignKey() {
        // Now uses the secretKey loaded from application.properties
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // --- Token Validation & Extraction (Essential for JWT Filter) ---

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (Exception e) {
            // Log if expiration cannot be extracted, but don't re-throw
            logger.warn("Could not extract expiration from token: {}", e.getMessage());
            return null; // Return null to allow safe handling in isTokenExpired
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        // Only apply resolver if claims are not null
        return claims != null ? claimsResolver.apply(claims) : null;
    }

    private Claims extractAllClaims(String token) {
        try {
            // CORRECTED: Use Jwts.parserBuilder() for modern JJWT versions
            return Jwts
                    .parser() // This method exists.
                    .setSigningKey(getSignKey())
                    .build() // This method exists, called after setSigningKey().
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("An unexpected error occurred during JWT parsing: {}", e.getMessage(), e);
        }
        return null; // Return null if any exception occurs during parsing
    }

    private Boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        // NEW: Safely check for null expiration date to prevent NPE
        return expiration != null && expiration.before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // NEW: Also check if username could be null if extraction failed
        return (username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public Set<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null) { // Handle case where claims could not be extracted
            return Set.of();
        }
        List<?> rolesList = (List<?>) claims.get("roles");
        if (rolesList != null) {
            return rolesList.stream()
                    .map(Object::toString)
                    .collect(Collectors.toSet());
        }
        return Set.of();
    }

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null) { // Handle case where claims could not be extracted
            return null;
        }
        Object userIdObj = claims.get("userId");
        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }
        return null;
    }
}