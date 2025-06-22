// src/main/java/com/example/demo/model/Role.java
package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "app_role")
public class Roles { // Renamed from Role to Roles in previous interactions
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // This will be the role_id in the 'app_role' table

    // Changed type back to Role enum for the role name, and re-added @Enumerated
    @Enumerated(EnumType.STRING) // Store the enum name as a string (e.g., "ADMIN")
    @Column(name = "role_name", unique = true, nullable = false) // Column to store the role name
    private Role roleName; // Now a 'Role' enum type again

    // Convenience constructor updated to accept a 'Role' enum
    public Roles(Role roleName) {
        this.roleName = roleName;
    }
}