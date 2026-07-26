package com.esmpf.commercial.domain;

import com.esmpf.shared.persistence.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "estimate", indexes = @Index(name = "idx_estimate_business", columnList = "business_id"))
class Estimate extends TenantEntity {
    @Column(name = "job_id", nullable = false) private UUID jobId;
    @Column(name = "number", nullable = false, length = 100) private String number;
    @Column(name = "status", nullable = false, length = 40) private String status;
    @Column(name = "currency", nullable = false, length = 3) private String currency;
    @Lob @Column(name = "lines_json", nullable = false) private String linesJson;
    @Column(name = "subtotal", nullable = false, precision = 19, scale = 4) private BigDecimal subtotal;
    @Column(name = "discount", nullable = false, precision = 19, scale = 4) private BigDecimal discount;
    @Column(name = "tax", nullable = false, precision = 19, scale = 4) private BigDecimal tax;
    @Column(name = "total", nullable = false, precision = 19, scale = 4) private BigDecimal total;
    @Lob @Column(name = "approval_data_json") private String approvalDataJson;
    @Column(name = "approved_at") private Instant approvedAt;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "invoice", indexes = @Index(name = "idx_invoice_business", columnList = "business_id"))
class Invoice extends TenantEntity {
    @Column(name = "job_id", nullable = false) private UUID jobId;
    @Column(name = "estimate_id") private UUID estimateId;
    @Column(name = "number", nullable = false, length = 100) private String number;
    @Column(name = "status", nullable = false, length = 40) private String status;
    @Column(name = "currency", nullable = false, length = 3) private String currency;
    @Lob @Column(name = "lines_json", nullable = false) private String linesJson;
    @Column(name = "subtotal", nullable = false, precision = 19, scale = 4) private BigDecimal subtotal;
    @Column(name = "tax", nullable = false, precision = 19, scale = 4) private BigDecimal tax;
    @Column(name = "total", nullable = false, precision = 19, scale = 4) private BigDecimal total;
    @Column(name = "paid_amount", nullable = false, precision = 19, scale = 4) private BigDecimal paidAmount;
    @Column(name = "due_date") private LocalDate dueDate;
    @Column(name = "external_accounting_id", length = 200) private String externalAccountingId;
    @Column(name = "generated_document_id") private UUID generatedDocumentId;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "payment", indexes = @Index(name = "idx_payment_business", columnList = "business_id"))
class Payment extends TenantEntity {
    @Column(name = "invoice_id", nullable = false) private UUID invoiceId;
    @Column(name = "amount", nullable = false, precision = 19, scale = 4) private BigDecimal amount;
    @Column(name = "currency", nullable = false, length = 3) private String currency;
    @Column(name = "method", nullable = false, length = 60) private String method;
    @Column(name = "status", nullable = false, length = 40) private String status;
    @Column(name = "paid_at") private Instant paidAt;
    @Column(name = "external_payment_id", length = 200) private String externalPaymentId;
    @Lob @Column(name = "details_json") private String detailsJson;
}
