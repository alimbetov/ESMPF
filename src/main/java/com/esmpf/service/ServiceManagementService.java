package com.esmpf.service;

import static com.esmpf.service.ServiceManagementDtos.*;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServiceManagementService {
    ServiceRequestResponse createRequest(ServiceRequestCreateCommand command);
    ServiceRequestResponse getRequest(UUID requestId);
    Page<ServiceRequestResponse> listRequests(Pageable pageable);
    ServiceRequestResponse triageRequest(UUID requestId, long version);
    ServiceRequestResponse acceptRequest(UUID requestId, long version);
    ServiceRequestResponse rejectRequest(UUID requestId, long version);
    ServiceRequestResponse cancelRequest(UUID requestId, long version);

    ServiceJobResponse createJob(ServiceJobCreateCommand command);
    ServiceJobResponse convertRequestToJob(UUID requestId, long requestVersion, ServiceJobCreateCommand command);
    ServiceJobResponse getJob(UUID jobId);
    Page<ServiceJobResponse> listJobs(Pageable pageable);
    ServiceJobResponse markJobReady(UUID jobId, long version);
    ServiceJobResponse scheduleJob(UUID jobId, ServiceJobScheduleCommand command);
    ServiceJobResponse startJob(UUID jobId, long version);
    ServiceJobResponse holdJob(UUID jobId, long version, String reason);
    ServiceJobResponse resumeJob(UUID jobId, long version);
    ServiceJobResponse completeJob(UUID jobId, long version);
    ServiceJobResponse closeJob(UUID jobId, long version);
    ServiceJobResponse cancelJob(UUID jobId, long version, String reason);

    JobVisitResponse planVisit(JobVisitPlanCommand command);
    JobVisitResponse startVisit(UUID visitId, long version, String arrivalDataJson);
    JobVisitResponse completeVisit(UUID visitId, long version, String completionDataJson, String customerConfirmationJson);
    JobVisitResponse cancelVisit(UUID visitId, long version);
    Page<JobVisitResponse> listVisits(UUID jobId, Pageable pageable);

    JobExecutionResponse startExecution(JobExecutionStartCommand command);
    JobExecutionResponse completeExecution(UUID executionId, long version, String answersJson);

    WorkReportResponse createWorkReport(WorkReportCreateCommand command);
    WorkReportResponse approveWorkReport(UUID reportId, long version);
}
