package com.esmpf.commercial.domain;

import static com.esmpf.commercial.CommercialDtos.*;

import com.esmpf.commercial.CommercialService;
import com.esmpf.service.ServiceReferenceQuery;
import com.esmpf.shared.exception.EntityNotFoundException;
import com.esmpf.shared.exception.StaleEntityException;
import com.esmpf.shared.tenant.TenantContext;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class CommercialServiceImpl implements CommercialService {
    private final TenantContext tenantContext;
    private final ServiceReferenceQuery serviceReferences;
    private final EstimateRepository estimateRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final CommercialMapper mapper;

    @Override @Transactional
    public EstimateResponse createEstimate(EstimateCreateCommand command) {
        serviceReferences.requireJob(command.jobId());
        validateMoney(command.currency(), command.subtotal(), command.discount(), command.tax(), command.total());
        if (estimateRepository.existsByBusinessIdAndNumberIgnoreCase(tenant(), command.number())) throw new IllegalArgumentException("Estimate number already exists");
        Estimate entity = mapper.toEntity(command); entity.setBusinessId(tenant());
        return mapper.toResponse(estimateRepository.saveAndFlush(entity));
    }

    @Override @Transactional(readOnly = true) public EstimateResponse getEstimate(UUID id) { return mapper.toResponse(requireEstimate(id)); }
    @Override @Transactional(readOnly = true) public Page<EstimateResponse> listEstimates(Pageable pageable) { return estimateRepository.findAllByBusinessId(tenant(), pageable).map(mapper::toResponse); }

    @Override @Transactional
    public EstimateResponse updateDraftEstimate(UUID id, EstimateUpdateCommand command) {
        Estimate entity = requireEstimate(id); checkVersion("Estimate", id, command.version(), entity.getVersion()); requireStatus(entity.getStatus(), "DRAFT");
        String currency = entity.getCurrency(); validateMoney(currency, value(command.subtotal(), entity.getSubtotal()), value(command.discount(), entity.getDiscount()), value(command.tax(), entity.getTax()), value(command.total(), entity.getTotal()));
        mapper.update(command, entity); return mapper.toResponse(estimateRepository.saveAndFlush(entity));
    }

    @Override @Transactional public EstimateResponse sendEstimate(UUID id, long version) { return transitionEstimate(id, version, "DRAFT", "SENT", null); }
    @Override @Transactional public EstimateResponse approveEstimate(UUID id, long version, String approval) { return transitionEstimate(id, version, "SENT", "APPROVED", approval); }
    @Override @Transactional public EstimateResponse rejectEstimate(UUID id, long version, String approval) { return transitionEstimate(id, version, "SENT", "REJECTED", approval); }

    @Override @Transactional
    public InvoiceResponse createInvoice(InvoiceCreateCommand command) {
        serviceReferences.requireJob(command.jobId());
        if (command.estimateId() != null) {
            Estimate estimate = requireEstimate(command.estimateId());
            if (!estimate.getJobId().equals(command.jobId())) throw new IllegalArgumentException("Estimate belongs to another job");
            if (!"APPROVED".equals(estimate.getStatus())) throw new IllegalStateException("Invoice requires an APPROVED estimate");
            if (invoiceRepository.existsByBusinessIdAndEstimateId(tenant(), estimate.getId())) throw new IllegalArgumentException("Estimate already converted to invoice");
        }
        validateInvoiceMoney(command.currency(), command.subtotal(), command.tax(), command.total());
        if (invoiceRepository.existsByBusinessIdAndNumberIgnoreCase(tenant(), command.number())) throw new IllegalArgumentException("Invoice number already exists");
        Invoice entity = mapper.toEntity(command); entity.setBusinessId(tenant());
        return mapper.toResponse(invoiceRepository.saveAndFlush(entity));
    }

    @Override @Transactional
    public InvoiceResponse createInvoiceFromEstimate(UUID estimateId, long estimateVersion, String invoiceNumber, LocalDate dueDate) {
        Estimate estimate = requireEstimate(estimateId); checkVersion("Estimate", estimateId, estimateVersion, estimate.getVersion()); requireStatus(estimate.getStatus(), "APPROVED");
        return createInvoice(new InvoiceCreateCommand(estimate.getJobId(), estimate.getId(), invoiceNumber, estimate.getCurrency(), estimate.getLinesJson(), estimate.getSubtotal(), estimate.getTax(), estimate.getTotal(), dueDate));
    }

    @Override @Transactional(readOnly = true) public InvoiceResponse getInvoice(UUID id) { return mapper.toResponse(requireInvoice(id)); }
    @Override @Transactional(readOnly = true) public Page<InvoiceResponse> listInvoices(Pageable pageable) { return invoiceRepository.findAllByBusinessId(tenant(), pageable).map(mapper::toResponse); }
    @Override @Transactional public InvoiceResponse issueInvoice(UUID id, long version) { return transitionInvoice(id, version, "DRAFT", "ISSUED"); }

    @Override @Transactional
    public InvoiceResponse markInvoiceOverdue(UUID id, long version) {
        Invoice entity = requireInvoice(id); checkVersion("Invoice", id, version, entity.getVersion());
        if (!("ISSUED".equals(entity.getStatus()) || "PARTIALLY_PAID".equals(entity.getStatus()))) throw new IllegalStateException("Only issued invoices can become overdue");
        if (entity.getDueDate() == null || !entity.getDueDate().isBefore(LocalDate.now())) throw new IllegalStateException("Invoice due date has not passed");
        entity.setStatus("OVERDUE"); return mapper.toResponse(invoiceRepository.saveAndFlush(entity));
    }

    @Override @Transactional
    public InvoiceResponse voidInvoice(UUID id, long version) {
        Invoice entity = requireInvoice(id); checkVersion("Invoice", id, version, entity.getVersion());
        if (entity.getPaidAmount().signum() > 0) throw new IllegalStateException("Paid invoice cannot be voided");
        if ("PAID".equals(entity.getStatus()) || "VOID".equals(entity.getStatus())) throw new IllegalStateException("Terminal invoice cannot be voided");
        entity.setStatus("VOID"); return mapper.toResponse(invoiceRepository.saveAndFlush(entity));
    }

    @Override @Transactional
    public InvoiceResponse attachGeneratedDocument(UUID id, long version, UUID documentId) {
        Invoice entity = requireInvoice(id); checkVersion("Invoice", id, version, entity.getVersion()); entity.setGeneratedDocumentId(documentId);
        return mapper.toResponse(invoiceRepository.saveAndFlush(entity));
    }

    @Override @Transactional
    public PaymentResponse registerPayment(PaymentCreateCommand command) {
        Invoice invoice = requireInvoice(command.invoiceId());
        if (!("ISSUED".equals(invoice.getStatus()) || "PARTIALLY_PAID".equals(invoice.getStatus()) || "OVERDUE".equals(invoice.getStatus()))) throw new IllegalStateException("Invoice is not payable");
        if (command.amount() == null || command.amount().signum() <= 0) throw new IllegalArgumentException("Payment amount must be positive");
        if (!invoice.getCurrency().equals(command.currency())) throw new IllegalArgumentException("Payment currency must match invoice currency");
        if (command.externalPaymentId() != null && paymentRepository.existsByBusinessIdAndExternalPaymentId(tenant(), command.externalPaymentId())) throw new IllegalArgumentException("External payment already registered");
        if (invoice.getPaidAmount().add(command.amount()).compareTo(invoice.getTotal()) > 0) throw new IllegalArgumentException("Payment exceeds invoice outstanding amount");
        Payment entity = mapper.toEntity(command); entity.setBusinessId(tenant());
        return mapper.toResponse(paymentRepository.saveAndFlush(entity));
    }

    @Override @Transactional
    public PaymentResponse confirmPayment(UUID id, long version) {
        Payment payment = requirePayment(id); checkVersion("Payment", id, version, payment.getVersion()); requireStatus(payment.getStatus(), "PENDING");
        Invoice invoice = requireInvoice(payment.getInvoiceId()); payment.setStatus("CONFIRMED"); payment.setPaidAt(Instant.now());
        invoice.setPaidAmount(invoice.getPaidAmount().add(payment.getAmount()));
        invoice.setStatus(invoice.getPaidAmount().compareTo(invoice.getTotal()) >= 0 ? "PAID" : "PARTIALLY_PAID");
        invoiceRepository.saveAndFlush(invoice); return mapper.toResponse(paymentRepository.saveAndFlush(payment));
    }

    @Override @Transactional public PaymentResponse failPayment(UUID id, long version, String details) { return transitionPayment(id, version, "PENDING", "FAILED", details); }

    @Override @Transactional
    public PaymentResponse refundPayment(UUID id, long version, String details) {
        Payment payment = requirePayment(id); checkVersion("Payment", id, version, payment.getVersion()); requireStatus(payment.getStatus(), "CONFIRMED");
        Invoice invoice = requireInvoice(payment.getInvoiceId());
        if (invoice.getPaidAmount().compareTo(payment.getAmount()) < 0) throw new IllegalStateException("Invoice paid amount is inconsistent");
        invoice.setPaidAmount(invoice.getPaidAmount().subtract(payment.getAmount()));
        invoice.setStatus(invoice.getPaidAmount().signum() == 0 ? "ISSUED" : "PARTIALLY_PAID");
        payment.setStatus("REFUNDED"); payment.setDetailsJson(details);
        invoiceRepository.saveAndFlush(invoice); return mapper.toResponse(paymentRepository.saveAndFlush(payment));
    }

    @Override @Transactional(readOnly = true)
    public Page<PaymentResponse> listPayments(UUID invoiceId, Pageable pageable) {
        requireInvoice(invoiceId); return paymentRepository.findAllByBusinessIdAndInvoiceId(tenant(), invoiceId, pageable).map(mapper::toResponse);
    }

    private EstimateResponse transitionEstimate(UUID id, long version, String from, String to, String approval) {
        Estimate entity = requireEstimate(id); checkVersion("Estimate", id, version, entity.getVersion()); requireStatus(entity.getStatus(), from);
        entity.setStatus(to); entity.setApprovalDataJson(approval); if ("APPROVED".equals(to)) entity.setApprovedAt(Instant.now());
        return mapper.toResponse(estimateRepository.saveAndFlush(entity));
    }
    private InvoiceResponse transitionInvoice(UUID id, long version, String from, String to) { Invoice entity = requireInvoice(id); checkVersion("Invoice", id, version, entity.getVersion()); requireStatus(entity.getStatus(), from); entity.setStatus(to); return mapper.toResponse(invoiceRepository.saveAndFlush(entity)); }
    private PaymentResponse transitionPayment(UUID id, long version, String from, String to, String details) { Payment entity = requirePayment(id); checkVersion("Payment", id, version, entity.getVersion()); requireStatus(entity.getStatus(), from); entity.setStatus(to); entity.setDetailsJson(details); return mapper.toResponse(paymentRepository.saveAndFlush(entity)); }
    private Estimate requireEstimate(UUID id) { return estimateRepository.findByIdAndBusinessId(id, tenant()).orElseThrow(() -> new EntityNotFoundException("Estimate", id)); }
    private Invoice requireInvoice(UUID id) { return invoiceRepository.findByIdAndBusinessId(id, tenant()).orElseThrow(() -> new EntityNotFoundException("Invoice", id)); }
    private Payment requirePayment(UUID id) { return paymentRepository.findByIdAndBusinessId(id, tenant()).orElseThrow(() -> new EntityNotFoundException("Payment", id)); }
    private UUID tenant() { return tenantContext.requireBusinessId(); }
    private static BigDecimal value(BigDecimal value, BigDecimal fallback) { return value == null ? fallback : value; }
    private static void validateMoney(String currency, BigDecimal subtotal, BigDecimal discount, BigDecimal tax, BigDecimal total) { requireCurrency(currency); requireNonNegative(subtotal, "subtotal"); requireNonNegative(discount, "discount"); requireNonNegative(tax, "tax"); requireNonNegative(total, "total"); if (subtotal.subtract(discount).add(tax).compareTo(total) != 0) throw new IllegalArgumentException("Estimate total is inconsistent"); }
    private static void validateInvoiceMoney(String currency, BigDecimal subtotal, BigDecimal tax, BigDecimal total) { requireCurrency(currency); requireNonNegative(subtotal, "subtotal"); requireNonNegative(tax, "tax"); requireNonNegative(total, "total"); if (subtotal.add(tax).compareTo(total) != 0) throw new IllegalArgumentException("Invoice total is inconsistent"); }
    private static void requireCurrency(String currency) { if (currency == null || currency.length() != 3) throw new IllegalArgumentException("Currency must be an ISO-4217 code"); }
    private static void requireNonNegative(BigDecimal value, String field) { if (value == null || value.signum() < 0) throw new IllegalArgumentException(field + " must be non-negative"); }
    private static void requireStatus(String actual, String expected) { if (!expected.equals(actual)) throw new IllegalStateException("Expected status " + expected + " but was " + actual); }
    private static void checkVersion(String type, UUID id, long expected, long actual) { if (expected != actual) throw new StaleEntityException(type, id, expected, actual); }
}