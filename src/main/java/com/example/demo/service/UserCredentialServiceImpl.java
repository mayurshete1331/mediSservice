// src/main/java/com/example/demo/service/UserCredentialServiceImpl.java
package com.example.demo.service;

import com.example.demo.dto.AuthRequest;
import com.example.demo.model.Role; // Import the Role enum (e.g., ADMIN, PATIENT)
import com.example.demo.model.Roles; // Import the Roles entity (your app_role table entity)
import com.example.demo.model.UserCredential;
import com.example.demo.repository.RoleRepository; // Import RoleRepository
import com.example.demo.repository.UserCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
// No longer need HashSet or Set imports for roles directly in UserCredential,
// as a user has one Roles entity.
// import java.util.HashSet;
// import java.util.Set;

@Service
public class UserCredentialServiceImpl implements UserCredentialService {

    @Autowired
    private UserCredentialRepository userCredentialRepository;

    @Autowired
    private RoleRepository roleRepository; // Inject RoleRepository to find Roles entities

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserCredential save(UserCredential userCredential) {
        // This method can be used for general saving.
        // If password is not encoded, it should be done before calling this.
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
        // Validate that a role is provided in the AuthRequest
        if (authRequest.getUserrole() == null || authRequest.getUserrole().isBlank()) {
            throw new RuntimeException("User role is required for registration.");
        }

        // Check if username already exists
        if (userCredentialRepository.findByUsername(authRequest.getUsername()).isPresent()) {
            throw new RuntimeException("Username '" + authRequest.getUsername() + "' already exists!");
        }

        UserCredential newUser = new UserCredential();
        newUser.setUsername(authRequest.getUsername());
        newUser.setPassword(passwordEncoder.encode(authRequest.getPassword())); // Encode the password!

        // Determine the role from the AuthRequest and fetch the corresponding Roles entity
        Role targetRoleEnum;
        try {
            targetRoleEnum = Role.valueOf(authRequest.getUserrole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role specified: '" + authRequest.getUserrole() + "'. Valid roles are: " + java.util.Arrays.toString(Role.values()), e);
        }

        // Fetch the Roles entity from the database using the determined Role enum
        Roles targetRoleEntity = roleRepository.findByRoleName(targetRoleEnum)
                .orElseThrow(() -> new RuntimeException("Role '" + targetRoleEnum.name() + "' not found in database. Please ensure roles are pre-populated."));

        // Set the fetched Roles entity on the new user
        newUser.setRole(targetRoleEntity);

        // Save the new user credential
        return userCredentialRepository.save(newUser);
    }

    /**
     * Creates and saves a user with a specific role.
     * This method is useful for initial setup, testing, or admin functionality where a specific role is assigned.
     * It bypasses the AuthRequest and directly accepts the Role enum.
     *
     * @param username The username for the new user.
     * @param password The raw password for the new user.
     * @param roleEnum The Role enum to assign to the new user (e.g., Role.ADMIN, Role.DOCTOR, Role.STAFF).
     * @return The saved UserCredential object.
     * @throws RuntimeException if the username already exists or the specified role is not found in the database.
     */
    @Override // Ensure this method is present in UserCredentialService interface
    public UserCredential createAndSaveUserWithRole(String username, String password, Role roleEnum) {
        if (userCredentialRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username '" + username + "' already exists!");
        }

        // Fetch the specific Roles entity from the database using the provided Role enum
        Roles targetRoleEntity = roleRepository.findByRoleName(roleEnum)
                .orElseThrow(() -> new RuntimeException("Role '" + roleEnum.name() + "' not found in database. Please ensure roles are pre-populated."));

        UserCredential newUser = new UserCredential();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password)); // Encode the password!
        newUser.setRole(targetRoleEntity); // Set the fetched Roles entity

        return userCredentialRepository.save(newUser);
    }
}
