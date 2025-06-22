// src/main/java/com/example/demo/repository/InvoiceLineItemRepository.java
package com.example.demo.repository;

import com.example.demo.model.InvoiceLineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceLineItemRepository extends JpaRepository<InvoiceLineItem, Long> {
    // No specific methods needed for now, as they are managed via Invoice cascade
}