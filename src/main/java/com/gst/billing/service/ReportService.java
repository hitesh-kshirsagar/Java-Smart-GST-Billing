package com.gst.billing.service;

import com.gst.billing.dto.InvoiceItemResponse;
import com.gst.billing.dto.InvoiceResponse;
import com.gst.billing.dto.ReportDto;
import com.gst.billing.entity.Invoice;
import com.gst.billing.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/**
 * Generates monthly sales and tax summary reports.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final InvoiceRepository invoiceRepository;

    /**
     * Return a full report for a given month and year.
     *
     * @param month 1-based month number (1 = January)
     * @param year  4-digit year
     */
    @Transactional(readOnly = true)
    public ReportDto getMonthlySalesReport(int month, int year) {
        List<Invoice> invoices = invoiceRepository.findByMonthAndYear(month, year);

        BigDecimal totalTaxable = BigDecimal.ZERO;
        BigDecimal totalCgst    = BigDecimal.ZERO;
        BigDecimal totalSgst    = BigDecimal.ZERO;
        BigDecimal totalIgst    = BigDecimal.ZERO;
        BigDecimal grandTotal   = BigDecimal.ZERO;

        for (Invoice inv : invoices) {
            totalTaxable = totalTaxable.add(inv.getTaxableAmount());
            totalCgst    = totalCgst.add(inv.getTotalCgst());
            totalSgst    = totalSgst.add(inv.getTotalSgst());
            totalIgst    = totalIgst.add(inv.getTotalIgst());
            grandTotal   = grandTotal.add(inv.getTotalAmount());
        }

        String period = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year;

        List<InvoiceResponse> invoiceResponses = invoices.stream()
            .map(this::toResponse)
            .toList();

        return ReportDto.builder()
            .period(period)
            .invoiceCount(invoices.size())
            .totalTaxableAmount(totalTaxable)
            .totalCgst(totalCgst)
            .totalSgst(totalSgst)
            .totalIgst(totalIgst)
            .grandTotal(grandTotal)
            .invoices(invoiceResponses)
            .build();
    }

    /**
     * Report for the current month.
     */
    @Transactional(readOnly = true)
    public ReportDto getCurrentMonthReport() {
        LocalDate now = LocalDate.now();
        return getMonthlySalesReport(now.getMonthValue(), now.getYear());
    }

    // ---- Mapping helper ----

    private InvoiceResponse toResponse(Invoice inv) {
        InvoiceResponse r = new InvoiceResponse();
        r.setId(inv.getId());
        r.setInvoiceNumber(inv.getInvoiceNumber());
        r.setDate(inv.getDate());
        r.setCustomerId(inv.getCustomer().getId());
        r.setCustomerName(inv.getCustomer().getName());
        r.setCustomerGstin(inv.getCustomer().getGstin());
        r.setCustomerState(inv.getCustomer().getState());
        r.setTaxableAmount(inv.getTaxableAmount());
        r.setTotalCgst(inv.getTotalCgst());
        r.setTotalSgst(inv.getTotalSgst());
        r.setTotalIgst(inv.getTotalIgst());
        r.setTotalAmount(inv.getTotalAmount());

        List<InvoiceItemResponse> items = inv.getItems().stream().map(item -> {
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
        r.setItems(items);
        return r;
    }
}
