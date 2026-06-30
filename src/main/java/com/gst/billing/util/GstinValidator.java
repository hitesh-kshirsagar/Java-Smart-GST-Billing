package com.gst.billing.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Validates GSTIN (Goods and Services Tax Identification Number).
 *
 * Format: 2-digit state code + 10-char PAN + 1 entity code + Z + 1 check digit
 * Example: 27AABCU9603R1ZX
 */
@Component
public class GstinValidator {

    /**
     * Regex: 15 characters
     *   [0-9]{2}       — 2-digit state code (01–37)
     *   [A-Z]{5}       — first 5 letters of PAN
     *   [0-9]{4}       — 4 digits of PAN
     *   [A-Z]{1}       — last character of PAN
     *   [1-9A-Z]{1}    — entity number (1–9, then A–Z)
     *   Z              — always 'Z' (reserved)
     *   [0-9A-Z]{1}    — check digit
     */
    private static final Pattern GSTIN_PATTERN =
        Pattern.compile("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$");

    /**
     * Returns true if the provided string is a valid GSTIN.
     *
     * @param gstin the GSTIN string to validate (null-safe)
     */
    public boolean isValid(String gstin) {
        if (gstin == null || gstin.isBlank()) {
            return false;
        }
        return GSTIN_PATTERN.matcher(gstin.toUpperCase().trim()).matches();
    }

    /**
     * Extracts the 2-digit state code from a valid GSTIN.
     * E.g. "27AABCU9603R1ZX" → "27" (Maharashtra)
     */
    public String extractStateCode(String gstin) {
        if (!isValid(gstin)) {
            throw new IllegalArgumentException("Cannot extract state code from invalid GSTIN: " + gstin);
        }
        return gstin.substring(0, 2);
    }
}
