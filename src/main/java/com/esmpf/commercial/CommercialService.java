package com.esmpf.commercial;

import static com.esmpf.commercial.CommercialDtos.*;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommercialService {
    EstimateResponse createEstimate(EstimateCreateCommand command);
    EstimateResponse getEstimate(UUID estimateId);
    Page<EstimateResponse> listEstimates(Pageable pageable);
    EstimateResponse updateDraftEstimate(UUID estimateId, EstimateUpdateCommand command);
    EstimateResponse sendEstimate(UUID estimateId, long version);
    EstimateResponse approveEstimate(UUID estimateId, long version, String approvalDataJson);
    EstimateResponse rejectEstimate(UUID estimateId, long version, String approvalDataJson);

    InvoiceResponse createInvoice(InvoiceCreateCommand command);
    InvoiceResponse createInvoiceFromEstimate(UUID estimateId, long estimateVersion, String invoiceNumber, java.time.LocalDate dueDate);
    InvoiceResponse getInvoice(UUID invoiceId);
    Page<InvoiceResponse> listInvoices(Pageable pageable);
    InvoiceResponse issueInvoice(UUID invoiceId, long version);
    InvoiceResponse markInvoiceOverdue(UUID invoiceId, long version);
    InvoiceResponse voidInvoice(UUID invoiceId, long version);
    InvoiceResponse attachGeneratedDocument(UUID invoiceId, long version, UUID generatedDocumentId);

    PaymentResponse registerPayment(PaymentCreateCommand command);
    PaymentResponse confirmPayment(UUID paymentId, long version);
    PaymentResponse failPayment(UUID paymentId, long version, String detailsJson);
    PaymentResponse refundPayment(UUID paymentId, long version, String detailsJson);
    Page<PaymentResponse> listPayments(UUID invoiceId, Pageable pageable);
}