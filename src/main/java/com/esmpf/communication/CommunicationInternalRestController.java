package com.esmpf.communication;

import static com.esmpf.communication.CommunicationDtos.NotificationResponse;
import static com.esmpf.web.ApiActionRequests.*;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/v1/notifications")
@RequiredArgsConstructor
public class CommunicationInternalRestController {
    private final CommunicationService service;

    @PostMapping("/{notificationId}/actions/mark-sending")
    public NotificationResponse markSending(@PathVariable UUID notificationId,
                                            @Valid @RequestBody VersionRequest request) {
        return service.markSending(notificationId, request.version());
    }

    @PostMapping("/{notificationId}/actions/mark-sent")
    public NotificationResponse markSent(@PathVariable UUID notificationId,
                                         @Valid @RequestBody TextRequest request) {
        return service.markSent(notificationId, request.version(), request.value());
    }

    @PostMapping("/{notificationId}/actions/mark-failed")
    public NotificationResponse markFailed(@PathVariable UUID notificationId,
                                           @Valid @RequestBody NotificationFailureRequest request) {
        return service.markFailed(notificationId, request.version(), request.error(), request.nextAttemptAt());
    }
}
