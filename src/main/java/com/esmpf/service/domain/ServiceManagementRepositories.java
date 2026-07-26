package com.esmpf.service.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface ServiceRequestRepository extends JpaRepository<ServiceRequest, UUID> {
    Optional<ServiceRequest> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<ServiceRequest> findAllByBusinessId(UUID businessId, Pageable pageable);
}

interface ServiceJobRepository extends JpaRepository<ServiceJob, UUID> {
    Optional<ServiceJob> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<ServiceJob> findAllByBusinessId(UUID businessId, Pageable pageable);
    boolean existsByBusinessIdAndRequestId(UUID businessId, UUID requestId);
}

interface JobVisitRepository extends JpaRepository<JobVisit, UUID> {
    Optional<JobVisit> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<JobVisit> findAllByBusinessIdAndJobId(UUID businessId, UUID jobId, Pageable pageable);
    boolean existsByBusinessIdAndJobIdAndStatusIn(UUID businessId, UUID jobId, Iterable<String> statuses);
}

interface JobExecutionRepository extends JpaRepository<JobExecution, UUID> {
    Optional<JobExecution> findByIdAndBusinessId(UUID id, UUID businessId);
    boolean existsByBusinessIdAndVisitIdAndStatusIn(UUID businessId, UUID visitId, Iterable<String> statuses);
}

interface WorkReportRepository extends JpaRepository<WorkReport, UUID> {
    Optional<WorkReport> findByIdAndBusinessId(UUID id, UUID businessId);
    boolean existsByBusinessIdAndJobId(UUID businessId, UUID jobId);
    boolean existsByBusinessIdAndJobExecutionId(UUID businessId, UUID executionId);
}
