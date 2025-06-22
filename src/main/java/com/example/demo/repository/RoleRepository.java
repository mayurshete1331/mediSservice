// src/main/java/com/example/demo/repository/RoleRepository.java
package com.example.demo.repository;

import com.example.demo.model.Roles; // Import your Roles entity
import com.example.demo.model.Role;   // Import your Role enum
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Roles, Long> {

    // Method to find a Roles entity by its roleName (e.g., Role.ADMIN, Role.DOCTOR)
    Optional<Roles> findByRoleName(Role roleName);
}
