package com.gst.billing;

import com.gst.billing.dto.GstCalculationResult;
import com.gst.billing.service.GstCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the GST Calculation Engine.
 * No Spring context needed — tests the pure calculation logic.
 */
class GstCalculationServiceTest {

    private GstCalculationService service;

    /** Business state: Maharashtra (state code 27) */
    private static final String SELLER_STATE = "Maharashtra";

    @BeforeEach
    void setUp() {
        service = new GstCalculationService();
        // Inject the seller state directly (normally read from application.properties)
        ReflectionTestUtils.setField(service, "sellerState", SELLER_STATE);
    }

    // ================================================================
    // INTRA-STATE tests (buyer = Maharashtra → CGST + SGST)
    // ================================================================

    @Test
    @DisplayName("Intra-state: 18% GST on ₹1000 qty 1 → CGST 90, SGST 90, IGST 0")
    void intraState_18Percent_singleUnit() {
        GstCalculationResult result = service.calculate(
            new BigDecimal("1000.00"), 1, new BigDecimal("18"), "Maharashtra"
        );

        assertTrue(result.isIntraState(), "Should be intra-state");
        assertEquals(new BigDecimal("1000.00"), result.getTaxableAmount());
        assertEquals(new BigDecimal("90.00"), result.getCgst());
        assertEquals(new BigDecimal("90.00"), result.getSgst());
        assertEquals(BigDecimal.ZERO.setScale(2), result.getIgst());
        assertEquals(new BigDecimal("1180.00"), result.getLineTotal());
    }

    @Test
    @DisplayName("Intra-state: 5% GST on ₹200 qty 3 → taxable ₹600, CGST 15, SGST 15")
    void intraState_5Percent_multipleQty() {
        GstCalculationResult result = service.calculate(
            new BigDecimal("200.00"), 3, new BigDecimal("5"), "Maharashtra"
        );

        assertTrue(result.isIntraState());
        assertEquals(new BigDecimal("600.00"), result.getTaxableAmount());
        assertEquals(new BigDecimal("15.00"), result.getCgst());
        assertEquals(new BigDecimal("15.00"), result.getSgst());
        assertEquals(new BigDecimal("630.00"), result.getLineTotal());
    }

    @Test
    @DisplayName("Intra-state: 28% GST on ₹500 qty 2 → taxable ₹1000, total ₹1280")
    void intraState_28Percent() {
        GstCalculationResult result = service.calculate(
            new BigDecimal("500.00"), 2, new BigDecimal("28"), "Maharashtra"
        );

        assertTrue(result.isIntraState());
        assertEquals(new BigDecimal("1000.00"), result.getTaxableAmount());
        assertEquals(new BigDecimal("140.00"), result.getCgst());
        assertEquals(new BigDecimal("140.00"), result.getSgst());
        assertEquals(new BigDecimal("1280.00"), result.getLineTotal());
    }

    @Test
    @DisplayName("Intra-state: 0% GST → no tax applied, line total equals taxable")
    void intraState_0Percent_noTax() {
        GstCalculationResult result = service.calculate(
            new BigDecimal("1500.00"), 1, new BigDecimal("0"), "Maharashtra"
        );

        assertEquals(new BigDecimal("1500.00"), result.getTaxableAmount());
        assertEquals(BigDecimal.ZERO.setScale(2), result.getCgst());
        assertEquals(BigDecimal.ZERO.setScale(2), result.getSgst());
        assertEquals(new BigDecimal("1500.00"), result.getLineTotal());
    }

    // ================================================================
    // INTER-STATE tests (buyer ≠ Maharashtra → IGST only)
    // ================================================================

    @Test
    @DisplayName("Inter-state: 18% GST on ₹1000 qty 1 → IGST 180, CGST/SGST 0")
    void interState_18Percent() {
        GstCalculationResult result = service.calculate(
            new BigDecimal("1000.00"), 1, new BigDecimal("18"), "Karnataka"
        );

        assertFalse(result.isIntraState(), "Should be inter-state");
        assertEquals(new BigDecimal("1000.00"), result.getTaxableAmount());
        assertEquals(BigDecimal.ZERO.setScale(2), result.getCgst());
        assertEquals(BigDecimal.ZERO.setScale(2), result.getSgst());
        assertEquals(new BigDecimal("180.00"), result.getIgst());
        assertEquals(new BigDecimal("1180.00"), result.getLineTotal());
    }

    @Test
    @DisplayName("Inter-state: 12% GST on ₹250 qty 4 → IGST 120, total 1120")
    void interState_12Percent_multipleQty() {
        GstCalculationResult result = service.calculate(
            new BigDecimal("250.00"), 4, new BigDecimal("12"), "Delhi"
        );

        assertFalse(result.isIntraState());
        assertEquals(new BigDecimal("1000.00"), result.getTaxableAmount());
        assertEquals(new BigDecimal("120.00"), result.getIgst());
        assertEquals(new BigDecimal("1120.00"), result.getLineTotal());
    }

    // ================================================================
    // Edge cases
    // ================================================================

    @Test
    @DisplayName("State comparison is case-insensitive")
    void stateComparisonCaseInsensitive() {
        GstCalculationResult lower = service.calculate(
            new BigDecimal("1000"), 1, new BigDecimal("18"), "maharashtra"
        );
        GstCalculationResult upper = service.calculate(
            new BigDecimal("1000"), 1, new BigDecimal("18"), "MAHARASHTRA"
        );

        assertTrue(lower.isIntraState(),  "lowercase should be intra-state");
        assertTrue(upper.isIntraState(),  "uppercase should be intra-state");
    }

    @Test
    @DisplayName("CGST + SGST always equals total GST for intra-state")
    void cgstPlusSgstEqualsGst() {
        GstCalculationResult result = service.calculate(
            new BigDecimal("999.99"), 1, new BigDecimal("18"), "Maharashtra"
        );
        BigDecimal sumTax = result.getCgst().add(result.getSgst());
        BigDecimal lineMinusTaxable = result.getLineTotal().subtract(result.getTaxableAmount());
        assertEquals(sumTax, lineMinusTaxable);
    }
}
