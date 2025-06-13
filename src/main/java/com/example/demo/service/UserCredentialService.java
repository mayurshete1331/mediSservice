// src/main/java/com/example/demo/service/UserCredentialService.java
package com.example.demo.service;

import com.example.demo.model.UserCredential;
import com.example.demo.repository.UserCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // Import the PasswordEncoder interface
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserCredentialService {
    @Autowired
    private UserCredentialRepository repo;

    // Corrected: Autowire the PasswordEncoder bean.
    // This ensures you're using the same encoder instance configured in SecurityConfig.
    @Autowired
    private PasswordEncoder passwordEncoder; // Injects the BCryptPasswordEncoder bean from SecurityConfig

    public Optional<UserCredential> findByUsername(String username) {
        return repo.findByUsername(username);
    }

    public UserCredential save(UserCredential user) {
        // IMPORTANT: Use the autowired passwordEncoder to hash the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return repo.save(user);
    }

    public boolean matches(String raw, String encoded) {
        // Use the autowired passwordEncoder to securely compare the raw password with the encoded one
        return passwordEncoder.matches(raw, encoded);
    }
}
