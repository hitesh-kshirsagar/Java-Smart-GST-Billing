package com.gst.billing.controller;

import com.gst.billing.dto.ReportDto;
import com.gst.billing.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for GST reports.
 *
 * GET /api/invoices/report              — current month report
 * GET /api/invoices/report?month=3&year=2024 — specific month
 */
@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    /**
     * Monthly sales and tax summary report.
     * Defaults to current month if no params provided.
     *
     * @param month 1–12 (optional)
     * @param year  e.g. 2024 (optional)
     */
    @GetMapping("/report")
    public ResponseEntity<ReportDto> getReport(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        if (month != null && year != null) {
            return ResponseEntity.ok(reportService.getMonthlySalesReport(month, year));
        }
        return ResponseEntity.ok(reportService.getCurrentMonthReport());
    }
}
