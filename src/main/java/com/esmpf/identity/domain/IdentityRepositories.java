package com.esmpf.identity.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface BusinessRepository extends JpaRepository<Business, UUID> {
    boolean existsByCodeIgnoreCase(String code);
}

interface BusinessLocationRepository extends JpaRepository<BusinessLocation, UUID> {
    Optional<BusinessLocation> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<BusinessLocation> findAllByBusinessId(UUID businessId, Pageable pageable);
}

interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
    Optional<UserAccount> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<UserAccount> findAllByBusinessId(UUID businessId, Pageable pageable);
    boolean existsByBusinessIdAndEmailIgnoreCase(UUID businessId, String email);
    boolean existsByBusinessIdAndEmailIgnoreCaseAndIdNot(UUID businessId, String email, UUID id);
    boolean existsByBusinessIdAndExternalProviderAndExternalSubject(UUID businessId, String provider, String subject);
}

interface WorkerQualificationRepository extends JpaRepository<WorkerQualification, UUID> {
    Optional<WorkerQualification> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<WorkerQualification> findAllByBusinessIdAndUserId(UUID businessId, UUID userId, Pageable pageable);
}