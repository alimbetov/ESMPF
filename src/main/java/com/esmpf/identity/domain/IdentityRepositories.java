package com.esmpf.identity.domain;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface BusinessRepository extends JpaRepository<Business, UUID> {
    boolean existsByCodeIgnoreCase(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Business b where b.id=:id")
    Optional<Business> lockById(@Param("id") UUID id);
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
