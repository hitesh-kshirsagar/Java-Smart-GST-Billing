package com.gst.billing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * A single line item on an invoice (one product + quantity + GST breakdown).
 */
@Entity
@Table(name = "invoice_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Parent invoice */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    /** Product being billed */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Number of units */
    @Column(nullable = false)
    private Integer quantity;

    /** Unit price at the time of invoicing (snapshot) */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** CGST amount for this line (intra-state) */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cgst;

    /** SGST amount for this line (intra-state) */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal sgst;

    /** IGST amount for this line (inter-state) */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal igst;

    /** Line total = (price × quantity) + cgst + sgst + igst */
    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;
}
