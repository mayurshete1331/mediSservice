// src/main/java/com/example/demo/model/UserHistory.java
package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference; // Prevents infinite recursion in JSON serialization
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "user_history")
public class UserHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description; // What happened
    private LocalDateTime timestamp; // When it happened

    // Many-to-One relationship with UserDetail
    @ManyToOne(fetch = FetchType.LAZY) // LAZY loading is generally better for performance
    @JoinColumn(name = "user_detail_id", nullable = false) // Foreign key column
    @JsonBackReference // This is crucial to avoid infinite recursion when serializing UserDetail
    private UserDetail userDetail;

    // Constructors
    public UserHistory() {
        this.timestamp = LocalDateTime.now(); // Default to current time
    }

    public UserHistory(String description, UserDetail userDetail) {
        this.description = description;
        this.timestamp = LocalDateTime.now();
        this.userDetail = userDetail;
    }

    // Getters and Setters
}
