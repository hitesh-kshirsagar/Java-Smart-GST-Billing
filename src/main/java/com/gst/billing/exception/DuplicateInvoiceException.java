package com.gst.billing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an invoice number already exists in the database.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateInvoiceException extends RuntimeException {

    public DuplicateInvoiceException(String invoiceNumber) {
        super("Invoice already exists with number: " + invoiceNumber);
    }
}
