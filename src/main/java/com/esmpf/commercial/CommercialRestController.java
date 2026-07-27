package com.esmpf.commercial;

import static com.esmpf.commercial.CommercialDtos.*;
import static com.esmpf.web.ApiActionRequests.InvoiceFromEstimateRequest;
import static com.esmpf.web.ApiActionRequests.JsonRequest;
import static com.esmpf.web.ApiActionRequests.ReferenceRequest;
import static com.esmpf.web.ApiActionRequests.VersionRequest;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommercialRestController {
    private final CommercialService service;

    @PostMapping("/estimates") @ResponseStatus(HttpStatus.CREATED)
    public EstimateResponse createEstimate(@Valid @RequestBody EstimateCreateCommand command) { return service.createEstimate(command); }
    @GetMapping("/estimates/{estimateId}") public EstimateResponse getEstimate(@PathVariable UUID estimateId) { return service.getEstimate(estimateId); }
    @GetMapping("/estimates") public Page<EstimateResponse> listEstimates(Pageable pageable) { return service.listEstimates(pageable); }
    @PutMapping("/estimates/{estimateId}") public EstimateResponse updateDraftEstimate(@PathVariable UUID estimateId, @Valid @RequestBody EstimateUpdateCommand command) { return service.updateDraftEstimate(estimateId, command); }
    @PostMapping("/estimates/{estimateId}/actions/send") public EstimateResponse sendEstimate(@PathVariable UUID estimateId, @Valid @RequestBody VersionRequest request) { return service.sendEstimate(estimateId, request.version()); }
    @PostMapping("/estimates/{estimateId}/actions/approve") public EstimateResponse approveEstimate(@PathVariable UUID estimateId, @Valid @RequestBody JsonRequest request) { return service.approveEstimate(estimateId, request.version(), request.dataJson()); }
    @PostMapping("/estimates/{estimateId}/actions/reject") public EstimateResponse rejectEstimate(@PathVariable UUID estimateId, @Valid @RequestBody JsonRequest request) { return service.rejectEstimate(estimateId, request.version(), request.dataJson()); }

    @PostMapping("/invoices") @ResponseStatus(HttpStatus.CREATED)
    public InvoiceResponse createInvoice(@Valid @RequestBody InvoiceCreateCommand command) { return service.createInvoice(command); }
    @PostMapping("/estimates/{estimateId}/actions/create-invoice") @ResponseStatus(HttpStatus.CREATED)
    public InvoiceResponse createInvoiceFromEstimate(@PathVariable UUID estimateId, @Valid @RequestBody InvoiceFromEstimateRequest request) {
        return service.createInvoiceFromEstimate(estimateId, request.estimateVersion(), request.invoiceNumber(), request.dueDate());
    }
    @GetMapping("/invoices/{invoiceId}") public InvoiceResponse getInvoice(@PathVariable UUID invoiceId) { return service.getInvoice(invoiceId); }
    @GetMapping("/invoices") public Page<InvoiceResponse> listInvoices(Pageable pageable) { return service.listInvoices(pageable); }
    @PostMapping("/invoices/{invoiceId}/actions/issue") public InvoiceResponse issueInvoice(@PathVariable UUID invoiceId, @Valid @RequestBody VersionRequest request) { return service.issueInvoice(invoiceId, request.version()); }
    @PostMapping("/invoices/{invoiceId}/actions/mark-overdue") public InvoiceResponse markInvoiceOverdue(@PathVariable UUID invoiceId, @Valid @RequestBody VersionRequest request) { return service.markInvoiceOverdue(invoiceId, request.version()); }
    @PostMapping("/invoices/{invoiceId}/actions/void") public InvoiceResponse voidInvoice(@PathVariable UUID invoiceId, @Valid @RequestBody VersionRequest request) { return service.voidInvoice(invoiceId, request.version()); }
    @PostMapping("/invoices/{invoiceId}/actions/attach-generated-document") public InvoiceResponse attachGeneratedDocument(@PathVariable UUID invoiceId, @Valid @RequestBody ReferenceRequest request) { return service.attachGeneratedDocument(invoiceId, request.version(), request.referenceId()); }

    @PostMapping("/payments") @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse registerPayment(@Valid @RequestBody PaymentCreateCommand command) { return service.registerPayment(command); }
    @PostMapping("/payments/{paymentId}/actions/confirm") public PaymentResponse confirmPayment(@PathVariable UUID paymentId, @Valid @RequestBody VersionRequest request) { return service.confirmPayment(paymentId, request.version()); }
    @PostMapping("/payments/{paymentId}/actions/fail") public PaymentResponse failPayment(@PathVariable UUID paymentId, @Valid @RequestBody JsonRequest request) { return service.failPayment(paymentId, request.version(), request.dataJson()); }
    @PostMapping("/payments/{paymentId}/actions/refund") public PaymentResponse refundPayment(@PathVariable UUID paymentId, @Valid @RequestBody JsonRequest request) { return service.refundPayment(paymentId, request.version(), request.dataJson()); }
    @GetMapping("/invoices/{invoiceId}/payments") public Page<PaymentResponse> listPayments(@PathVariable UUID invoiceId, Pageable pageable) { return service.listPayments(invoiceId, pageable); }
}
