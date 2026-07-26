package com.esmpf.communication;

import static com.esmpf.communication.CommunicationDtos.*;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommunicationService {
    NotificationTemplateResponse createTemplate(NotificationTemplateCommand command);
    NotificationTemplateResponse updateDraftTemplate(UUID templateId, NotificationTemplateCommand command);
    NotificationTemplateResponse activateTemplate(UUID templateId, long version);
    NotificationTemplateResponse archiveTemplate(UUID templateId, long version);
    Page<NotificationTemplateResponse> listTemplates(Pageable pageable);

    NotificationResponse enqueueNotification(NotificationCommand command);
    NotificationResponse markSending(UUID notificationId, long version);
    NotificationResponse markSent(UUID notificationId, long version, String providerMessageId);
    NotificationResponse markFailed(UUID notificationId, long version, String error, Instant nextAttemptAt);
    Page<NotificationResponse> listNotifications(Pageable pageable);

    FeedbackResponse registerFeedback(FeedbackCommand command);
    FeedbackResponse respondFeedback(UUID feedbackId, long version, String response);
    FeedbackResponse resolveFeedback(UUID feedbackId, long version);
    FeedbackResponse rejectFeedback(UUID feedbackId, long version, String response);
    Page<FeedbackResponse> listFeedback(Pageable pageable);
}