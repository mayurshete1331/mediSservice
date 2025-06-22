package com.example.demo.dto;

import java.util.Set;

public class AuthResponse {

private String token;
private Long userId;        // NEW: Field for User ID
private Set<String> roles; // NEW: Field for User Roles (as strings)

// Modified Constructor to include new fields
public AuthResponse(String token, Long userId, Set<String> roles) {
    this.token = token;
    this.userId = userId;
    this.roles = roles;
}

public String getToken() {
    return token;
}

// NEW: Getter for userId
public Long getUserId() {
    return userId;
}

// NEW: Getter for roles
public Set<String> getRoles() {
    return roles;
}

// Optional: You can add setters if needed, but for an immutable response DTO, getters are usually enough.
}
