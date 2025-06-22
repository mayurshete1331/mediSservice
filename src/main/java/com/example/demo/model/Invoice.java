// src/main/java/com/example/demo/model/Invoice.java
package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Getter // Make sure Lombok is generating getters
@Setter // Make sure Lombok is generating setters
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String invoiceNumber;

    private LocalDate invoiceDate;

    // NEW FIELD ADDED HERE:
    @Column(nullable = false) // Assuming original file name is always present
    private String originalFileName; // To store the name of the uploaded file

    // Seller Details (from whom the pharmacist bought)
    private String sellerName;
    private String sellerAddress;
    private String sellerGstin; // GST Identification Number for India

    // Buyer Details (the pharmacist/pharmacy)
    private String buyerName;
    private String buyerAddress;
    private String buyerGstin;

    @Column(columnDefinition = "TEXT") // Store the full extracted text for debugging/re-processing
    private String rawExtractedText;

    private BigDecimal subTotal;
    private BigDecimal taxAmount;
    private BigDecimal grandTotal;

    private String currency; // e.g., "INR", "USD"

    @Column(nullable = false)
    private LocalDateTime uploadTimestamp;

    // Line items for the invoice (One-to-Many relationship)
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceLineItem> lineItems = new ArrayList<>();

    // Optional: Link to the UserDetail who uploaded this invoice
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_detail_id")
    private UserDetail uploadedByUserDetail;

    @PrePersist
    protected void onCreate() {
        this.uploadTimestamp = LocalDateTime.now();
    }

    public void addLineItem(InvoiceLineItem item) {
        lineItems.add(item);
        item.setInvoice(this);
    }

    public void removeLineItem(InvoiceLineItem item) {
        lineItems.remove(item);
        item.setInvoice(null);
    }
}