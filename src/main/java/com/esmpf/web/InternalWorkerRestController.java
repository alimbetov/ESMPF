package com.esmpf.web;

import static com.esmpf.communication.CommunicationDtos.NotificationResponse;
import static com.esmpf.document.DocumentDtos.GeneratedDocumentResponse;
import static com.esmpf.platform.PlatformDtos.*;
import static com.esmpf.web.ApiActionRequests.*;

import com.esmpf.communication.CommunicationService;
import com.esmpf.document.DocumentService;
import com.esmpf.platform.PlatformService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/v1")
@RequiredArgsConstructor
public class InternalWorkerRestController {
    private final PlatformService platformService;
    private final CommunicationService communicationService;
    private final DocumentService documentService;

    @PostMapping("/platform/outbox-events")
    @ResponseStatus(HttpStatus.CREATED)
    public OutboxResponse appendOutboxEvent(@Valid @RequestBody OutboxCommand command) {
        return platformService.appendOutboxEvent(command);
    }

    @PostMapping("/platform/outbox-events/{eventId}/actions/mark-publishing")
    public OutboxResponse markOutboxPublishing(@PathVariable UUID eventId,
                                               @Valid @RequestBody VersionRequest request) {
        return platformService.markOutboxPublishing(eventId, request.version());
    }

    @PostMapping("/platform/outbox-events/{eventId}/actions/mark-published")
    public OutboxResponse markOutboxPublished(@PathVariable UUID eventId,
                                              @Valid @RequestBody VersionRequest request) {
        return platformService.markOutboxPublished(eventId, request.version());
    }

    @PostMapping("/platform/outbox-events/{eventId}/actions/mark-failed")
    public OutboxResponse markOutboxFailed(@PathVariable UUID eventId,
                                           @Valid @RequestBody NotificationFailureRequest request) {
        return platformService.markOutboxFailed(eventId, request.version(), request.error(), request.nextAttemptAt());
    }

    @PostMapping("/platform/audit-events")
    @ResponseStatus(HttpStatus.CREATED)
    public AuditResponse appendAudit(@Valid @RequestBody AuditCommand command) {
        return platformService.appendAudit(command);
    }

    @PostMapping("/platform/idempotency-records")
    @ResponseStatus(HttpStatus.CREATED)
    public IdempotencyResponse beginIdempotentOperation(@Valid @RequestBody IdempotencyCommand command) {
        return platformService.beginIdempotentOperation(command);
    }

    @PostMapping("/platform/idempotency-records/{recordId}/actions/complete")
    public IdempotencyResponse completeIdempotentOperation(@PathVariable UUID recordId,
                                                           @Valid @RequestBody TextRequest request) {
        return platformService.completeIdempotentOperation(recordId, request.version(), request.value());
    }

    @PostMapping("/platform/idempotency-records/{recordId}/actions/fail")
    public IdempotencyResponse failIdempotentOperation(@PathVariable UUID recordId,
                                                       @Valid @RequestBody VersionRequest request) {
        return platformService.failIdempotentOperation(recordId, request.version());
    }

    @PostMapping("/platform/data-jobs/{jobId}/actions/start")
    public DataJobResponse startDataJob(@PathVariable UUID jobId,
                                        @Valid @RequestBody VersionRequest request) {
        return platformService.startDataJob(jobId, request.version());
    }

    @PostMapping("/platform/data-jobs/{jobId}/actions/progress")
    public DataJobResponse updateDataJobProgress(@PathVariable UUID jobId,
                                                 @Valid @RequestBody ProgressRequest request) {
        return platformService.updateDataJobProgress(jobId, request.version(), request.progress());
    }

    @PostMapping("/platform/data-jobs/{jobId}/actions/complete")
    public DataJobResponse completeDataJob(@PathVariable UUID jobId,
                                           @Valid @RequestBody ReferenceRequest request) {
        return platformService.completeDataJob(jobId, request.version(), request.referenceId());
    }

    @PostMapping("/platform/data-jobs/{jobId}/actions/fail")
    public DataJobResponse failDataJob(@PathVariable UUID jobId,
                                       @Valid @RequestBody JsonRequest request) {
        return platformService.failDataJob(jobId, request.version(), request.dataJson());
    }

    @PostMapping("/platform/integrations/{connectionId}/actions/record-success")
    public IntegrationResponse recordIntegrationSuccess(@PathVariable UUID connectionId,
                                                        @Valid @RequestBody VersionRequest request) {
        return platformService.recordIntegrationSuccess(connectionId, request.version());
    }

    @PostMapping("/platform/integrations/{connectionId}/actions/record-failure")
    public IntegrationResponse recordIntegrationFailure(@PathVariable UUID connectionId,
                                                        @Valid @RequestBody VersionRequest request) {
        return platformService.recordIntegrationFailure(connectionId, request.version());
    }

    @PostMapping("/notifications/{notificationId}/actions/mark-sending")
    public NotificationResponse markSending(@PathVariable UUID notificationId,
                                            @Valid @RequestBody VersionRequest request) {
        return communicationService.markSending(notificationId, request.version());
    }

    @PostMapping("/notifications/{notificationId}/actions/mark-sent")
    public NotificationResponse markSent(@PathVariable UUID notificationId,
                                         @Valid @RequestBody TextRequest request) {
        return communicationService.markSent(notificationId, request.version(), request.value());
    }

    @PostMapping("/notifications/{notificationId}/actions/mark-failed")
    public NotificationResponse markFailed(@PathVariable UUID notificationId,
                                           @Valid @RequestBody NotificationFailureRequest request) {
        return communicationService.markFailed(notificationId, request.version(), request.error(), request.nextAttemptAt());
    }

    @PostMapping("/generated-documents/{documentId}/actions/start")
    public GeneratedDocumentResponse startGeneration(@PathVariable UUID documentId,
                                                      @Valid @RequestBody VersionRequest request) {
        return documentService.startGeneration(documentId, request.version());
    }

    @PostMapping("/generated-documents/{documentId}/actions/complete")
    public GeneratedDocumentResponse completeGeneration(@PathVariable UUID documentId,
                                                         @Valid @RequestBody GeneratedDocumentCompleteRequest request) {
        return documentService.completeGeneration(documentId, request.version(), request.attachmentId(), request.checksum());
    }

    @PostMapping("/generated-documents/{documentId}/actions/fail")
    public GeneratedDocumentResponse failGeneration(@PathVariable UUID documentId,
                                                     @Valid @RequestBody JsonRequest request) {
        return documentService.failGeneration(documentId, request.version(), request.dataJson());
    }
}
