package com.esmpf.maintenance;

import static com.esmpf.maintenance.MaintenanceDtos.*;
import static com.esmpf.web.ApiActionRequests.ReasonRequest;
import static com.esmpf.web.ApiActionRequests.ReferenceRequest;
import static com.esmpf.web.ApiActionRequests.VersionRequest;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MaintenanceRestController {
    private final MaintenanceService service;

    @PostMapping("/maintenance-plans") @ResponseStatus(HttpStatus.CREATED)
    public MaintenancePlanResponse createPlan(@Valid @RequestBody MaintenancePlanCreateCommand command) { return service.createPlan(command); }
    @GetMapping("/maintenance-plans/{id}") public MaintenancePlanResponse getPlan(@PathVariable UUID id) { return service.getPlan(id); }
    @GetMapping("/maintenance-plans") public Page<MaintenancePlanResponse> listPlans(Pageable pageable) { return service.listPlans(pageable); }
    @PutMapping("/maintenance-plans/{id}") public MaintenancePlanResponse updatePlan(@PathVariable UUID id, @Valid @RequestBody MaintenancePlanUpdateCommand command) { return service.updatePlan(id, command); }
    @PostMapping("/maintenance-plans/{id}/actions/activate") public MaintenancePlanResponse activatePlan(@PathVariable UUID id, @Valid @RequestBody VersionRequest request) { return service.activatePlan(id, request.version()); }
    @PostMapping("/maintenance-plans/{id}/actions/suspend") public MaintenancePlanResponse suspendPlan(@PathVariable UUID id, @Valid @RequestBody VersionRequest request) { return service.suspendPlan(id, request.version()); }
    @PostMapping("/maintenance-plans/{id}/actions/close") public MaintenancePlanResponse closePlan(@PathVariable UUID id, @Valid @RequestBody VersionRequest request) { return service.closePlan(id, request.version()); }

    @PostMapping("/maintenance-occurrences") @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceOccurrenceResponse generateOccurrence(@Valid @RequestBody MaintenanceOccurrenceCreateCommand command) { return service.generateOccurrence(command); }
    @GetMapping("/maintenance-occurrences/{id}") public MaintenanceOccurrenceResponse getOccurrence(@PathVariable UUID id) { return service.getOccurrence(id); }
    @GetMapping("/maintenance-plans/{planId}/occurrences") public Page<MaintenanceOccurrenceResponse> listOccurrences(@PathVariable UUID planId, Pageable pageable) { return service.listOccurrences(planId, pageable); }
    @PostMapping("/maintenance-occurrences/{occurrenceId}/actions/link-service-job") public MaintenanceOccurrenceResponse linkServiceJob(@PathVariable UUID occurrenceId, @Valid @RequestBody ReferenceRequest request) { return service.linkServiceJob(occurrenceId, request.version(), request.referenceId()); }
    @PostMapping("/maintenance-occurrences/{occurrenceId}/actions/complete") public MaintenanceOccurrenceResponse completeOccurrence(@PathVariable UUID occurrenceId, @Valid @RequestBody VersionRequest request) { return service.completeOccurrence(occurrenceId, request.version()); }
    @PostMapping("/maintenance-occurrences/{occurrenceId}/actions/cancel") public MaintenanceOccurrenceResponse cancelOccurrence(@PathVariable UUID occurrenceId, @Valid @RequestBody ReasonRequest request) { return service.cancelOccurrence(occurrenceId, request.version(), request.reason()); }
}
