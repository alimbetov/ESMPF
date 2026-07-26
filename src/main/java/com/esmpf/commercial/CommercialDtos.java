package com.esmpf.commercial;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class CommercialDtos {
    private CommercialDtos() {}

    public record EstimateCreateCommand(UUID jobId, String number, String currency, String linesJson,
                                        BigDecimal subtotal, BigDecimal discount, BigDecimal tax,
                                        BigDecimal total) {}
    public record EstimateUpdateCommand(long version, String linesJson, BigDecimal subtotal,
                                        BigDecimal discount, BigDecimal tax, BigDecimal total) {}
    public record EstimateResponse(UUID id, long version, UUID jobId, String number, String status,
                                   String currency, String linesJson, BigDecimal subtotal,
                                   BigDecimal discount, BigDecimal tax, BigDecimal total,
                                   String approvalDataJson, Instant approvedAt,
                                   Instant createdAt, Instant updatedAt) {}

    public record InvoiceCreateCommand(UUID jobId, UUID estimateId, String number, String currency,
                                       String linesJson, BigDecimal subtotal, BigDecimal tax,
                                       BigDecimal total, LocalDate dueDate) {}
    public record InvoiceResponse(UUID id, long version, UUID jobId, UUID estimateId, String number,
                                  String status, String currency, String linesJson,
                                  BigDecimal subtotal, BigDecimal tax, BigDecimal total,
                                  BigDecimal paidAmount, LocalDate dueDate,
                                  String externalAccountingId, UUID generatedDocumentId,
                                  Instant createdAt, Instant updatedAt) {}

    public record PaymentCreateCommand(UUID invoiceId, BigDecimal amount, String currency,
                                       String method, String externalPaymentId, String detailsJson) {}
    public record PaymentResponse(UUID id, long version, UUID invoiceId, BigDecimal amount,
                                  String currency, String method, String status, Instant paidAt,
                                  String externalPaymentId, String detailsJson,
                                  Instant createdAt, Instant updatedAt) {}
}