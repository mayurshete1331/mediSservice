// src/main/java/com/example/demo/service/CustomUserDetailsService.java
package com.example.demo.service;

import com.example.demo.model.UserCredential; // Import your UserCredential model
import com.example.demo.model.Role; // NEW: Import your Role enum
import com.example.demo.repository.UserCredentialRepository; // Import your UserCredentialRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User; // Spring Security's User object
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService; // Interface to implement
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * CustomUserDetailsService implements Spring Security's UserDetailsService interface.
 * It is responsible for loading user-specific data during the authentication process.
 * When a user attempts to log in, Spring Security calls loadUserByUsername()
 * to retrieve the user's details, including their hashed password and authorities (roles).
 */
@Service // Marks this class as a Spring Service, making it discoverable for dependency injection
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserCredentialRepository userCredentialRepository; // Autowire your repository to fetch user data

    /**
     * Locates the user based on the username. In the actual authentication process,
     * the passed-in username will be from the login attempt.
     * This method is called by Spring Security's AuthenticationManager.
     *
     * @param username The username identifying the user whose data is required.
     * @return A UserDetails object that Spring Security uses for authentication and authorization.
     * @throws UsernameNotFoundException if the user could not be found or the user has no GrantedAuthority.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Fetch UserCredential from your database using the repository
        UserCredential userCredential = userCredentialRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // 2. Convert your custom UserCredential roles (Role enum) to Spring Security's GrantedAuthority objects.
        // Spring Security expects roles to be prefixed with "ROLE_".
        // The Role enum already includes the "ROLE_" prefix, so we just need to get its name().
        Collection<SimpleGrantedAuthority> authorities;
        if (userCredential.getRoles() != null) {
            authorities = userCredential.getRoles().stream()
                    .map(Role::name) // Corrected: Use Role::name() to get the string representation of the enum
                    .map(SimpleGrantedAuthority::new) // Create SimpleGrantedAuthority directly from the string
                    .collect(Collectors.toList());
        } else {
            authorities = Collections.emptyList(); // Handle case where roles might be null
        }

        // 3. Return a Spring Security User object.
        // This User object contains the username, HASHED password, and authorities.
        // Spring Security will then compare the provided (raw) password with this hashed password.
        return new User(
                userCredential.getUsername(),
                userCredential.getPassword(), // This MUST be the HASHED password from the database
                authorities
        );
    }
}
