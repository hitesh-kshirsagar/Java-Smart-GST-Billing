package com.gst.billing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a provided GSTIN does not match the standard 15-character format.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidGstinException extends RuntimeException {

    public InvalidGstinException(String gstin) {
        super("Invalid GSTIN format: " + gstin + ". Expected: 27AABCU9603R1ZX (15 alphanumeric chars)");
    }
}
