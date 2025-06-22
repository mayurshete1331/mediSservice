// src/main/java/com/example/demo/controller/AuthController.java
package com.example.demo.controller;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.model.UserCredential;
import com.example.demo.service.JwtService;
import com.example.demo.service.UserCredentialService; // Ensure this is the UserCredentialService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;
import java.util.Set; // Import Set

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserCredentialService userCredentialService; // Renamed from userService for clarity

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            if (authentication.isAuthenticated()) {
                UserCredential user = userCredentialService.findByUsername(authRequest.getUsername())
                        .orElseThrow(() -> new UsernameNotFoundException("User not found after successful authentication"));

                // Original problematic line: Set<String> roles = user.getRoles() != null ? user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()) : Set.of();

                Set<String> roles = (user.getRole() != null)
                        ? Set.of(user.getRole().name()) // Convert single Role to a Set containing its name
                        : Set.of();

                // --- IMPORTANT CHANGE HERE ---
                // If you modified JwtService.generateToken to accept userId (e.g., generateToken(userName, userId, roles))
                String token = jwtService.generateToken(user.getUsername(), user.getId(), roles);
                // If your JwtService.generateToken method does NOT accept userId,
                // you would keep the original call:
                // String token = jwtService.generateToken(user.getUsername(), user.getRoles());
                // -----------------------------

                return ResponseEntity.ok(new AuthResponse(token, user.getId(), roles));
            } else {
                // ... (rest of the error handling remains the same)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse("Authentication failed", null, null));
            }
        } catch (UsernameNotFoundException e) {
            // ... (rest of the error handling remains the same)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse("User not found", null, null));
        } catch (Exception e) {
            // ... (rest of the error handling remains the same)
            System.err.println("Authentication error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse("Invalid username or password", null, null));
        }
    }
    // MODIFIED: Accepts AuthRequest for registration
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthRequest authRequest) { // Change UserCredential to AuthRequest
        try {
            // Call the new registerUser method in UserCredentialService
            userCredentialService.registerUser(authRequest);
            return ResponseEntity.ok("User registered successfully!");
        } catch (RuntimeException e) { // Catch RuntimeException for specific messages from service
            System.err.println("Registration error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed: " + e.getMessage());
        } catch (Exception e) { // Catch any other unexpected exceptions
            System.err.println("Unexpected registration error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred during registration.");
        }
    }
}