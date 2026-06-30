package com.gst.billing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents a product/service that can be added to an invoice.
 */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Product or service name */
    @Column(nullable = false, length = 200)
    private String name;

    /** Base price (before GST) */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** GST rate in percent, e.g. 18.0 for 18% */
    @Column(name = "gst_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal gstPercentage;

    /** Harmonized System of Nomenclature code */
    @Column(name = "hsn_code", nullable = false, length = 20)
    private String hsnCode;
}
