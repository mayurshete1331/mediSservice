// src/main/java/com/example/demo/model/UserDetail.java
package com.example.demo.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList; // For initializing the list

@Entity // Marks this class as a JPA entity
@Table(name = "user_details") // Maps to a table named 'user_details' in the database
public class UserDetail {

    @Id // Marks id as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generates ID for new entities
    private Long id;

    @Column(unique = true, nullable = false) // Ensures username is unique and not null
    private String username; // Linking to UserCredential's username

    @Column(nullable = false)
    private String fullName;

    private String contactNumber; // New field for contact number
    private String email;
    private String address;

    // One-to-Many relationship with UserHistory
    // CascadeType.ALL: Operations on UserDetail cascade to UserHistory
    // orphanRemoval = true: Removes UserHistory records if they are no longer linked to a UserDetail
    @OneToMany(mappedBy = "userDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserHistory> history = new ArrayList<>(); // Initialize to prevent NullPointerExceptions

    // Constructors
    public UserDetail() {
    }

    public UserDetail(String username, String fullName, String contactNumber, String email, String address) {
        this.username = username;
        this.fullName = fullName;
        this.contactNumber = contactNumber;
        this.email = email;
        this.address = address;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<UserHistory> getHistory() {
        return history;
    }

    public void setHistory(List<UserHistory> history) {
        this.history = history;
    }

    // Helper method to add history entry
    public void addHistory(UserHistory entry) {
        history.add(entry);
        entry.setUserDetail(this); // Set the back-reference
    }

    // Helper method to remove history entry
    public void removeHistory(UserHistory entry) {
        history.remove(entry);
        entry.setUserDetail(null);
    }
}
