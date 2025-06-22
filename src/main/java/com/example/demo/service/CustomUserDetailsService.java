// src/main/java/com/example/demo/service/CustomUserDetailsService.java
package com.example.demo.service;

import com.example.demo.model.UserCredential;
import com.example.demo.model.Roles; // Import the Roles entity
import com.example.demo.model.Role;   // Import the Role enum
import com.example.demo.repository.UserCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections; // Import Collections for singletonList
import java.util.stream.Collectors; // Keep for general use, though not strictly needed for single role

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserCredentialRepository userCredentialRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserCredential userCredential = userCredentialRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Get the Roles entity from UserCredential
        Roles userRolesEntity = userCredential.getRole();

        Collection<? extends GrantedAuthority> authorities;

        // Check if the Roles entity and its roleName are not null
        if (userRolesEntity != null && userRolesEntity.getRoleName() != null) {
            // Get the Role enum from the Roles entity, then its name string
            String roleNameString = userRolesEntity.getRoleName().name();
            // Create a SimpleGrantedAuthority with the "ROLE_" prefix
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleNameString));
        } else {
            // If no role is found, return an empty list of authorities
            authorities = Collections.emptyList();
        }

        return new User(userCredential.getUsername(), userCredential.getPassword(), authorities);
    }
}
