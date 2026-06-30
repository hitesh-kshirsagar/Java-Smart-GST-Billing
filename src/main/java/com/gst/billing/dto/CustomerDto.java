package com.gst.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** DTO for creating/reading a customer */
@Data
public class CustomerDto {

    private Long id;

    @NotBlank(message = "Customer name is required")
    @Size(max = 200)
    private String name;

    /**
     * GSTIN: 15-character alphanumeric string.
     * Format: 2-digit state code + 10-char PAN + 1 entity code + Z + 1 check digit
     * Example: 27AABCU9603R1ZX
     */
    @Pattern(
        regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
        message = "Invalid GSTIN format. Expected format: 27AABCU9603R1ZX"
    )
    private String gstin;

    @NotBlank(message = "State is required")
    @Size(max = 100)
    private String state;
}
