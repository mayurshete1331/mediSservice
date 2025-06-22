// src/main/java/com/example/demo/controller/InvoiceUploadController.java
package com.example.demo.controller;

import com.example.demo.model.Invoice;
import com.example.demo.service.InvoiceProcessingService; // Changed service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import net.sourceforge.tess4j.TesseractException;

import java.io.IOException;
import java.util.List;

//@RestController
@RequestMapping("/api/invoices") // Changed request mapping
public class InvoiceUploadController {

    @Autowired
    private InvoiceProcessingService invoiceProcessingService; // Changed service

    /**
     * Endpoint to upload an invoice document (image or PDF), extract and parse data,
     * and store it in the database as an Invoice entity.
     * The user (pharmacist) must be authenticated.
     *
     * @param file The invoice file to upload.
     * @return ResponseEntity with the saved Invoice entity or an error message.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadInvoice(@RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        if (username == null || username.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required or username not found.");
        }

        try {
            Invoice savedInvoice = invoiceProcessingService.processAndSaveInvoice(file, username);
            return ResponseEntity.ok(savedInvoice); // Return the saved Invoice details
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing file: " + e.getMessage());
        } catch (TesseractException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during OCR text extraction (Tesseract): " + e.getMessage());
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            System.err.println("Unexpected error during invoice upload: " + e.getMessage());
            e.printStackTrace(); // Log stack trace for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred during invoice processing.");
        }
    }

    // You can add more endpoints here, e.g., to retrieve invoices for a specific pharmacist
    @GetMapping("/{invoiceId}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable Long invoiceId) {
        return invoiceProcessingService.getInvoiceById(invoiceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // You might also add a method to get all invoices for the authenticated user
    @GetMapping("/my-invoices")
    public ResponseEntity<List<Invoice>> getMyInvoices() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        if (username == null || username.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(invoiceProcessingService.getInvoicesByUploadedUser(username));
    }
}