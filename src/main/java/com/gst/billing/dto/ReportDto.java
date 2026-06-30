package com.gst.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/** Data returned for the monthly / tax summary report */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDto {

    /** Month-Year label, e.g. "January 2024" */
    private String period;

    /** Total number of invoices in this period */
    private long invoiceCount;

    /** Sum of taxable amounts (before tax) */
    private BigDecimal totalTaxableAmount;

    /** Total CGST collected */
    private BigDecimal totalCgst;

    /** Total SGST collected */
    private BigDecimal totalSgst;

    /** Total IGST collected */
    private BigDecimal totalIgst;

    /** Grand total including all taxes */
    private BigDecimal grandTotal;

    /** All invoices included in this period */
    private List<InvoiceResponse> invoices;
}
