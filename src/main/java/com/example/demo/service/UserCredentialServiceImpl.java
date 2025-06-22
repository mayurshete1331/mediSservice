package com.example.demo.service;

import com.example.demo.dto.AuthRequest;
import com.example.demo.model.UserCredential;
import com.example.demo.model.Role;
import com.example.demo.repository.UserCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UserCredentialServiceImpl implements UserCredentialService {

    @Autowired
    private UserCredentialRepository userCredentialRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserCredential save(UserCredential userCredential) {
        // Optionally encode password if not already hashed
        return userCredentialRepository.save(userCredential);
    }

    @Override
    public Optional<UserCredential> findByUsername(String username) {
        return userCredentialRepository.findByUsername(username);
    }

    @Override
    public Optional<UserCredential> findById(Long id) {
        return userCredentialRepository.findById(id);
    }

    @Override
    @Transactional
    public UserCredential registerUser(AuthRequest authRequest) {
        if (authRequest.getUserrole() == null || authRequest.getUserrole().isBlank()) {
            throw new RuntimeException("User role is required for registration.");
        }

        if (userCredentialRepository.findByUsername(authRequest.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists!");
        }

        UserCredential newUser = new UserCredential();
        newUser.setUsername(authRequest.getUsername());
        newUser.setPassword(passwordEncoder.encode(authRequest.getPassword()));

        Set<Role> roles = new HashSet<>();
        try {
            Role userRole = Role.valueOf(authRequest.getUserrole().toUpperCase());
            roles.add(userRole);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role specified: " + authRequest.getUserrole(), e);
        }

        // Assuming you want to assign a default role (e.g., USER) during registration
        newUser.setRole(Role.STAFF); // Set a single default role
        // If your AuthRequest includes a role and you want to use it:
        // newUser.setRole(authRequest.getRole()); // Requires getRole() in AuthRequest

        // The line 'newUser.setRoles(roles);' is completely removed/replaced.
        return userCredentialRepository.save(newUser);
    }
}
