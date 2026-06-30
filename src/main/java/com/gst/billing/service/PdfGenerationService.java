package com.gst.billing.service;

import com.gst.billing.entity.Invoice;
import com.gst.billing.entity.InvoiceItem;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Generates a professional PDF invoice using iText 5.
 * Layout: header, business/customer block, product table, GST summary, footer.
 */
@Service
@RequiredArgsConstructor
public class PdfGenerationService {

    private final InvoiceService invoiceService;

    @Value("${app.business.name}")    private String businessName;
    @Value("${app.business.gstin}")   private String businessGstin;
    @Value("${app.business.address}") private String businessAddress;
    @Value("${app.business.phone}")   private String businessPhone;
    @Value("${app.business.email}")   private String businessEmail;
    @Value("${app.business.state}")   private String businessState;

    private static final Font TITLE_FONT  = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD,  new BaseColor(33, 37, 41));
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD,  BaseColor.WHITE);
    private static final Font LABEL_FONT  = new Font(Font.FontFamily.HELVETICA, 9,  Font.BOLD,  new BaseColor(73, 80, 87));
    private static final Font VALUE_FONT  = new Font(Font.FontFamily.HELVETICA, 9,  Font.NORMAL, new BaseColor(33, 37, 41));
    private static final Font SMALL_FONT  = new Font(Font.FontFamily.HELVETICA, 8,  Font.NORMAL, new BaseColor(108, 117, 125));
    private static final BaseColor ACCENT = new BaseColor(13, 110, 253);
    private static final BaseColor ROW_ALT = new BaseColor(248, 249, 250);

    /**
     * Generate a PDF for the given invoice ID and return the raw bytes.
     */
    public byte[] generateInvoicePdf(Long invoiceId) throws DocumentException {
        Invoice invoice = invoiceService.getInvoiceEntity(invoiceId);

        Document doc = new Document(PageSize.A4, 40, 40, 50, 50);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, out);
        doc.open();

        addHeader(doc, invoice);
        addParties(doc, invoice);
        addItemsTable(doc, invoice);
        addTaxSummary(doc, invoice);
        addFooter(doc);

        doc.close();
        return out.toByteArray();
    }

    // ---- Section builders ----

    private void addHeader(Document doc, Invoice invoice) throws DocumentException {
        // Blue banner
        PdfPTable banner = new PdfPTable(2);
        banner.setWidthPercentage(100);
        banner.setWidths(new float[]{60, 40});

        PdfPCell titleCell = new PdfPCell();
        titleCell.setBackgroundColor(ACCENT);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setPadding(12);
        Paragraph title = new Paragraph("TAX INVOICE", new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, BaseColor.WHITE));
        titleCell.addElement(title);
        banner.addCell(titleCell);

        PdfPCell infoCell = new PdfPCell();
        infoCell.setBackgroundColor(ACCENT);
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setPadding(12);
        infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph invNo = new Paragraph("Invoice No: " + invoice.getInvoiceNumber(),
            new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE));
        invNo.setAlignment(Element.ALIGN_RIGHT);
        Paragraph invDate = new Paragraph("Date: " + invoice.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
            new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.WHITE));
        invDate.setAlignment(Element.ALIGN_RIGHT);
        infoCell.addElement(invNo);
        infoCell.addElement(invDate);
        banner.addCell(infoCell);

        doc.add(banner);
        doc.add(Chunk.NEWLINE);
    }

    private void addParties(Document doc, Invoice invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);
        table.setSpacingAfter(8);

        // Seller
        PdfPCell sellerCell = new PdfPCell();
        sellerCell.setBorder(Rectangle.BOX);
        sellerCell.setBorderColor(new BaseColor(206, 212, 218));
        sellerCell.setPadding(10);
        sellerCell.addElement(new Paragraph("SELLER / SUPPLIER", LABEL_FONT));
        sellerCell.addElement(new Paragraph(businessName, TITLE_FONT));
        sellerCell.addElement(new Paragraph("GSTIN: " + businessGstin, VALUE_FONT));
        sellerCell.addElement(new Paragraph(businessAddress, VALUE_FONT));
        sellerCell.addElement(new Paragraph("Ph: " + businessPhone + "  |  " + businessEmail, SMALL_FONT));
        table.addCell(sellerCell);

        // Buyer
        PdfPCell buyerCell = new PdfPCell();
        buyerCell.setBorder(Rectangle.BOX);
        buyerCell.setBorderColor(new BaseColor(206, 212, 218));
        buyerCell.setPadding(10);
        buyerCell.addElement(new Paragraph("BILL TO / BUYER", LABEL_FONT));
        buyerCell.addElement(new Paragraph(invoice.getCustomer().getName(),
            new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, new BaseColor(33, 37, 41))));
        String gstin = invoice.getCustomer().getGstin() != null ? invoice.getCustomer().getGstin() : "Unregistered";
        buyerCell.addElement(new Paragraph("GSTIN: " + gstin, VALUE_FONT));
        buyerCell.addElement(new Paragraph("State: " + invoice.getCustomer().getState(), VALUE_FONT));
        table.addCell(buyerCell);

        doc.add(table);
    }

    private void addItemsTable(Document doc, Invoice invoice) throws DocumentException {
        boolean intraState = businessState.equalsIgnoreCase(invoice.getCustomer().getState());

        // Column headers depend on transaction type
        PdfPTable table;
        if (intraState) {
            table = new PdfPTable(new float[]{5, 25, 10, 10, 10, 10, 10, 10, 10});
        } else {
            table = new PdfPTable(new float[]{5, 25, 10, 10, 10, 15, 10, 15});
        }
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);

        String[] headers = intraState
            ? new String[]{"#", "Product / HSN", "Qty", "Rate", "Taxable", "CGST", "SGST", "Total"}
            : new String[]{"#", "Product / HSN", "Qty", "Rate", "Taxable", "IGST", "Total"};

        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, HEADER_FONT));
            cell.setBackgroundColor(new BaseColor(52, 58, 64));
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPadding(7);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        boolean alt = false;
        int idx = 1;
        for (InvoiceItem item : invoice.getItems()) {
            BaseColor bg = alt ? ROW_ALT : BaseColor.WHITE;
            alt = !alt;

            addCell(table, String.valueOf(idx++), bg, Element.ALIGN_CENTER);
            addCell(table, item.getProduct().getName() + "\nHSN: " + item.getProduct().getHsnCode(), bg, Element.ALIGN_LEFT);
            addCell(table, String.valueOf(item.getQuantity()), bg, Element.ALIGN_CENTER);
            addCell(table, fmt(item.getPrice()), bg, Element.ALIGN_RIGHT);
            addCell(table, fmt(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))), bg, Element.ALIGN_RIGHT);

            if (intraState) {
                addCell(table, fmt(item.getCgst()), bg, Element.ALIGN_RIGHT);
                addCell(table, fmt(item.getSgst()), bg, Element.ALIGN_RIGHT);
                addCell(table, fmt(item.getLineTotal()), bg, Element.ALIGN_RIGHT);
            } else {
                addCell(table, fmt(item.getIgst()), bg, Element.ALIGN_RIGHT);
                addCell(table, fmt(item.getLineTotal()), bg, Element.ALIGN_RIGHT);
            }
        }

        doc.add(table);
    }

    private void addTaxSummary(Document doc, Invoice invoice) throws DocumentException {
        doc.add(Chunk.NEWLINE);

        PdfPTable summary = new PdfPTable(2);
        summary.setWidthPercentage(45);
        summary.setHorizontalAlignment(Element.ALIGN_RIGHT);

        addSummaryRow(summary, "Taxable Amount", fmt(invoice.getTaxableAmount()), false);

        if (invoice.getTotalCgst().compareTo(BigDecimal.ZERO) > 0) {
            addSummaryRow(summary, "CGST", fmt(invoice.getTotalCgst()), false);
            addSummaryRow(summary, "SGST", fmt(invoice.getTotalSgst()), false);
        }
        if (invoice.getTotalIgst().compareTo(BigDecimal.ZERO) > 0) {
            addSummaryRow(summary, "IGST", fmt(invoice.getTotalIgst()), false);
        }
        addSummaryRow(summary, "GRAND TOTAL", "\u20B9 " + fmt(invoice.getTotalAmount()), true);

        doc.add(summary);

        // Transaction type note
        boolean intra = businessState.equalsIgnoreCase(invoice.getCustomer().getState());
        Paragraph note = new Paragraph("* " + (intra ? "Intra-state supply: CGST + SGST applied" : "Inter-state supply: IGST applied"), SMALL_FONT);
        note.setAlignment(Element.ALIGN_RIGHT);
        note.setSpacingBefore(6);
        doc.add(note);
    }

    private void addFooter(Document doc) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        Paragraph footer = new Paragraph(
            "This is a computer-generated invoice and does not require a signature.\nThank you for your business!",
            SMALL_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);
    }

    // ---- Helpers ----

    private void addCell(PdfPTable table, String text, BaseColor bg, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, VALUE_FONT));
        cell.setBackgroundColor(bg);
        cell.setBorderColor(new BaseColor(222, 226, 230));
        cell.setPadding(6);
        cell.setHorizontalAlignment(alignment);
        table.addCell(cell);
    }

    private void addSummaryRow(PdfPTable table, String label, String value, boolean highlight) {
        Font f = highlight
            ? new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE)
            : VALUE_FONT;
        BaseColor bg = highlight ? ACCENT : BaseColor.WHITE;

        PdfPCell lc = new PdfPCell(new Phrase(label, highlight ? f : LABEL_FONT));
        lc.setBorderColor(new BaseColor(222, 226, 230));
        lc.setPadding(6);
        lc.setBackgroundColor(bg);
        table.addCell(lc);

        PdfPCell vc = new PdfPCell(new Phrase(value, f));
        vc.setBorderColor(new BaseColor(222, 226, 230));
        vc.setPadding(6);
        vc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        vc.setBackgroundColor(bg);
        table.addCell(vc);
    }

    private String fmt(BigDecimal val) {
        return val == null ? "0.00" : String.format("%.2f", val);
    }
}
