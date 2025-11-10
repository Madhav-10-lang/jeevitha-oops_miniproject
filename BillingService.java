// src/main/java/com/app/service/BillingService.java

package com.app.service;

import com.app.model.Invoice;
import com.app.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

@Service
public class BillingService {

    private final InvoiceRepository invoiceRepository;

    public BillingService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    // Core Business Logic
    public Invoice generateFinalBill(Invoice newInvoice) {
        // 1. Calculate base cost (e.g., sum of all services + medicines)
        double baseCost = calculateBaseCost(newInvoice);

        // 2. Apply insurance logic
        double discount = applyInsurancePolicy(newInvoice.getPatient());

        // 3. Final calculation
        double finalAmount = baseCost - discount;
        
        newInvoice.setTotalAmount(finalAmount);
        newInvoice.setPaymentStatus("PENDING");
        
        // 4. Save to database
        return invoiceRepository.save(newInvoice);
    }
    
    // (Private helper methods for calculation and insurance logic...)
    private double calculateBaseCost(Invoice invoice) { /* ... logic ... */ return 0.0; }
    private double applyInsurancePolicy(Patient patient) { /* ... logic ... */ return 0.0; }
}
