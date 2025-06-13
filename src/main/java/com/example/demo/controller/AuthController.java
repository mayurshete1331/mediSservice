// src/main/java/com/example/demo/controller/AuthController.java
package com.example.demo.controller;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.model.UserCredential;
import com.example.demo.service.JwtService;
import com.example.demo.service.UserCredentialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // Import HttpStatus
import org.springframework.http.ResponseEntity; // Import ResponseEntity
import org.springframework.security.authentication.AuthenticationManager; // Import AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Import
import org.springframework.security.core.Authentication; // Import
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Import for specific exception handling
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
// You can add @CrossOrigin here too, though global config in SecurityConfig is usually enough
// @CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserCredentialService userService; // Still needed for register and retrieving UserCredential object

    @Autowired
    private AuthenticationManager authenticationManager; // Autowire AuthenticationManager

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        try {
            // Use AuthenticationManager to authenticate the user
            // This will trigger CustomUserDetailsService.loadUserByUsername()
            // and then compare passwords using the configured PasswordEncoder.
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            // If authentication is successful (no exception thrown), generate JWT
            if (authentication.isAuthenticated()) {
                // Retrieve the full UserCredential object to get roles for JWT generation
                // The principal from authentication might be Spring Security's UserDetails,
                // so we fetch our UserCredential directly.
                UserCredential user = userService.findByUsername(authRequest.getUsername())
                        .orElseThrow(() -> new UsernameNotFoundException("User not found after successful authentication"));

                // Generate JWT token with username and roles
                String token = jwtService.generateToken(user.getUsername(), user.getRoles());
                return ResponseEntity.ok(new AuthResponse(token)); // Return 200 OK with token
            } else {
                // This block should theoretically not be reached if authenticate() throws
                // an exception on failure, but included for explicit handling.
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse("Authentication failed"));
            }
        } catch (UsernameNotFoundException e) {
            // Handles cases where the user doesn't exist in the database
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse("User not found"));
        } catch (Exception e) {
            // Catches BadCredentialsException (incorrect password) and other authentication exceptions
            System.err.println("Authentication error: " + e.getMessage()); // Log the error for debugging
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse("Invalid username or password"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserCredential user) {
        try {
            userService.save(user); // Password hashing is handled inside userService.save()
            return ResponseEntity.ok("User registered successfully!");
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed: " + e.getMessage());
        }
    }
}
