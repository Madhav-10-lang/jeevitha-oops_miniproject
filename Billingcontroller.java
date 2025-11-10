// src/main/java/com/app/controller/BillingController.java

package com.app.controller;

import com.app.model.Invoice;
import com.app.service.BillingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    // Endpoint to generate a new invoice
    // HTTP POST: http://localhost:8080/api/billing/generate
    @PostMapping("/generate")
    public ResponseEntity<Invoice> createInvoice(@RequestBody Invoice invoice) {
        // The service method performs all the business logic
        Invoice generatedInvoice = billingService.generateFinalBill(invoice);
        return new ResponseEntity<>(generatedInvoice, HttpStatus.CREATED);
    }

    // Endpoint to get an invoice by ID
    // HTTP GET: http://localhost:8080/api/billing/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoice(@PathVariable Long id) {
        // Use repository or a dedicated find service method
        // ... find logic
        // return ResponseEntity.ok(invoice);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND); 
    }
}
