package com.gst.billing.repository;

import com.gst.billing.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    boolean existsByInvoiceNumber(String invoiceNumber);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    /**
     * Fetch all invoices for a given calendar month and year.
     */
    @Query("SELECT i FROM Invoice i WHERE MONTH(i.date) = :month AND YEAR(i.date) = :year ORDER BY i.date DESC")
    List<Invoice> findByMonthAndYear(@Param("month") int month, @Param("year") int year);

    /**
     * Invoices between two dates (for custom range reports).
     */
    List<Invoice> findByDateBetweenOrderByDateDesc(LocalDate from, LocalDate to);

    /**
     * Count of invoices to help generate next invoice number.
     */
    @Query("SELECT COUNT(i) FROM Invoice i WHERE YEAR(i.date) = :year")
    long countByYear(@Param("year") int year);

    /**
     * Fetch a single invoice with its items and product details eagerly loaded.
     * Prevents LazyInitializationException when the Hibernate session closes
     * before invoice items are accessed (e.g. inside PdfGenerationService).
     */
    @Query("SELECT i FROM Invoice i " +
           "LEFT JOIN FETCH i.items item " +
           "LEFT JOIN FETCH item.product " +
           "LEFT JOIN FETCH i.customer " +
           "WHERE i.id = :id")
    Optional<Invoice> findByIdWithItems(@Param("id") Long id);
}
