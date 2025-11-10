// src/main/java/com/app/model/Invoice.java

package com.app.model;

import jakarta.persistence.*; // Use 'javax.persistence.*' for older Spring Boot/JPA

@Entity
@Table(name = "invoices")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Use Foreign Key to Patient (assuming Patient entity exists)
    @ManyToOne 
    @JoinColumn(name = "patient_id")
    private Patient patient; 

    private double totalAmount;
    private double insuranceDiscount;
    private String paymentStatus; // e.g., PENDING, PAID

    // Getters and Setters (omitted for brevity)
}
