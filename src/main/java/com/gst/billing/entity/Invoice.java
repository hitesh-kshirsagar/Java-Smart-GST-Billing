package com.gst.billing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a GST Invoice issued to a customer.
 */
@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique auto-generated invoice number, e.g. INV-2024-0001 */
    @Column(name = "invoice_number", nullable = false, unique = true, length = 30)
    private String invoiceNumber;

    /** Date the invoice was issued */
    @Column(nullable = false)
    private LocalDate date;

    /** Customer this invoice belongs to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /** Grand total including all taxes */
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    /** Sum of all CGST on this invoice */
    @Column(name = "total_cgst", precision = 12, scale = 2)
    private BigDecimal totalCgst;

    /** Sum of all SGST on this invoice */
    @Column(name = "total_sgst", precision = 12, scale = 2)
    private BigDecimal totalSgst;

    /** Sum of all IGST on this invoice (inter-state only) */
    @Column(name = "total_igst", precision = 12, scale = 2)
    private BigDecimal totalIgst;

    /** Taxable value (before taxes) */
    @Column(name = "taxable_amount", precision = 12, scale = 2)
    private BigDecimal taxableAmount;

    /** Line items belonging to this invoice */
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InvoiceItem> items = new ArrayList<>();
}
