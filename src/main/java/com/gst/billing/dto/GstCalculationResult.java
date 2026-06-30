package com.gst.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Holds the GST breakdown for a single line item.
 * CGST + SGST are used for intra-state; IGST for inter-state.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GstCalculationResult {

    /** Taxable base = price × quantity */
    private BigDecimal taxableAmount;

    /** Central GST (half of GST rate, intra-state only) */
    private BigDecimal cgst;

    /** State GST (half of GST rate, intra-state only) */
    private BigDecimal sgst;

    /** Integrated GST (full GST rate, inter-state only) */
    private BigDecimal igst;

    /** Line total = taxableAmount + cgst + sgst + igst */
    private BigDecimal lineTotal;

    /** true if intra-state transaction (CGST + SGST applied) */
    private boolean intraState;
}
