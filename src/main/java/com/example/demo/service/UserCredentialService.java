// src/main/java/com/example/demo/service/UserCredentialService.java (Interface)
package com.example.demo.service;

import com.example.demo.dto.AuthRequest; // NEW: Import AuthRequest
import com.example.demo.model.UserCredential;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public interface UserCredentialService {
    UserCredential save(UserCredential userCredential); // Existing method for general save
    Optional<UserCredential> findByUsername(String username);
    // NEW: Method for user registration using AuthRequest
    UserCredential registerUser(AuthRequest authRequest);
    // You might also want a method for finding by ID for login response
    Optional<UserCredential> findById(Long id);
}