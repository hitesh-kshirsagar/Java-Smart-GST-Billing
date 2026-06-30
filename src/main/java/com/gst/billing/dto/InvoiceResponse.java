package com.gst.billing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Full invoice response sent to the client */
@Data
public class InvoiceResponse {
    private Long id;
    private String invoiceNumber;
    private LocalDate date;
    private Long customerId;
    private String customerName;
    private String customerGstin;
    private String customerState;
    private BigDecimal taxableAmount;
    private BigDecimal totalCgst;
    private BigDecimal totalSgst;
    private BigDecimal totalIgst;
    private BigDecimal totalAmount;
    private List<InvoiceItemResponse> items;
}
