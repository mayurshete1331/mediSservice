// src/main/java/com/example/demo/controller/AuthController.java
package com.example.demo.controller;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.model.UserCredential;
// Ensure this is the UserCredentialService
import com.example.demo.service.JwtService;
import com.example.demo.service.UserCredentialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Set; // Import Set
import java.util.stream.Collectors; // Import Collectors

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserCredentialService userCredentialService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        try {
            // Attempt to authenticate the user using Spring Security's AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            // If authentication is successful
            if (authentication.isAuthenticated()) {
                // Retrieve the UserCredential object from the database using the username
                UserCredential user = userCredentialService.findByUsername(authRequest.getUsername())
                        .orElseThrow(() -> new UsernameNotFoundException("User not found after successful authentication"));

                // Extract roles from the UserCredential for the AuthResponse (even if not in token)
                Set<String> roles;
                if (user.getRole() != null && user.getRole().getRoleName() != null) {
                    // Convert the single Role enum name to a Set<String> for consistent response
                    roles = Set.of(user.getRole().getRoleName().name());
                } else {
                    roles = Set.of(); // If no role is found, return an empty set
                }

                // --- START MODIFICATION FOR SIMPLER TOKEN ---
                // For testing purposes, generate a simpler token that does NOT contain userId or roles as claims.
                // The JwtService.generateSimpleToken method will create a token with only the username (subject).
                String token = jwtService.generateSimpleToken(user.getUsername());
                // --- END MODIFICATION FOR SIMPLER TOKEN ---

                // Return the generated token, userId, and roles in the response body.
                // Note: userId and roles are still sent in the AuthResponse, but not embedded in the JWT itself.
                return ResponseEntity.ok(new AuthResponse(token, user.getId(), roles));
            } else {
                // If authentication fails (though unlikely if authenticate() didn't throw an exception)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse("Authentication failed", null, null));
            }
        } catch (UsernameNotFoundException e) {
            // Handle case where user is not found during authentication
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse("User not found", null, null));
        } catch (Exception e) {
            // Catch any other authentication-related exceptions
            System.err.println("Authentication error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse("Invalid username or password", null, null));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthRequest authRequest) {
        try {
            // Delegate user registration to the UserCredentialService
            userCredentialService.registerUser(authRequest);
            return ResponseEntity.ok("User registered successfully!");
        } catch (RuntimeException e) {
            // Handle specific registration-related exceptions (e.g., username already exists)
            System.err.println("Registration error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed: " + e.getMessage());
        } catch (Exception e) {
            // Handle any unexpected errors during registration
            System.err.println("Unexpected registration error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred during registration.");
        }
    }
}
