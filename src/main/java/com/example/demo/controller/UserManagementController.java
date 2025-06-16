// src/main/java/com/example/demo/controller/UserManagementController.java
package com.example.demo.controller;

import com.example.demo.model.UserDetail;
import com.example.demo.model.UserHistory;
import com.example.demo.service.UserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // For role-based access
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserManagementController {

    @Autowired
    private UserDetailService userDetailService;

    // Endpoint to add new user details (only accessible by ADMIN)
    // This is for adding detailed user profiles after initial registration
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')") // Only ADMIN can add user details
    public ResponseEntity<UserDetail> addUserDetail(@RequestBody UserDetail userDetail) {
        try {
            UserDetail newUser = userDetailService.addUserDetail(userDetail);
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Or return error message
        }
    }

    // Endpoint to search for a user by username or contact number
    // Accessible by ADMIN and DOCTOR
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF')") // Example: ADMINs, DOCTORs, STAFF can search
    public ResponseEntity<UserDetail> searchUser(@RequestParam String searchTerm) {
        Optional<UserDetail> userDetail = userDetailService.findUserBySearchTerm(searchTerm);
        return userDetail.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Endpoint to get user history
    // Accessible by ADMIN and DOCTOR
    @GetMapping("/{username}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF')") // Example: ADMINs, DOCTORs, STAFF can view history
    public ResponseEntity<List<UserHistory>> getUserHistory(@PathVariable String username) {
        Optional<UserDetail> userDetailOptional = userDetailService.findUserBySearchTerm(username); // Search by username here
        if (userDetailOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Long userDetailId = userDetailOptional.get().getId();
        List<UserHistory> history = userDetailService.getUserHistory(userDetailId);
        return ResponseEntity.ok(history);
    }

    // Endpoint to add a history entry to a user
    // Accessible by ADMIN and DOCTOR
    @PostMapping("/{username}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF')") // Example: ADMINs, DOCTORs, STAFF can add history
    public ResponseEntity<UserHistory> addHistory(@PathVariable String username, @RequestBody String description) {
        try {
            UserHistory newEntry = userDetailService.addHistoryToUser(username, description);
            return ResponseEntity.status(HttpStatus.CREATED).body(newEntry);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // Endpoint to update user details
    // Accessible by ADMIN
    @PutMapping("/update/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDetail> updateUserDetail(@PathVariable String username, @RequestBody UserDetail updatedUserDetail) {
        try {
            UserDetail updated = userDetailService.updateUserDetail(username, updatedUserDetail);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Or return error message
        }
    }
}
