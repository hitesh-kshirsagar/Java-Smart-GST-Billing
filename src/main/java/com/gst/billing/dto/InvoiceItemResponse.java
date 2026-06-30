package com.gst.billing.dto;

import lombok.Data;

import java.math.BigDecimal;

/** Response DTO for a single invoice line item */
@Data
public class InvoiceItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String hsnCode;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;
    private BigDecimal lineTotal;
}
