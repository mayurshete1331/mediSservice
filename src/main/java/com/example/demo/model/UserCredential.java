package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

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

    @Enumerated(EnumType.STRING) // Store the enum name as a string
    @Column(name = "role_id") // This will create a column named role_id in user_credential table
    private Role role; // Changed from Set<Role> to single Role

    @OneToOne(mappedBy = "userCredential", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    private UserDetail userDetail;

    public UserCredential(String username, String password, Role role) { // Constructor updated
        this.username = username;
        this.password = password;
        this.role = role;
    }
}
