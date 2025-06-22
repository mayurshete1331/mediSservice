// src/main/java/com/example/demo/model/UserCredential.java
package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_credential")
public class UserCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    // Many-to-One relationship with the Roles entity (which maps to app_role table)
    @ManyToOne(fetch = FetchType.EAGER) // Fetch role eagerly as each user has one role
    @JoinColumn(name = "role_id", nullable = false) // This creates the role_id foreign key column in user_credential
    private Roles role; // Each UserCredential now holds a single Roles entity

    @OneToOne(mappedBy = "userCredential", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    private UserDetail userDetail;

    // Constructor updated to accept a Roles entity directly
    public UserCredential(String username, String password, Roles role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}
