// src/main/java/com/example/demo/service/InvoiceProcessingService.java
package com.example.demo.service;

import com.example.demo.model.Invoice;
import com.example.demo.model.InvoiceLineItem;
import com.example.demo.model.UserDetail;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.UserDetailRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // For transactional operations
import org.springframework.web.multipart.MultipartFile;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class InvoiceProcessingService { // Renamed from DocumentProcessingService

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private UserDetailRepository userDetailRepository;

    private Tesseract tesseract;

    public InvoiceProcessingService() {
        tesseract = new Tesseract();
        // IMPORTANT: Configure your Tesseract tessdata path here
        tesseract.setDatapath("/path/to/tessdata"); // <<< YOU MUST CONFIGURE THIS
        tesseract.setLanguage("eng");
    }


    @Transactional
    public Invoice processAndSaveInvoice(MultipartFile file, String username)
            throws IOException, TesseractException, IllegalArgumentException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty.");
        }

        String fileType = file.getContentType();
        String rawExtractedText = "";

        Path tempFilePath = Files.createTempFile("uploaded-invoice-", "." + getFileExtension(file.getOriginalFilename()));
        file.transferTo(tempFilePath.toFile());

        try {
            if (fileType != null && fileType.startsWith("application/pdf")) {
                rawExtractedText = extractTextFromPdf(tempFilePath.toFile());
            } else if (fileType != null && (fileType.startsWith("image/"))) {
                rawExtractedText = extractTextFromImage(tempFilePath.toFile());
            } else {
                throw new IllegalArgumentException("Unsupported file type: " + fileType);
            }
        } finally {
            Files.delete(tempFilePath); // Clean up temp file
        }

        // --- Core Invoice Parsing Logic ---
        Invoice invoice = parseInvoiceText(rawExtractedText);
        invoice.setOriginalFileName(file.getOriginalFilename()); // Store original file name
        invoice.setRawExtractedText(rawExtractedText); // Store full text for review/debugging

        // Link to UserDetail (pharmacist)
        Optional<UserDetail> uploadedByUserDetail = userDetailRepository.findByUsername(username);
        uploadedByUserDetail.ifPresent(invoice::setUploadedByUserDetail);

        return invoiceRepository.save(invoice);
    }

    // --- Private Extraction Methods (Same as before) ---
    private String extractTextFromPdf(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }

    private String extractTextFromImage(File imageFile) throws IOException, TesseractException {
        BufferedImage image = ImageIO.read(imageFile);
        if (image == null) {
            throw new IOException("Could not read image file.");
        }
        return tesseract.doOCR(image);
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }


    private Invoice parseInvoiceText(String rawText) {
        Invoice invoice = new Invoice();
        invoice.setRawExtractedText(rawText); // Store raw text for audit/debugging

        // Example Regex Patterns (Highly dependent on actual invoice formats)
        // These are illustrative and need to be adapted to your specific invoice layouts.
        Pattern invoiceNumPattern = Pattern.compile("Invoice No[.:]?\\s*([A-Za-z0-9-]+)");
        Pattern datePattern = Pattern.compile("Date[.:]?\\s*(\\d{2}[-/.]\\d{2}[-/.]\\d{4})"); // DD-MM-YYYY or DD/MM/YYYY or DD.MM.YYYY
        Pattern totalPattern = Pattern.compile("(Total|Grand Total|Amount Due)[.:]?\\s*[A-Z]{2,3}\\s*([0-9.,]+)"); // e.g., Total: INR 123.45

        Matcher matcher;

        // Invoice Number
        matcher = invoiceNumPattern.matcher(rawText);
        if (matcher.find()) {
            invoice.setInvoiceNumber(matcher.group(1));
        } else {
            invoice.setInvoiceNumber("UNKNOWN_" + System.currentTimeMillis()); // Fallback
        }

        // Invoice Date
        matcher = datePattern.matcher(rawText);
        if (matcher.find()) {
            String dateStr = matcher.group(1);
            try {
                // Try multiple common date formats
                if (dateStr.contains("-")) {
                    invoice.setInvoiceDate(LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd-MM-yyyy")));
                } else if (dateStr.contains("/")) {
                    invoice.setInvoiceDate(LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                } else if (dateStr.contains(".")) {
                    invoice.setInvoiceDate(LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                }
            } catch (DateTimeParseException e) {
                System.err.println("Could not parse date: " + dateStr + " - " + e.getMessage());
            }
        }

        Pattern sellerNamePattern = Pattern.compile("Seller:\\s*(.+?)\\n");
        matcher = sellerNamePattern.matcher(rawText);
        if (matcher.find()) {
            invoice.setSellerName(matcher.group(1).trim());
        } else {
            // Fallback or try another pattern
            Pattern firmNamePattern = Pattern.compile("(?i)(pharmacy|chemists|medicines?|Pvt\\.? Ltd\\.?|Ltd\\.?|Corp\\.|LLP)\\s*(.+?)\\n");
            matcher = firmNamePattern.matcher(rawText);
            if (matcher.find()) {
                invoice.setSellerName(matcher.group(1).trim());
            }
        }

        // Grand Total and Currency
        matcher = totalPattern.matcher(rawText);
        if (matcher.find()) {
            String totalStr = matcher.group(2).replace(",", ""); // Remove commas for parsing
            try {
                invoice.setGrandTotal(new BigDecimal(totalStr));
                if (matcher.group(0).contains("INR")) { // Simple currency detection
                    invoice.setCurrency("INR");
                } else {
                    invoice.setCurrency("UNKNOWN");
                }
            } catch (NumberFormatException e) {
                System.err.println("Could not parse total: " + totalStr + " - " + e.getMessage());
            }
        }

        Pattern lineItemPattern = Pattern.compile("(.+?)\\s+(\\d+)\\s+([0-9.]+)\\s+([0-9.]+)");
        String[] lines = rawText.split("\\n");
        for (String line : lines) {
            Matcher itemMatcher = lineItemPattern.matcher(line.trim());
            if (itemMatcher.find()) {
                try {
                    InvoiceLineItem item = new InvoiceLineItem();
                    item.setDescription(itemMatcher.group(1).trim());
                    item.setQuantity(Integer.parseInt(itemMatcher.group(2)));
                    item.setUnitPrice(new BigDecimal(itemMatcher.group(3)));
                    item.setLineTotal(new BigDecimal(itemMatcher.group(4)));
                    invoice.addLineItem(item); // Add to the invoice
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing line item: " + line + " - " + e.getMessage());
                }
            }
        }

        // You would similarly extract subTotal, taxAmount, seller/buyer address, GSTIN etc.
        // This requires custom regex patterns for each field based on typical invoice layouts.

        return invoice;
    }
    public Optional<Invoice> getInvoiceById(Long invoiceId) {
        return invoiceRepository.findById(invoiceId);
    }

    // Assuming UserDetail has a findByUsername method or you retrieve it via repo
    public List<Invoice> getInvoicesByUploadedUser(String username) {
        return userDetailRepository.findByUsername(username)
                .map(invoiceRepository::findByUploadedByUserDetail) // Assuming you add this method to InvoiceRepository
                .orElse(List.of()); // Return empty list if user not found
    }
}