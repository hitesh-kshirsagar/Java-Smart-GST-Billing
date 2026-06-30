package com.gst.billing.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/** DTO for creating/reading a product */
@Data
public class ProductDto {

    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(max = 200)
    private String name;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "GST percentage is required")
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private BigDecimal gstPercentage;

    @NotBlank(message = "HSN code is required")
    @Size(max = 20)
    private String hsnCode;
}
