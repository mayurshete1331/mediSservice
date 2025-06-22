// src/main/java/com/example/demo/service/UserCredentialService.java
package com.example.demo.service;

import com.example.demo.dto.AuthRequest;
import com.example.demo.model.Role; // Import the Role enum
import com.example.demo.model.UserCredential;

import java.util.Optional;

public interface UserCredentialService {
    Optional<UserCredential> findByUsername(String username);
    Optional<UserCredential> findById(Long id); // Uncommented this line to resolve compilation error
    UserCredential registerUser(AuthRequest authRequest);
    UserCredential createAndSaveUserWithRole(String username, String password, Role roleEnum);

    // This signature must exactly match the one in UserCredentialServiceImpl
    UserCredential save(UserCredential userCredential);
}
