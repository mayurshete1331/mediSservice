// src/main/java/com/example/demo/repository/InvoiceRepository.java
package com.example.demo.repository;

import com.example.demo.model.Invoice;
import com.example.demo.model.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    // Add other query methods as needed
    List<Invoice> findByUploadedByUserDetail(UserDetail userDetail); // NEW method

}
