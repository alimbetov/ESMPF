package com.esmpf.service.domain;

import static com.esmpf.service.ServiceManagementDtos.*;

import com.esmpf.catalog.CatalogDtos.ChecklistTemplateReference;
import com.esmpf.catalog.CatalogDtos.JobTypeReference;
import com.esmpf.catalog.CatalogReferenceQuery;
import com.esmpf.customer.CustomerDtos.CustomerReference;
import com.esmpf.customer.CustomerDtos.ServiceLocationReference;
import com.esmpf.customer.CustomerReferenceQuery;
import com.esmpf.equipment.EquipmentDtos.EquipmentReference;
import com.esmpf.equipment.EquipmentReferenceQuery;
import com.esmpf.service.ServiceManagementService;
import com.esmpf.service.ServiceReferenceQuery;
import com.esmpf.shared.exception.EntityNotFoundException;
import com.esmpf.shared.exception.StaleEntityException;
import com.esmpf.shared.tenant.TenantContext;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class ServiceManagementServiceImpl implements ServiceManagementService, ServiceReferenceQuery {

    private final TenantContext tenantContext;
    private final CustomerReferenceQuery customerReferences;
    private final EquipmentReferenceQuery equipmentReferences;
    private final CatalogReferenceQuery catalogReferences;
    private final ServiceRequestRepository requestRepository;
    private final ServiceJobRepository jobRepository;
    private final JobVisitRepository visitRepository;
    private final JobExecutionRepository executionRepository;
    private final WorkReportRepository reportRepository;
    private final ServiceManagementMapper mapper;

    @Override
    @Transactional
    public ServiceRequestResponse createRequest(ServiceRequestCreateCommand command) {
        validateCustomerGraph(command.customerId(), command.serviceLocationId(), command.equipmentId());
        ServiceRequest entity = mapper.toEntity(command);
        entity.setBusinessId(tenant());
        entity.setRequestedAt(Instant.now());
        entity.setRequestedBy(tenantContext.requireUserId());
        return mapper.toResponse(requestRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRequestResponse getRequest(UUID requestId) {
        return mapper.toResponse(requireRequestEntity(requestId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceRequestResponse> listRequests(Pageable pageable) {
        return requestRepository.findAllByBusinessId(tenant(), pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public ServiceRequestResponse triageRequest(UUID requestId, long version) {
        ServiceRequest entity = requireRequestEntity(requestId);
        transitionRequest(entity, version, ServiceRequestStatus.NEW, ServiceRequestStatus.TRIAGED);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ServiceRequestResponse acceptRequest(UUID requestId, long version) {
        ServiceRequest entity = requireRequestEntity(requestId);
        transitionRequest(entity, version, ServiceRequestStatus.TRIAGED, ServiceRequestStatus.ACCEPTED);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ServiceRequestResponse rejectRequest(UUID requestId, long version) {
        ServiceRequest entity = requireRequestEntity(requestId);
        transitionRequest(entity, version, ServiceRequestStatus.TRIAGED, ServiceRequestStatus.REJECTED);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ServiceRequestResponse cancelRequest(UUID requestId, long version) {
        ServiceRequest entity = requireRequestEntity(requestId);
        checkVersion("ServiceRequest", requestId, version, entity.getVersion());
        requireNonTerminalRequest(entity.getStatus());
        entity.setStatus(ServiceRequestStatus.CANCELLED.name());
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ServiceJobResponse createJob(ServiceJobCreateCommand command) {
        validateCustomerGraph(command.customerId(), command.serviceLocationId(), command.equipmentId());
        JobTypeReference jobType = catalogReferences.requireJobType(command.jobTypeId());
        requireActive(jobType.status(), "JobType");
        if (command.requestId() != null) {
            ServiceRequest request = requireRequestEntity(command.requestId());
            if (!ServiceRequestStatus.ACCEPTED.name().equals(request.getStatus())) {
                throw new IllegalStateException("Job can only be created from an ACCEPTED service request");
            }
            validateRequestMatchesJob(request, command);
            if (jobRepository.existsByBusinessIdAndRequestId(tenant(), command.requestId())) {
                throw new IllegalArgumentException("Service request already has a service job");
            }
        }
        ServiceJob entity = mapper.toEntity(command);
        entity.setBusinessId(tenant());
        return mapper.toResponse(jobRepository.save(entity));
    }

    @Override
    @Transactional
    public ServiceJobResponse convertRequestToJob(
            UUID requestId,
            long requestVersion,
            ServiceJobCreateCommand command
    ) {
        ServiceRequest request = requireRequestEntity(requestId);
        checkVersion("ServiceRequest", requestId, requestVersion, request.getVersion());
        if (!ServiceRequestStatus.ACCEPTED.name().equals(request.getStatus())) {
            throw new IllegalStateException("Only ACCEPTED service requests can be converted to jobs");
        }
        if (command.requestId() != null && !requestId.equals(command.requestId())) {
            throw new IllegalArgumentException("Command requestId does not match converted request");
        }
        ServiceJobCreateCommand normalized = new ServiceJobCreateCommand(
                requestId,
                command.maintenanceOccurrenceId(),
                request.getCustomerId(),
                request.getServiceLocationId(),
                request.getEquipmentId(),
                command.jobTypeId(),
                command.serviceAgreementId(),
                command.priority(),
                command.title(),
                command.description(),
                command.leadWorkerId(),
                command.assignedWorkerIdsJson());
        ServiceJobResponse job = createJob(normalized);
        request.setStatus(ServiceRequestStatus.CONVERTED_TO_JOB.name());
        return job;
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceJobResponse getJob(UUID jobId) {
        return mapper.toResponse(requireJobEntity(jobId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceJobResponse> listJobs(Pageable pageable) {
        return jobRepository.findAllByBusinessId(tenant(), pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public ServiceJobResponse markJobReady(UUID jobId, long version) {
        ServiceJob entity = requireJobEntity(jobId);
        transitionJob(entity, version, ServiceJobStatus.DRAFT, ServiceJobStatus.READY);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ServiceJobResponse scheduleJob(UUID jobId, ServiceJobScheduleCommand command) {
        ServiceJob entity = requireJobEntity(jobId);
        checkVersion("ServiceJob", jobId, command.version(), entity.getVersion());
        if (!(ServiceJobStatus.READY.name().equals(entity.getStatus())
                || ServiceJobStatus.SCHEDULED.name().equals(entity.getStatus()))) {
            throw new IllegalStateException("Only READY or SCHEDULED jobs can be scheduled");
        }
        if (!command.plannedEnd().isAfter(command.plannedStart())) {
            throw new IllegalArgumentException("plannedEnd must be after plannedStart");
        }
        entity.setPlannedStart(command.plannedStart());
        entity.setPlannedEnd(command.plannedEnd());
        entity.setLeadWorkerId(command.leadWorkerId());
        entity.setAssignedWorkerIdsJson(command.assignedWorkerIdsJson());
        entity.setStatus(ServiceJobStatus.SCHEDULED.name());
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ServiceJobResponse startJob(UUID jobId, long version) {
        ServiceJob entity = requireJobEntity(jobId);
        transitionJob(entity, version, ServiceJobStatus.SCHEDULED, ServiceJobStatus.IN_PROGRESS);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ServiceJobResponse holdJob(UUID jobId, long version, String reason) {
        ServiceJob entity = requireJobEntity(jobId);
        transitionJob(entity, version, ServiceJobStatus.IN_PROGRESS, ServiceJobStatus.WAITING);
        entity.setBlockedReason(reason);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ServiceJobResponse resumeJob(UUID jobId, long version) {
        ServiceJob entity = requireJobEntity(jobId);
        transitionJob(entity, version, ServiceJobStatus.WAITING, ServiceJobStatus.IN_PROGRESS);
        entity.setBlockedReason(null);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ServiceJobResponse completeJob(UUID jobId, long version) {
        ServiceJob entity = requireJobEntity(jobId);
        checkVersion("ServiceJob", jobId, version, entity.getVersion());
        if (!(ServiceJobStatus.IN_PROGRESS.name().equals(entity.getStatus())
                || ServiceJobStatus.WAITING.name().equals(entity.getStatus()))) {
            throw new IllegalStateException("Only IN_PROGRESS or WAITING jobs can be completed");
        }
        if (visitRepository.existsByBusinessIdAndJobIdAndStatusIn(
                tenant(), jobId, List.of("PLANNED", "IN_PROGRESS"))) {
            throw new IllegalStateException("Service job has unfinished visits");
        }
        if (!reportRepository.existsByBusinessIdAndJobId(tenant(), jobId)) {
            throw new IllegalStateException("Service job requires a work report before completion");
        }
        entity.setStatus(ServiceJobStatus.COMPLETED.name());
        entity.setBlockedReason(null);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ServiceJobResponse closeJob(UUID jobId, long version) {
        ServiceJob entity = requireJobEntity(jobId);
        transitionJob(entity, version, ServiceJobStatus.COMPLETED, ServiceJobStatus.CLOSED);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ServiceJobResponse cancelJob(UUID jobId, long version, String reason) {
        ServiceJob entity = requireJobEntity(jobId);
        checkVersion("ServiceJob", jobId, version, entity.getVersion());
        if (ServiceJobStatus.CLOSED.name().equals(entity.getStatus())
                || ServiceJobStatus.COMPLETED.name().equals(entity.getStatus())
                || ServiceJobStatus.CANCELLED.name().equals(entity.getStatus())) {
            throw new IllegalStateException("Terminal service job cannot be cancelled");
        }
        entity.setStatus(ServiceJobStatus.CANCELLED.name());
        entity.setBlockedReason(reason);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public JobVisitResponse planVisit(JobVisitPlanCommand command) {
        ServiceJob job = requireJobEntity(command.jobId());
        if (!(ServiceJobStatus.READY.name().equals(job.getStatus())
                || ServiceJobStatus.SCHEDULED.name().equals(job.getStatus()))) {
            throw new IllegalStateException("Visits can only be planned for READY or SCHEDULED jobs");
        }
        if (!command.scheduledEnd().isAfter(command.scheduledStart())) {
            throw new IllegalArgumentException("scheduledEnd must be after scheduledStart");
        }
        JobVisit entity = mapper.toEntity(command);
        entity.setBusinessId(tenant());
        JobVisit saved = visitRepository.save(entity);
        if (ServiceJobStatus.READY.name().equals(job.getStatus())) {
            job.setStatus(ServiceJobStatus.SCHEDULED.name());
            job.setPlannedStart(command.scheduledStart());
            job.setPlannedEnd(command.scheduledEnd());
        }
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public JobVisitResponse startVisit(UUID visitId, long version, String arrivalDataJson) {
        JobVisit entity = requireVisitEntity(visitId);
        checkVersion("JobVisit", visitId, version, entity.getVersion());
        if (!JobVisitStatus.PLANNED.name().equals(entity.getStatus())) {
            throw new IllegalStateException("Only PLANNED visits can be started");
        }
        ServiceJob job = requireJobEntity(entity.getJobId());
        if (!(ServiceJobStatus.SCHEDULED.name().equals(job.getStatus())
                || ServiceJobStatus.IN_PROGRESS.name().equals(job.getStatus()))) {
            throw new IllegalStateException("Job is not ready for visit execution");
        }
        entity.setStatus(JobVisitStatus.IN_PROGRESS.name());
        entity.setActualStart(Instant.now());
        entity.setArrivalDataJson(arrivalDataJson);
        job.setStatus(ServiceJobStatus.IN_PROGRESS.name());
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public JobVisitResponse completeVisit(
            UUID visitId,
            long version,
            String completionDataJson,
            String customerConfirmationJson
    ) {
        JobVisit entity = requireVisitEntity(visitId);
        checkVersion("JobVisit", visitId, version, entity.getVersion());
        if (!JobVisitStatus.IN_PROGRESS.name().equals(entity.getStatus())) {
            throw new IllegalStateException("Only IN_PROGRESS visits can be completed");
        }
        entity.setStatus(JobVisitStatus.COMPLETED.name());
        entity.setActualEnd(Instant.now());
        entity.setCompletionDataJson(completionDataJson);
        entity.setCustomerConfirmationJson(customerConfirmationJson);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public JobVisitResponse cancelVisit(UUID visitId, long version) {
        JobVisit entity = requireVisitEntity(visitId);
        checkVersion("JobVisit", visitId, version, entity.getVersion());
        if (!(JobVisitStatus.PLANNED.name().equals(entity.getStatus())
                || JobVisitStatus.CONFIRMED.name().equals(entity.getStatus()))) {
            throw new IllegalStateException("Only PLANNED or CONFIRMED visits can be cancelled");
        }
        entity.setStatus(JobVisitStatus.CANCELLED.name());
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobVisitResponse> listVisits(UUID jobId, Pageable pageable) {
        requireJobEntity(jobId);
        return visitRepository.findAllByBusinessIdAndJobId(tenant(), jobId, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public JobExecutionResponse startExecution(JobExecutionStartCommand command) {
        ServiceJob job = requireJobEntity(command.jobId());
        JobVisit visit = requireVisitEntity(command.visitId());
        if (!visit.getJobId().equals(job.getId())) {
            throw new IllegalArgumentException("Job visit belongs to another service job");
        }
        if (!JobVisitStatus.IN_PROGRESS.name().equals(visit.getStatus())) {
            throw new IllegalStateException("Checklist execution requires an IN_PROGRESS visit");
        }
        ChecklistTemplateReference checklist = catalogReferences.requireChecklistTemplate(command.checklistTemplateId());
        if (!"PUBLISHED".equals(checklist.status())) {
            throw new IllegalStateException("Checklist template must be published");
        }
        if (!checklist.templateVersion().equals(command.templateVersion())) {
            throw new IllegalArgumentException("Checklist template version mismatch");
        }
        if (checklist.equipmentTypeId() != null && job.getEquipmentId() != null) {
            EquipmentReference equipment = equipmentReferences.requireEquipment(job.getEquipmentId());
            if (!checklist.equipmentTypeId().equals(equipment.equipmentTypeId())) {
                throw new IllegalArgumentException("Checklist template does not match equipment type");
            }
        }
        if (checklist.jobTypeId() != null && !checklist.jobTypeId().equals(job.getJobTypeId())) {
            throw new IllegalArgumentException("Checklist template does not match job type");
        }
        if (executionRepository.existsByBusinessIdAndVisitIdAndStatusIn(
                tenant(), visit.getId(), List.of("IN_PROGRESS", "COMPLETED"))) {
            throw new IllegalArgumentException("Visit already has a checklist execution");
        }
        JobExecution entity = mapper.toEntity(command);
        entity.setBusinessId(tenant());
        entity.setStartedAt(Instant.now());
        return mapper.toResponse(executionRepository.save(entity));
    }

    @Override
    @Transactional
    public JobExecutionResponse completeExecution(UUID executionId, long version, String answersJson) {
        JobExecution entity = requireExecutionEntity(executionId);
        checkVersion("JobExecution", executionId, version, entity.getVersion());
        if (!"IN_PROGRESS".equals(entity.getStatus())) {
            throw new IllegalStateException("Only IN_PROGRESS checklist executions can be completed");
        }
        if (answersJson == null || answersJson.isBlank()) {
            throw new IllegalArgumentException("Checklist answers are required");
        }
        entity.setAnswersJson(answersJson);
        entity.setCompletedAt(Instant.now());
        entity.setCompletedBy(tenantContext.requireUserId());
        entity.setStatus("COMPLETED");
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public WorkReportResponse createWorkReport(WorkReportCreateCommand command) {
        ServiceJob job = requireJobEntity(command.jobId());
        if (ServiceJobStatus.CLOSED.name().equals(job.getStatus())
                || ServiceJobStatus.CANCELLED.name().equals(job.getStatus())) {
            throw new IllegalStateException("Cannot create a work report for a terminal job");
        }
        if (command.visitId() != null) {
            JobVisit visit = requireVisitEntity(command.visitId());
            if (!visit.getJobId().equals(job.getId())) {
                throw new IllegalArgumentException("Work report visit belongs to another job");
            }
            if (!JobVisitStatus.COMPLETED.name().equals(visit.getStatus())) {
                throw new IllegalStateException("Work report requires a completed visit");
            }
        }
        if (command.jobExecutionId() != null) {
            JobExecution execution = requireExecutionEntity(command.jobExecutionId());
            if (!execution.getJobId().equals(job.getId())) {
                throw new IllegalArgumentException("Work report execution belongs to another job");
            }
            if (!"COMPLETED".equals(execution.getStatus())) {
                throw new IllegalStateException("Work report requires a completed checklist execution");
            }
            if (reportRepository.existsByBusinessIdAndJobExecutionId(tenant(), execution.getId())) {
                throw new IllegalArgumentException("Checklist execution already has a work report");
            }
        }
        if (reportRepository.existsByBusinessIdAndJobId(tenant(), job.getId())) {
            throw new IllegalArgumentException("Service job already has a work report");
        }
        WorkReport entity = mapper.toEntity(command);
        entity.setBusinessId(tenant());
        entity.setCompletedBy(tenantContext.requireUserId());
        entity.setCompletedAt(Instant.now());
        return mapper.toResponse(reportRepository.save(entity));
    }

    @Override
    @Transactional
    public WorkReportResponse approveWorkReport(UUID reportId, long version) {
        WorkReport entity = requireReportEntity(reportId);
        checkVersion("WorkReport", reportId, version, entity.getVersion());
        if (!"DRAFT".equals(entity.getStatus())) {
            throw new IllegalStateException("Only DRAFT work reports can be approved");
        }
        entity.setStatus("APPROVED");
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceJobReference requireJob(UUID jobId) {
        ServiceJob entity = requireJobEntity(jobId);
        return new ServiceJobReference(
                entity.getId(), entity.getCustomerId(), entity.getServiceLocationId(),
                entity.getEquipmentId(), entity.getJobTypeId(), entity.getStatus());
    }

    private void validateCustomerGraph(UUID customerId, UUID locationId, UUID equipmentId) {
        CustomerReference customer = customerReferences.requireCustomer(customerId);
        requireActive(customer.status(), "Customer");
        ServiceLocationReference location = customerReferences.requireServiceLocation(locationId);
        if (!location.customerId().equals(customerId)) {
            throw new IllegalArgumentException("Service location belongs to another customer");
        }
        requireActive(location.status(), "ServiceLocation");
        if (equipmentId != null) {
            EquipmentReference equipment = equipmentReferences.requireEquipment(equipmentId);
            requireActive(equipment.status(), "Equipment");
            if (!equipment.customerId().equals(customerId)) {
                throw new IllegalArgumentException("Equipment belongs to another customer");
            }
            if (!equipment.serviceLocationId().equals(locationId)) {
                throw new IllegalArgumentException("Equipment belongs to another service location");
            }
        }
    }

    private static void validateRequestMatchesJob(ServiceRequest request, ServiceJobCreateCommand command) {
        if (!request.getCustomerId().equals(command.customerId())
                || !request.getServiceLocationId().equals(command.serviceLocationId())
                || !java.util.Objects.equals(request.getEquipmentId(), command.equipmentId())) {
            throw new IllegalArgumentException("Service job does not match service request customer graph");
        }
    }

    private void transitionRequest(
            ServiceRequest entity,
            long version,
            ServiceRequestStatus from,
            ServiceRequestStatus to
    ) {
        checkVersion("ServiceRequest", entity.getId(), version, entity.getVersion());
        if (!from.name().equals(entity.getStatus())) {
            throw new IllegalStateException("Invalid service request transition: " + entity.getStatus() + " -> " + to);
        }
        entity.setStatus(to.name());
    }

    private void transitionJob(ServiceJob entity, long version, ServiceJobStatus from, ServiceJobStatus to) {
        checkVersion("ServiceJob", entity.getId(), version, entity.getVersion());
        if (!from.name().equals(entity.getStatus())) {
            throw new IllegalStateException("Invalid service job transition: " + entity.getStatus() + " -> " + to);
        }
        entity.setStatus(to.name());
    }

    private static void requireNonTerminalRequest(String status) {
        if (ServiceRequestStatus.CONVERTED_TO_JOB.name().equals(status)
                || ServiceRequestStatus.CLOSED.name().equals(status)
                || ServiceRequestStatus.REJECTED.name().equals(status)
                || ServiceRequestStatus.CANCELLED.name().equals(status)) {
            throw new IllegalStateException("Terminal service request cannot be cancelled");
        }
    }

    private UUID tenant() {
        return tenantContext.requireBusinessId();
    }

    private ServiceRequest requireRequestEntity(UUID id) {
        return requestRepository.findByIdAndBusinessId(id, tenant())
                .orElseThrow(() -> new EntityNotFoundException("ServiceRequest", id));
    }

    private ServiceJob requireJobEntity(UUID id) {
        return jobRepository.findByIdAndBusinessId(id, tenant())
                .orElseThrow(() -> new EntityNotFoundException("ServiceJob", id));
    }

    private JobVisit requireVisitEntity(UUID id) {
        return visitRepository.findByIdAndBusinessId(id, tenant())
                .orElseThrow(() -> new EntityNotFoundException("JobVisit", id));
    }

    private JobExecution requireExecutionEntity(UUID id) {
        return executionRepository.findByIdAndBusinessId(id, tenant())
                .orElseThrow(() -> new EntityNotFoundException("JobExecution", id));
    }

    private WorkReport requireReportEntity(UUID id) {
        return reportRepository.findByIdAndBusinessId(id, tenant())
                .orElseThrow(() -> new EntityNotFoundException("WorkReport", id));
    }

    private static void checkVersion(String name, UUID id, long expected, long actual) {
        if (expected != actual) {
            throw new StaleEntityException(name, id, expected, actual);
        }
    }

    private static void requireActive(String status, String entity) {
        if (!"ACTIVE".equals(status)) {
            throw new IllegalStateException(entity + " is not active");
        }
    }
}
