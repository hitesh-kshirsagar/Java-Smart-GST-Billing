package com.gst.billing.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a buyer/customer in the GST billing system.
 */
@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Full name of the customer or business */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * GST Identification Number (15-character alphanumeric).
     * Can be null for unregistered buyers.
     */
    @Column(unique = true, length = 15)
    private String gstin;

    /** Indian state name — used to decide CGST/SGST vs IGST */
    @Column(nullable = false, length = 100)
    private String state;
}
