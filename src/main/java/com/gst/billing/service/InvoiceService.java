package com.gst.billing.service;

import com.gst.billing.dto.*;
import com.gst.billing.entity.*;
import com.gst.billing.exception.DuplicateInvoiceException;
import com.gst.billing.exception.ResourceNotFoundException;
import com.gst.billing.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles invoice creation with auto-numbering and GST calculation per line item.
 */
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerService customerService;
    private final ProductService productService;
    private final GstCalculationService gstCalculationService;

    /**
     * Create a new invoice.
     * Auto-generates an invoice number in the format: INV-YYYY-NNNN
     */
    @Transactional
    public InvoiceResponse createInvoice(InvoiceRequest request) {
        Customer customer = customerService.findOrThrow(request.getCustomerId());

        // Build unique invoice number
        int year = LocalDate.now().getYear();
        long count = invoiceRepository.countByYear(year);
        String invoiceNumber = String.format("INV-%d-%04d", year, count + 1);

        // Guard against duplicate (race condition safety)
        if (invoiceRepository.existsByInvoiceNumber(invoiceNumber)) {
            throw new DuplicateInvoiceException(invoiceNumber);
        }

        Invoice invoice = Invoice.builder()
            .invoiceNumber(invoiceNumber)
            .date(LocalDate.now())
            .customer(customer)
            .totalAmount(BigDecimal.ZERO)
            .totalCgst(BigDecimal.ZERO)
            .totalSgst(BigDecimal.ZERO)
            .totalIgst(BigDecimal.ZERO)
            .taxableAmount(BigDecimal.ZERO)
            .items(new ArrayList<>())
            .build();

        BigDecimal totalTaxable = BigDecimal.ZERO;
        BigDecimal totalCgst   = BigDecimal.ZERO;
        BigDecimal totalSgst   = BigDecimal.ZERO;
        BigDecimal totalIgst   = BigDecimal.ZERO;
        BigDecimal grandTotal  = BigDecimal.ZERO;

        // Process each line item
        for (InvoiceItemRequest itemReq : request.getItems()) {
            Product product = productService.findOrThrow(itemReq.getProductId());

            GstCalculationResult gst = gstCalculationService.calculate(
                product.getPrice(),
                itemReq.getQuantity(),
                product.getGstPercentage(),
                customer.getState()
            );

            InvoiceItem item = InvoiceItem.builder()
                .invoice(invoice)
                .product(product)
                .quantity(itemReq.getQuantity())
                .price(product.getPrice())
                .cgst(gst.getCgst())
                .sgst(gst.getSgst())
                .igst(gst.getIgst())
                .lineTotal(gst.getLineTotal())
                .build();

            invoice.getItems().add(item);

            totalTaxable = totalTaxable.add(gst.getTaxableAmount());
            totalCgst    = totalCgst.add(gst.getCgst());
            totalSgst    = totalSgst.add(gst.getSgst());
            totalIgst    = totalIgst.add(gst.getIgst());
            grandTotal   = grandTotal.add(gst.getLineTotal());
        }

        invoice.setTaxableAmount(totalTaxable);
        invoice.setTotalCgst(totalCgst);
        invoice.setTotalSgst(totalSgst);
        invoice.setTotalIgst(totalIgst);
        invoice.setTotalAmount(grandTotal);

        Invoice saved = invoiceRepository.save(invoice);
        return toResponse(saved);
    }

    /** Fetch a single invoice by ID */
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));
        return toResponse(invoice);
    }

    /** Fetch all invoices */
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    /**
     * Fetch invoice entity with items and products eagerly loaded.
     * Uses JOIN FETCH to avoid LazyInitializationException in PdfGenerationService,
     * which accesses invoice.getItems() outside of a Hibernate session.
     */
    @Transactional(readOnly = true)
    public Invoice getInvoiceEntity(Long id) {
        return invoiceRepository.findByIdWithItems(id)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));
    }

    // ---- Mapping ----

    private InvoiceResponse toResponse(Invoice inv) {
        InvoiceResponse resp = new InvoiceResponse();
        resp.setId(inv.getId());
        resp.setInvoiceNumber(inv.getInvoiceNumber());
        resp.setDate(inv.getDate());
        resp.setCustomerId(inv.getCustomer().getId());
        resp.setCustomerName(inv.getCustomer().getName());
        resp.setCustomerGstin(inv.getCustomer().getGstin());
        resp.setCustomerState(inv.getCustomer().getState());
        resp.setTaxableAmount(inv.getTaxableAmount());
        resp.setTotalCgst(inv.getTotalCgst());
        resp.setTotalSgst(inv.getTotalSgst());
        resp.setTotalIgst(inv.getTotalIgst());
        resp.setTotalAmount(inv.getTotalAmount());

        List<InvoiceItemResponse> itemResponses = inv.getItems().stream().map(item -> {
            InvoiceItemResponse ir = new InvoiceItemResponse();
            ir.setId(item.getId());
            ir.setProductId(item.getProduct().getId());
            ir.setProductName(item.getProduct().getName());
            ir.setHsnCode(item.getProduct().getHsnCode());
            ir.setQuantity(item.getQuantity());
            ir.setPrice(item.getPrice());
            ir.setCgst(item.getCgst());
            ir.setSgst(item.getSgst());
            ir.setIgst(item.getIgst());
            ir.setLineTotal(item.getLineTotal());
            return ir;
        }).toList();

        resp.setItems(itemResponses);
        return resp;
    }
}
