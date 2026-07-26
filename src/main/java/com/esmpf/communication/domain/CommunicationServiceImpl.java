package com.esmpf.communication.domain;

import static com.esmpf.communication.CommunicationDtos.*;

import com.esmpf.communication.CommunicationService;
import com.esmpf.customer.CustomerReferenceQuery;
import com.esmpf.service.ServiceReferenceQuery;
import com.esmpf.shared.exception.EntityNotFoundException;
import com.esmpf.shared.exception.StaleEntityException;
import com.esmpf.shared.tenant.TenantContext;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class CommunicationServiceImpl implements CommunicationService {
    private final TenantContext tenantContext;
    private final CustomerReferenceQuery customerReferences;
    private final ServiceReferenceQuery serviceReferences;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationRepository notificationRepository;
    private final CustomerFeedbackRepository feedbackRepository;

    @Override @Transactional
    public NotificationTemplateResponse createTemplate(NotificationTemplateCommand command) {
        validateTemplate(command);
        if (templateRepository.existsByBusinessIdAndCodeIgnoreCaseAndChannelIgnoreCaseAndLocaleIgnoreCaseAndTemplateVersion(tenant(), command.code(), command.channel(), command.locale(), command.templateVersion())) throw new IllegalArgumentException("Notification template version already exists");
        NotificationTemplate entity = new NotificationTemplate(); entity.setBusinessId(tenant()); entity.setCode(command.code()); entity.setChannel(command.channel()); entity.setLocale(command.locale()); entity.setTemplateVersion(command.templateVersion()); entity.setSubjectTemplate(command.subjectTemplate()); entity.setBodyTemplate(command.bodyTemplate()); entity.setStatus("DRAFT");
        return response(templateRepository.saveAndFlush(entity));
    }
    @Override @Transactional
    public NotificationTemplateResponse updateDraftTemplate(UUID id, NotificationTemplateCommand command) {
        NotificationTemplate entity = requireTemplate(id); checkVersion("NotificationTemplate", id, command.version(), entity.getVersion()); requireStatus(entity.getStatus(), "DRAFT"); validateTemplate(command);
        entity.setSubjectTemplate(command.subjectTemplate()); entity.setBodyTemplate(command.bodyTemplate()); entity.setTemplateVersion(command.templateVersion());
        return response(templateRepository.saveAndFlush(entity));
    }
    @Override @Transactional public NotificationTemplateResponse activateTemplate(UUID id, long version) { return transitionTemplate(id, version, "DRAFT", "ACTIVE"); }
    @Override @Transactional public NotificationTemplateResponse archiveTemplate(UUID id, long version) { NotificationTemplate entity = requireTemplate(id); checkVersion("NotificationTemplate", id, version, entity.getVersion()); entity.setStatus("ARCHIVED"); return response(templateRepository.saveAndFlush(entity)); }
    @Override @Transactional(readOnly = true) public Page<NotificationTemplateResponse> listTemplates(Pageable pageable) { return templateRepository.findAllByBusinessId(tenant(), pageable).map(this::response); }

    @Override @Transactional
    public NotificationResponse enqueueNotification(NotificationCommand command) {
        if (command.customerId() != null) customerReferences.requireCustomer(command.customerId());
        NotificationTemplate template = null;
        if (command.notificationTemplateId() != null) { template = requireTemplate(command.notificationTemplateId()); requireStatus(template.getStatus(), "ACTIVE"); if (!template.getChannel().equalsIgnoreCase(command.channel())) throw new IllegalArgumentException("Notification channel does not match template"); }
        if (command.recipient() == null || command.recipient().isBlank()) throw new IllegalArgumentException("recipient is required");
        Notification entity = new Notification(); entity.setBusinessId(tenant()); entity.setCustomerId(command.customerId()); entity.setRecipient(command.recipient()); entity.setChannel(command.channel()); entity.setNotificationTemplateId(command.notificationTemplateId()); entity.setPayloadJson(command.payloadJson()); entity.setStatus("QUEUED"); entity.setAttemptCount(0); entity.setNextAttemptAt(command.nextAttemptAt() == null ? Instant.now() : command.nextAttemptAt());
        return response(notificationRepository.saveAndFlush(entity));
    }
    @Override @Transactional public NotificationResponse markSending(UUID id, long version) { Notification entity = requireNotification(id); checkVersion("Notification", id, version, entity.getVersion()); if (!("QUEUED".equals(entity.getStatus()) || "FAILED".equals(entity.getStatus()))) throw new IllegalStateException("Notification is not ready for sending"); entity.setStatus("SENDING"); entity.setAttemptCount(entity.getAttemptCount() + 1); entity.setLastError(null); return response(notificationRepository.saveAndFlush(entity)); }
    @Override @Transactional public NotificationResponse markSent(UUID id, long version, String providerMessageId) { Notification entity = requireNotification(id); checkVersion("Notification", id, version, entity.getVersion()); requireStatus(entity.getStatus(), "SENDING"); entity.setStatus("SENT"); entity.setSentAt(Instant.now()); entity.setProviderMessageId(providerMessageId); entity.setNextAttemptAt(null); return response(notificationRepository.saveAndFlush(entity)); }
    @Override @Transactional public NotificationResponse markFailed(UUID id, long version, String error, Instant nextAttemptAt) { Notification entity = requireNotification(id); checkVersion("Notification", id, version, entity.getVersion()); requireStatus(entity.getStatus(), "SENDING"); entity.setStatus("FAILED"); entity.setLastError(error); entity.setNextAttemptAt(nextAttemptAt); return response(notificationRepository.saveAndFlush(entity)); }
    @Override @Transactional(readOnly = true) public Page<NotificationResponse> listNotifications(Pageable pageable) { return notificationRepository.findAllByBusinessId(tenant(), pageable).map(this::response); }

    @Override @Transactional
    public FeedbackResponse registerFeedback(FeedbackCommand command) {
        customerReferences.requireCustomer(command.customerId()); if (command.jobId() != null) serviceReferences.requireJob(command.jobId());
        if (command.rating() != null && (command.rating() < 1 || command.rating() > 5)) throw new IllegalArgumentException("rating must be between 1 and 5");
        CustomerFeedback entity = new CustomerFeedback(); entity.setBusinessId(tenant()); entity.setCustomerId(command.customerId()); entity.setJobId(command.jobId()); entity.setType(command.type()); entity.setRating(command.rating()); entity.setComment(command.comment()); entity.setPublicationConsent(command.publicationConsent()); entity.setStatus("OPEN");
        return response(feedbackRepository.saveAndFlush(entity));
    }
    @Override @Transactional public FeedbackResponse respondFeedback(UUID id, long version, String responseText) { CustomerFeedback entity = requireFeedback(id); checkVersion("CustomerFeedback", id, version, entity.getVersion()); requireStatus(entity.getStatus(), "OPEN"); entity.setCompanyResponse(responseText); entity.setRespondedBy(tenantContext.requireUserId()); entity.setRespondedAt(Instant.now()); entity.setStatus("RESPONDED"); return response(feedbackRepository.saveAndFlush(entity)); }
    @Override @Transactional public FeedbackResponse resolveFeedback(UUID id, long version) { CustomerFeedback entity = requireFeedback(id); checkVersion("CustomerFeedback", id, version, entity.getVersion()); if (!("OPEN".equals(entity.getStatus()) || "RESPONDED".equals(entity.getStatus()))) throw new IllegalStateException("Feedback cannot be resolved"); entity.setStatus("RESOLVED"); entity.setResolvedAt(Instant.now()); return response(feedbackRepository.saveAndFlush(entity)); }
    @Override @Transactional public FeedbackResponse rejectFeedback(UUID id, long version, String responseText) { CustomerFeedback entity = requireFeedback(id); checkVersion("CustomerFeedback", id, version, entity.getVersion()); requireStatus(entity.getStatus(), "OPEN"); entity.setStatus("REJECTED"); entity.setCompanyResponse(responseText); entity.setRespondedBy(tenantContext.requireUserId()); entity.setRespondedAt(Instant.now()); entity.setResolvedAt(Instant.now()); return response(feedbackRepository.saveAndFlush(entity)); }
    @Override @Transactional(readOnly = true) public Page<FeedbackResponse> listFeedback(Pageable pageable) { return feedbackRepository.findAllByBusinessId(tenant(), pageable).map(this::response); }

    private NotificationTemplateResponse transitionTemplate(UUID id, long version, String from, String to) { NotificationTemplate entity = requireTemplate(id); checkVersion("NotificationTemplate", id, version, entity.getVersion()); requireStatus(entity.getStatus(), from); entity.setStatus(to); return response(templateRepository.saveAndFlush(entity)); }
    private NotificationTemplate requireTemplate(UUID id) { return templateRepository.findByIdAndBusinessId(id, tenant()).orElseThrow(() -> new EntityNotFoundException("NotificationTemplate", id)); }
    private Notification requireNotification(UUID id) { return notificationRepository.findByIdAndBusinessId(id, tenant()).orElseThrow(() -> new EntityNotFoundException("Notification", id)); }
    private CustomerFeedback requireFeedback(UUID id) { return feedbackRepository.findByIdAndBusinessId(id, tenant()).orElseThrow(() -> new EntityNotFoundException("CustomerFeedback", id)); }
    private UUID tenant() { return tenantContext.requireBusinessId(); }
    private static void validateTemplate(NotificationTemplateCommand c) { if (c.templateVersion() == null || c.templateVersion() <= 0) throw new IllegalArgumentException("templateVersion must be positive"); if (c.bodyTemplate() == null || c.bodyTemplate().isBlank()) throw new IllegalArgumentException("bodyTemplate is required"); }
    private static void requireStatus(String actual, String expected) { if (!expected.equals(actual)) throw new IllegalStateException("Expected status " + expected + " but was " + actual); }
    private static void checkVersion(String type, UUID id, long expected, long actual) { if (expected != actual) throw new StaleEntityException(type, id, expected, actual); }
    private static NotificationTemplateResponse response(NotificationTemplate e) { return new NotificationTemplateResponse(e.getId(), e.getVersion(), e.getCode(), e.getChannel(), e.getLocale(), e.getTemplateVersion(), e.getSubjectTemplate(), e.getBodyTemplate(), e.getStatus(), e.getCreatedAt(), e.getUpdatedAt()); }
    private static NotificationResponse response(Notification e) { return new NotificationResponse(e.getId(), e.getVersion(), e.getCustomerId(), e.getRecipient(), e.getChannel(), e.getNotificationTemplateId(), e.getPayloadJson(), e.getStatus(), e.getAttemptCount(), e.getNextAttemptAt(), e.getSentAt(), e.getProviderMessageId(), e.getLastError(), e.getCreatedAt(), e.getUpdatedAt()); }
    private static FeedbackResponse response(CustomerFeedback e) { return new FeedbackResponse(e.getId(), e.getVersion(), e.getCustomerId(), e.getJobId(), e.getType(), e.getRating(), e.getComment(), Boolean.TRUE.equals(e.getPublicationConsent()), e.getStatus(), e.getCompanyResponse(), e.getRespondedBy(), e.getRespondedAt(), e.getResolvedAt(), e.getCreatedAt(), e.getUpdatedAt()); }
}