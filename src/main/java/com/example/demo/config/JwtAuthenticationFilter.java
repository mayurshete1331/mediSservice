// src/main/java/com/example/demo/config/JwtAuthenticationFilter.java
package com.example.demo.config;

import com.example.demo.service.CustomUserDetailsService;
import com.example.demo.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // Marks this as a Spring component for dependency injection
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService; // To handle JWT token operations

    @Autowired
    private CustomUserDetailsService customUserDetailsService; // To load UserDetails from username

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        // 1. Check for Authorization header and Bearer token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // Extract the actual token (after "Bearer ")
            username = jwtService.extractUsername(token); // Extract username from token using JwtService
        }

        // 2. If username is found and no authentication is currently set in SecurityContext
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username); // Load user details

            // 3. Validate the token with the loaded user details
            // The validation here will now be more robust due to changes in JwtService
            if (jwtService.validateToken(token, userDetails)) {
                // If token is valid, create an Authentication object
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 4. Set the authentication object in the SecurityContext
                // This marks the user as authenticated for the current request
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // 5. Continue the filter chain
        filterChain.doFilter(request, response);
    }
}