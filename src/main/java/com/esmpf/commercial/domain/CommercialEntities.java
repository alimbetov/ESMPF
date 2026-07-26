package com.esmpf.commercial.domain;

import com.esmpf.shared.persistence.BaseEntity;
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
class Estimate extends BaseEntity {
    @Column(name = "job_id") private UUID jobId;
    @Column(name = "number") private String number;
    @Column(name = "status") private String status;
    @Column(name = "currency") private String currency;
    @Lob @Column(name = "lines_json") private String linesJson;
    @Column(name = "subtotal") private BigDecimal subtotal;
    @Column(name = "discount") private BigDecimal discount;
    @Column(name = "tax") private BigDecimal tax;
    @Column(name = "total") private BigDecimal total;
    @Lob @Column(name = "approval_data_json") private String approvalDataJson;
    @Column(name = "approved_at") private Instant approvedAt;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "invoice", indexes = @Index(name = "idx_invoice_business", columnList = "business_id"))
class Invoice extends BaseEntity {
    @Column(name = "job_id") private UUID jobId;
    @Column(name = "estimate_id") private UUID estimateId;
    @Column(name = "number") private String number;
    @Column(name = "status") private String status;
    @Column(name = "currency") private String currency;
    @Lob @Column(name = "lines_json") private String linesJson;
    @Column(name = "subtotal") private BigDecimal subtotal;
    @Column(name = "tax") private BigDecimal tax;
    @Column(name = "total") private BigDecimal total;
    @Column(name = "paid_amount") private BigDecimal paidAmount;
    @Column(name = "due_date") private LocalDate dueDate;
    @Column(name = "external_accounting_id") private String externalAccountingId;
    @Column(name = "generated_document_id") private UUID generatedDocumentId;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "payment", indexes = @Index(name = "idx_payment_business", columnList = "business_id"))
class Payment extends BaseEntity {
    @Column(name = "invoice_id") private UUID invoiceId;
    @Column(name = "amount") private BigDecimal amount;
    @Column(name = "currency") private String currency;
    @Column(name = "method") private String method;
    @Column(name = "status") private String status;
    @Column(name = "paid_at") private Instant paidAt;
    @Column(name = "external_payment_id") private String externalPaymentId;
    @Lob @Column(name = "details_json") private String detailsJson;
}
