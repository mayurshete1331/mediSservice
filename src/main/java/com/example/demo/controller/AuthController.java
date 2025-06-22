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
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            if (authentication.isAuthenticated()) {
                UserCredential user = userCredentialService.findByUsername(authRequest.getUsername())
                        .orElseThrow(() -> new UsernameNotFoundException("User not found after successful authentication"));

                // Get the single role entity from UserCredential, then its Role enum name, then convert to a Set<String>
                Set<String> roles;
                if (user.getRole() != null && user.getRole().getRoleName() != null) {
                    roles = Set.of(user.getRole().getRoleName().name()); // Access the Role enum name from the Roles entity
                } else {
                    roles = Set.of(); // No role found, return an empty set
                }

                // Call JwtService.generateToken with the username, userId, and the Set<String> of roles
                String token = jwtService.generateToken(user.getUsername(), user.getId(), roles);

                return ResponseEntity.ok(new AuthResponse(token, user.getId(), roles));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse("Authentication failed", null, null));
            }
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse("User not found", null, null));
        } catch (Exception e) {
            System.err.println("Authentication error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse("Invalid username or password", null, null));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthRequest authRequest) {
        try {
            // The UserCredentialService will handle finding/setting the Roles entity based on default or provided logic
            userCredentialService.registerUser(authRequest);
            return ResponseEntity.ok("User registered successfully!");
        } catch (RuntimeException e) {
            System.err.println("Registration error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected registration error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred during registration.");
        }
    }
}
