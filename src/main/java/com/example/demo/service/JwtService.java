// src/main/java/com/example/demo/service/JwtService.java
package com.example.demo.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm; // This import is not strictly necessary for HS256 with Keys.hmacShaKeyFor
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtService {

    // NOTE: Store this in a secure place (like environment variables)
    private static final String SECRET = "YS1zdHJpbmctc2VjcmV0LWF0LWxlYXN0LTI1Ni1iaXRzLWxvbmc=";
    private static final long EXPIRATION_MILLIS = 1000 * 60 * 30; // 30 minutes

    /**
     * Generates a JWT token for the user with roles and userId.
     * This method will still be available if you decide to revert to full claims.
     */
    public String generateToken(String userName, Long userId, Set<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roles", new ArrayList<>(roles));
        return createToken(claims, userName);
    }

    /**
     * NEW METHOD: Generates a simpler JWT token with only the username as subject.
     * This token will NOT contain userId or roles in its claims.
     * This is intended for temporary testing where you want to bypass role/ID checks within the token itself.
     */
    public String generateSimpleToken(String userName) {
        Map<String, Object> claims = new HashMap<>(); // Empty claims for this simplified token
        return createToken(claims, userName);
    }

    /**
     * Creates the JWT token with claims and subject.
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_MILLIS);

        return Jwts.builder()
                .claims(claims) // Use the provided claims map (can be empty for simple token)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSignKey())
                .compact();
    }

    /**
     * Builds the correct SecretKey for signing and verifying JWTs using HS256.
     */
    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extracts username (subject) from token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts expiration date from token.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic claim extraction method.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims using JJWT 0.12.x parser.
     */
    private Claims extractAllClaims(String token) {
        // Build and parse the token to get all claims
        return Jwts.parser()
                .verifyWith(getSignKey()) // Use the secret key for verification
                .build()
                .parseSignedClaims(token)
                .getPayload(); // Get the claims payload
    }

    /**
     * Checks if token is expired.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validates token's username and expiration against UserDetails.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // Token is valid if username matches UserDetails' username and token is not expired
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
}
