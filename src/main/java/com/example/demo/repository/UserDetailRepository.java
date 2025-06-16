// src/main/java/com/example/demo/repository/UserDetailRepository.java
package com.example.demo.repository;

import com.example.demo.model.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDetailRepository extends JpaRepository<UserDetail, Long> {

    // Find by username (unique identifier linking to UserCredential)
    Optional<UserDetail> findByUsername(String username);

    // Find by contact number (assuming it's a unique identifier for search)
    Optional<UserDetail> findByContactNumber(String contactNumber);
}
