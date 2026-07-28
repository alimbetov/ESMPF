package com.esmpf.platform;

import static com.esmpf.platform.PlatformDtos.*;
import static com.esmpf.web.ApiActionRequests.JsonRequest;
import static com.esmpf.web.ApiActionRequests.NotificationFailureRequest;
import static com.esmpf.web.ApiActionRequests.ProgressRequest;
import static com.esmpf.web.ApiActionRequests.ReferenceRequest;
import static com.esmpf.web.ApiActionRequests.TextRequest;
import static com.esmpf.web.ApiActionRequests.VersionRequest;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/platform")
@RequiredArgsConstructor
public class PlatformRestController {
    private final PlatformService service;

    @PostMapping("/public-tokens") @ResponseStatus(HttpStatus.CREATED)
    public PublicTokenResponse createPublicToken(@Valid @RequestBody PublicTokenCommand command) { return service.createPublicToken(command); }
    @PostMapping("/public-tokens/{tokenId}/actions/consume") public PublicTokenResponse consumePublicToken(@PathVariable UUID tokenId) { return service.consumePublicToken(tokenId); }
    @PostMapping("/public-tokens/{tokenId}/actions/revoke") public PublicTokenResponse revokePublicToken(@PathVariable UUID tokenId, @Valid @RequestBody VersionRequest request) { return service.revokePublicToken(tokenId, request.version()); }

    @PostMapping("/data-jobs") @ResponseStatus(HttpStatus.ACCEPTED)
    public DataJobResponse createDataJob(@Valid @RequestBody DataJobCommand command) { return service.createDataJob(command); }
    @PostMapping("/data-jobs/{jobId}/actions/start") public DataJobResponse startDataJob(@PathVariable UUID jobId, @Valid @RequestBody VersionRequest request) { return service.startDataJob(jobId, request.version()); }
    @PostMapping("/data-jobs/{jobId}/actions/progress") public DataJobResponse updateDataJobProgress(@PathVariable UUID jobId, @Valid @RequestBody ProgressRequest request) { return service.updateDataJobProgress(jobId, request.version(), request.progress()); }
    @PostMapping("/data-jobs/{jobId}/actions/complete") public DataJobResponse completeDataJob(@PathVariable UUID jobId, @Valid @RequestBody ReferenceRequest request) { return service.completeDataJob(jobId, request.version(), request.referenceId()); }
    @PostMapping("/data-jobs/{jobId}/actions/fail") public DataJobResponse failDataJob(@PathVariable UUID jobId, @Valid @RequestBody JsonRequest request) { return service.failDataJob(jobId, request.version(), request.dataJson()); }
    @GetMapping("/data-jobs") public Page<DataJobResponse> listDataJobs(Pageable pageable) { return service.listDataJobs(pageable); }

    @PostMapping("/outbox-events") @ResponseStatus(HttpStatus.CREATED)
    public OutboxResponse appendOutboxEvent(@Valid @RequestBody OutboxCommand command) { return service.appendOutboxEvent(command); }
    @PostMapping("/outbox-events/{eventId}/actions/mark-publishing") public OutboxResponse markOutboxPublishing(@PathVariable UUID eventId, @Valid @RequestBody VersionRequest request) { return service.markOutboxPublishing(eventId, request.version()); }
    @PostMapping("/outbox-events/{eventId}/actions/mark-published") public OutboxResponse markOutboxPublished(@PathVariable UUID eventId, @Valid @RequestBody VersionRequest request) { return service.markOutboxPublished(eventId, request.version()); }
    @PostMapping("/outbox-events/{eventId}/actions/mark-failed") public OutboxResponse markOutboxFailed(@PathVariable UUID eventId, @Valid @RequestBody NotificationFailureRequest request) { return service.markOutboxFailed(eventId, request.version(), request.error(), request.nextAttemptAt()); }
    @GetMapping("/outbox-events") public Page<OutboxResponse> listOutbox(Pageable pageable) { return service.listOutbox(pageable); }

    @PostMapping("/audit-events") @ResponseStatus(HttpStatus.CREATED)
    public AuditResponse appendAudit(@Valid @RequestBody AuditCommand command) { return service.appendAudit(command); }
    @GetMapping("/audit-events") public Page<AuditResponse> listAudit(Pageable pageable) { return service.listAudit(pageable); }

    @PostMapping("/idempotency-records") @ResponseStatus(HttpStatus.CREATED)
    public IdempotencyResponse beginIdempotentOperation(@Valid @RequestBody IdempotencyCommand command) { return service.beginIdempotentOperation(command); }
    @PostMapping("/idempotency-records/{recordId}/actions/complete") public IdempotencyResponse completeIdempotentOperation(@PathVariable UUID recordId, @Valid @RequestBody TextRequest request) { return service.completeIdempotentOperation(recordId, request.version(), request.value()); }
    @PostMapping("/idempotency-records/{recordId}/actions/fail") public IdempotencyResponse failIdempotentOperation(@PathVariable UUID recordId, @Valid @RequestBody VersionRequest request) { return service.failIdempotentOperation(recordId, request.version()); }

    @PostMapping("/integrations") @ResponseStatus(HttpStatus.CREATED)
    public IntegrationResponse createIntegration(@Valid @RequestBody IntegrationCommand command) { return service.createIntegration(command); }
    @PutMapping("/integrations/{connectionId}") public IntegrationResponse updateIntegration(@PathVariable UUID connectionId, @Valid @RequestBody IntegrationCommand command) { return service.updateIntegration(connectionId, command); }
    @PostMapping("/integrations/{connectionId}/actions/activate") public IntegrationResponse activateIntegration(@PathVariable UUID connectionId, @Valid @RequestBody VersionRequest request) { return service.activateIntegration(connectionId, request.version()); }
    @PostMapping("/integrations/{connectionId}/actions/suspend") public IntegrationResponse suspendIntegration(@PathVariable UUID connectionId, @Valid @RequestBody VersionRequest request) { return service.suspendIntegration(connectionId, request.version()); }
    @PostMapping("/integrations/{connectionId}/actions/record-success") public IntegrationResponse recordIntegrationSuccess(@PathVariable UUID connectionId, @Valid @RequestBody VersionRequest request) { return service.recordIntegrationSuccess(connectionId, request.version()); }
    @PostMapping("/integrations/{connectionId}/actions/record-failure") public IntegrationResponse recordIntegrationFailure(@PathVariable UUID connectionId, @Valid @RequestBody VersionRequest request) { return service.recordIntegrationFailure(connectionId, request.version()); }
    @GetMapping("/integrations") public Page<IntegrationResponse> listIntegrations(Pageable pageable) { return service.listIntegrations(pageable); }

    @PostMapping("/document-numbers/actions/allocate")
    public String allocateDocumentNumber(@RequestParam String documentType,
                                         @RequestParam int year,
                                         @RequestParam(required = false, defaultValue = "") String prefix) {
        return service.allocateDocumentNumber(documentType, year, prefix);
    }
}
