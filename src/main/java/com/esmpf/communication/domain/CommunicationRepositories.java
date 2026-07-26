package com.esmpf.communication.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {
    Optional<NotificationTemplate> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<NotificationTemplate> findAllByBusinessId(UUID businessId, Pageable pageable);
    boolean existsByBusinessIdAndCodeIgnoreCaseAndChannelIgnoreCaseAndLocaleIgnoreCaseAndTemplateVersion(UUID businessId, String code, String channel, String locale, Integer version);
}
interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Optional<Notification> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<Notification> findAllByBusinessId(UUID businessId, Pageable pageable);
}
interface CustomerFeedbackRepository extends JpaRepository<CustomerFeedback, UUID> {
    Optional<CustomerFeedback> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<CustomerFeedback> findAllByBusinessId(UUID businessId, Pageable pageable);
}