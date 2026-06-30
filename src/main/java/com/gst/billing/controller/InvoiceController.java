package com.gst.billing.controller;

import com.gst.billing.dto.InvoiceRequest;
import com.gst.billing.dto.InvoiceResponse;
import com.gst.billing.service.InvoiceService;
import com.gst.billing.service.PdfGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for invoice management and PDF export.
 *
 * POST /api/invoices/create    — create invoice (calculates GST automatically)
 * GET  /api/invoices           — list all invoices
 * GET  /api/invoices/{id}      — fetch one invoice
 * GET  /api/invoices/{id}/pdf  — download PDF
 */
@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PdfGenerationService pdfService;

    @PostMapping("/create")
    public ResponseEntity<InvoiceResponse> create(@Valid @RequestBody InvoiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.createInvoice(request));
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> getAll() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    /**
     * Stream a PDF invoice to the browser.
     * The response header triggers a download: "invoice-INV-2024-0001.pdf"
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) throws Exception {
        byte[] pdf = pdfService.generateInvoicePdf(id);
        InvoiceResponse invoice = invoiceService.getInvoiceById(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData(
            "attachment", "invoice-" + invoice.getInvoiceNumber() + ".pdf"
        );

        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
