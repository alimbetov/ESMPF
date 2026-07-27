package com.esmpf.communication;

import static com.esmpf.communication.CommunicationDtos.*;
import static com.esmpf.web.ApiActionRequests.NotificationFailureRequest;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommunicationRestController {
    private final CommunicationService service;

    @PostMapping("/notification-templates") @ResponseStatus(HttpStatus.CREATED)
    public NotificationTemplateResponse createTemplate(@Valid @RequestBody NotificationTemplateCommand command) { return service.createTemplate(command); }
    @PutMapping("/notification-templates/{templateId}") public NotificationTemplateResponse updateDraftTemplate(@PathVariable UUID templateId, @Valid @RequestBody NotificationTemplateCommand command) { return service.updateDraftTemplate(templateId, command); }
    @PostMapping("/notification-templates/{templateId}/actions/activate") public NotificationTemplateResponse activateTemplate(@PathVariable UUID templateId, @Valid @RequestBody VersionRequest request) { return service.activateTemplate(templateId, request.version()); }
    @PostMapping("/notification-templates/{templateId}/actions/archive") public NotificationTemplateResponse archiveTemplate(@PathVariable UUID templateId, @Valid @RequestBody VersionRequest request) { return service.archiveTemplate(templateId, request.version()); }
    @GetMapping("/notification-templates") public Page<NotificationTemplateResponse> listTemplates(Pageable pageable) { return service.listTemplates(pageable); }

    @PostMapping("/notifications") @ResponseStatus(HttpStatus.ACCEPTED)
    public NotificationResponse enqueueNotification(@Valid @RequestBody NotificationCommand command) { return service.enqueueNotification(command); }
    @PostMapping("/notifications/{notificationId}/actions/mark-sending") public NotificationResponse markSending(@PathVariable UUID notificationId, @Valid @RequestBody VersionRequest request) { return service.markSending(notificationId, request.version()); }
    @PostMapping("/notifications/{notificationId}/actions/mark-sent") public NotificationResponse markSent(@PathVariable UUID notificationId, @Valid @RequestBody TextRequest request) { return service.markSent(notificationId, request.version(), request.value()); }
    @PostMapping("/notifications/{notificationId}/actions/mark-failed") public NotificationResponse markFailed(@PathVariable UUID notificationId, @Valid @RequestBody NotificationFailureRequest request) { return service.markFailed(notificationId, request.version(), request.error(), request.nextAttemptAt()); }
    @GetMapping("/notifications") public Page<NotificationResponse> listNotifications(Pageable pageable) { return service.listNotifications(pageable); }

    @PostMapping("/feedback") @ResponseStatus(HttpStatus.CREATED)
    public FeedbackResponse registerFeedback(@Valid @RequestBody FeedbackCommand command) { return service.registerFeedback(command); }
    @PostMapping("/feedback/{feedbackId}/actions/respond") public FeedbackResponse respondFeedback(@PathVariable UUID feedbackId, @Valid @RequestBody TextRequest request) { return service.respondFeedback(feedbackId, request.version(), request.value()); }
    @PostMapping("/feedback/{feedbackId}/actions/resolve") public FeedbackResponse resolveFeedback(@PathVariable UUID feedbackId, @Valid @RequestBody VersionRequest request) { return service.resolveFeedback(feedbackId, request.version()); }
    @PostMapping("/feedback/{feedbackId}/actions/reject") public FeedbackResponse rejectFeedback(@PathVariable UUID feedbackId, @Valid @RequestBody TextRequest request) { return service.rejectFeedback(feedbackId, request.version(), request.value()); }
    @GetMapping("/feedback") public Page<FeedbackResponse> listFeedback(Pageable pageable) { return service.listFeedback(pageable); }
}
