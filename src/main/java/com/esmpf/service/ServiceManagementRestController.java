package com.esmpf.service;

import static com.esmpf.service.ServiceManagementDtos.*;
import static com.esmpf.web.ApiActionRequests.JsonRequest;
import static com.esmpf.web.ApiActionRequests.ReasonRequest;
import static com.esmpf.web.ApiActionRequests.VersionRequest;
import static com.esmpf.web.ApiActionRequests.VisitCompleteRequest;
import static com.esmpf.web.ApiActionRequests.VisitStartRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ServiceManagementRestController {
    private final ServiceManagementService service;

    @PostMapping("/service-requests") @ResponseStatus(HttpStatus.CREATED)
    public ServiceRequestResponse createRequest(@Valid @RequestBody ServiceRequestCreateCommand command) { return service.createRequest(command); }
    @GetMapping("/service-requests/{requestId}") public ServiceRequestResponse getRequest(@PathVariable UUID requestId) { return service.getRequest(requestId); }
    @GetMapping("/service-requests") public Page<ServiceRequestResponse> listRequests(Pageable pageable) { return service.listRequests(pageable); }
    @PostMapping("/service-requests/{requestId}/actions/triage") public ServiceRequestResponse triageRequest(@PathVariable UUID requestId, @Valid @RequestBody VersionRequest request) { return service.triageRequest(requestId, request.version()); }
    @PostMapping("/service-requests/{requestId}/actions/accept") public ServiceRequestResponse acceptRequest(@PathVariable UUID requestId, @Valid @RequestBody VersionRequest request) { return service.acceptRequest(requestId, request.version()); }
    @PostMapping("/service-requests/{requestId}/actions/reject") public ServiceRequestResponse rejectRequest(@PathVariable UUID requestId, @Valid @RequestBody VersionRequest request) { return service.rejectRequest(requestId, request.version()); }
    @PostMapping("/service-requests/{requestId}/actions/cancel") public ServiceRequestResponse cancelRequest(@PathVariable UUID requestId, @Valid @RequestBody VersionRequest request) { return service.cancelRequest(requestId, request.version()); }

    @PostMapping("/service-jobs") @ResponseStatus(HttpStatus.CREATED)
    public ServiceJobResponse createJob(@Valid @RequestBody ServiceJobCreateCommand command) { return service.createJob(command); }
    @PostMapping("/service-requests/{requestId}/actions/convert-to-job") @ResponseStatus(HttpStatus.CREATED)
    public ServiceJobResponse convertRequestToJob(
            @PathVariable UUID requestId,
            @Valid @RequestBody ConvertRequestToJobRequest request
    ) {
        return service.convertRequestToJob(requestId, request.requestVersion(), request.job());
    }
    @GetMapping("/service-jobs/{jobId}") public ServiceJobResponse getJob(@PathVariable UUID jobId) { return service.getJob(jobId); }
    @GetMapping("/service-jobs") public Page<ServiceJobResponse> listJobs(Pageable pageable) { return service.listJobs(pageable); }
    @PostMapping("/service-jobs/{jobId}/actions/mark-ready") public ServiceJobResponse markJobReady(@PathVariable UUID jobId, @Valid @RequestBody VersionRequest request) { return service.markJobReady(jobId, request.version()); }
    @PostMapping("/service-jobs/{jobId}/actions/schedule") public ServiceJobResponse scheduleJob(@PathVariable UUID jobId, @Valid @RequestBody ServiceJobScheduleCommand command) { return service.scheduleJob(jobId, command); }
    @PostMapping("/service-jobs/{jobId}/actions/start") public ServiceJobResponse startJob(@PathVariable UUID jobId, @Valid @RequestBody VersionRequest request) { return service.startJob(jobId, request.version()); }
    @PostMapping("/service-jobs/{jobId}/actions/hold") public ServiceJobResponse holdJob(@PathVariable UUID jobId, @Valid @RequestBody ReasonRequest request) { return service.holdJob(jobId, request.version(), request.reason()); }
    @PostMapping("/service-jobs/{jobId}/actions/resume") public ServiceJobResponse resumeJob(@PathVariable UUID jobId, @Valid @RequestBody VersionRequest request) { return service.resumeJob(jobId, request.version()); }
    @PostMapping("/service-jobs/{jobId}/actions/complete") public ServiceJobResponse completeJob(@PathVariable UUID jobId, @Valid @RequestBody VersionRequest request) { return service.completeJob(jobId, request.version()); }
    @PostMapping("/service-jobs/{jobId}/actions/close") public ServiceJobResponse closeJob(@PathVariable UUID jobId, @Valid @RequestBody VersionRequest request) { return service.closeJob(jobId, request.version()); }
    @PostMapping("/service-jobs/{jobId}/actions/cancel") public ServiceJobResponse cancelJob(@PathVariable UUID jobId, @Valid @RequestBody ReasonRequest request) { return service.cancelJob(jobId, request.version(), request.reason()); }

    @PostMapping("/job-visits") @ResponseStatus(HttpStatus.CREATED)
    public JobVisitResponse planVisit(@Valid @RequestBody JobVisitPlanCommand command) { return service.planVisit(command); }
    @PostMapping("/job-visits/{visitId}/actions/start") public JobVisitResponse startVisit(@PathVariable UUID visitId, @Valid @RequestBody VisitStartRequest request) { return service.startVisit(visitId, request.version(), request.arrivalDataJson()); }
    @PostMapping("/job-visits/{visitId}/actions/complete") public JobVisitResponse completeVisit(@PathVariable UUID visitId, @Valid @RequestBody VisitCompleteRequest request) { return service.completeVisit(visitId, request.version(), request.completionDataJson(), request.customerConfirmationJson()); }
    @PostMapping("/job-visits/{visitId}/actions/cancel") public JobVisitResponse cancelVisit(@PathVariable UUID visitId, @Valid @RequestBody VersionRequest request) { return service.cancelVisit(visitId, request.version()); }
    @GetMapping("/service-jobs/{jobId}/visits") public Page<JobVisitResponse> listVisits(@PathVariable UUID jobId, Pageable pageable) { return service.listVisits(jobId, pageable); }

    @PostMapping("/job-executions") @ResponseStatus(HttpStatus.CREATED)
    public JobExecutionResponse startExecution(@Valid @RequestBody JobExecutionStartCommand command) { return service.startExecution(command); }
    @PostMapping("/job-executions/{executionId}/actions/complete") public JobExecutionResponse completeExecution(@PathVariable UUID executionId, @Valid @RequestBody JsonRequest request) { return service.completeExecution(executionId, request.version(), request.dataJson()); }

    @PostMapping("/work-reports") @ResponseStatus(HttpStatus.CREATED)
    public WorkReportResponse createWorkReport(@Valid @RequestBody WorkReportCreateCommand command) { return service.createWorkReport(command); }
    @PostMapping("/work-reports/{reportId}/actions/approve") public WorkReportResponse approveWorkReport(@PathVariable UUID reportId, @Valid @RequestBody VersionRequest request) { return service.approveWorkReport(reportId, request.version()); }

    public record ConvertRequestToJobRequest(
            @Min(0) long requestVersion,
            @NotNull @Valid ServiceJobCreateCommand job
    ) {}
}
