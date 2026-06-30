package com.gst.billing.service;

import com.gst.billing.dto.GstCalculationResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Core GST calculation engine.
 *
 * Rules:
 *  - Intra-state (seller state == buyer state): CGST = GST/2, SGST = GST/2
 *  - Inter-state (seller state != buyer state): IGST = full GST
 *
 * All amounts are rounded to 2 decimal places (HALF_UP).
 */
@Service
public class GstCalculationService {

    /** Seller's registered state (configured in application.properties) */
    @Value("${app.business.state}")
    private String sellerState;

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    /**
     * Calculate GST for a single line item.
     *
     * @param unitPrice     Price per unit (before tax)
     * @param quantity      Number of units
     * @param gstPercentage GST rate, e.g. 18.0 for 18%
     * @param buyerState    State of the customer/buyer
     * @return              Breakdown of CGST, SGST, IGST, and totals
     */
    public GstCalculationResult calculate(BigDecimal unitPrice,
                                          int quantity,
                                          BigDecimal gstPercentage,
                                          String buyerState) {

        // Taxable base = unit price × quantity
        BigDecimal taxableAmount = unitPrice
            .multiply(BigDecimal.valueOf(quantity))
            .setScale(SCALE, ROUNDING);

        // Total GST amount = taxable × (gstRate / 100)
        BigDecimal totalGst = taxableAmount
            .multiply(gstPercentage)
            .divide(BigDecimal.valueOf(100), SCALE, ROUNDING);

        boolean intraState = sellerState.equalsIgnoreCase(buyerState.trim());

        BigDecimal cgst, sgst, igst;

        if (intraState) {
            // Split GST equally between Centre and State
            cgst = totalGst.divide(BigDecimal.valueOf(2), SCALE, ROUNDING);
            sgst = totalGst.subtract(cgst); // Avoids rounding penny loss
            igst = BigDecimal.ZERO;
        } else {
            // Entire GST goes as Integrated GST
            cgst = BigDecimal.ZERO;
            sgst = BigDecimal.ZERO;
            igst = totalGst;
        }

        BigDecimal lineTotal = taxableAmount.add(cgst).add(sgst).add(igst);

        return GstCalculationResult.builder()
            .taxableAmount(taxableAmount)
            .cgst(cgst)
            .sgst(sgst)
            .igst(igst)
            .lineTotal(lineTotal)
            .intraState(intraState)
            .build();
    }
}
