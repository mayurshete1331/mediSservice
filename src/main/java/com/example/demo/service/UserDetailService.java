// src/main/java/com/example/demo/service/UserDetailService.java
package com.example.demo.service;

import com.example.demo.model.UserDetail;
import com.example.demo.model.UserHistory;
import com.example.demo.repository.UserDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // For transactional operations

import java.util.Optional;
import java.util.List;

@Service
public class UserDetailService {

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Autowired
    private UserCredentialService userCredentialService; // To link UserDetail to existing UserCredential

    // Add a new user with detailed information
    @Transactional
    public UserDetail addUserDetail(UserDetail userDetail) {
        // Optional: Perform a check here if userDetail.getUsername() exists in UserCredential
        // and if it's not already linked to a UserDetail.
        // For simplicity, we assume the username passed here corresponds to an existing UserCredential
        // or a new one is created simultaneously (if registration wasn't separate).
        // Since we have separate registration, ensure the username exists in UserCredential table.
        userCredentialService.findByUsername(userDetail.getUsername())
                .orElseThrow(() -> new RuntimeException("UserCredential not found for username: " + userDetail.getUsername()));

        // Check if a UserDetail already exists for this username (to prevent duplicates)
        if (userDetailRepository.findByUsername(userDetail.getUsername()).isPresent()) {
            throw new RuntimeException("User details already exist for username: " + userDetail.getUsername());
        }

        return userDetailRepository.save(userDetail);
    }

    // Find user by username or contact number
    public Optional<UserDetail> findUserBySearchTerm(String searchTerm) {
        // First try to find by username
        Optional<UserDetail> user = userDetailRepository.findByUsername(searchTerm);
        if (user.isPresent()) {
            return user;
        }
        // If not found by username, try by contact number
        return userDetailRepository.findByContactNumber(searchTerm);
    }

    // Get user history
    public List<UserHistory> getUserHistory(Long userDetailId) {
        return userDetailRepository.findById(userDetailId)
                .map(UserDetail::getHistory)
                .orElse(null); // Or throw an exception
    }

    // Add history to a user
    @Transactional
    public UserHistory addHistoryToUser(String username, String description) {
        UserDetail userDetail = userDetailRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User details not found for username: " + username));
        UserHistory newEntry = new UserHistory(description, userDetail);
        userDetail.addHistory(newEntry); // Add to the collection and set back-reference
        userDetailRepository.save(userDetail); // Save the updated UserDetail (cascades to history)
        return newEntry;
    }

    // Update user details
    @Transactional
    public UserDetail updateUserDetail(String username, UserDetail updatedUserDetail) {
        UserDetail existingUser = userDetailRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User details not found for username: " + username));

        existingUser.setFullName(updatedUserDetail.getFullName());
        existingUser.setContactNumber(updatedUserDetail.getContactNumber());
        existingUser.setEmail(updatedUserDetail.getEmail());
        existingUser.setAddress(updatedUserDetail.getAddress());
        // Do not update username via this method if it's the identifier for lookup

        return userDetailRepository.save(existingUser);
    }
}
