package com.esmpf.maintenance;

import static com.esmpf.maintenance.MaintenanceDtos.*;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MaintenanceService {
    MaintenancePlanResponse createPlan(MaintenancePlanCreateCommand command);
    MaintenancePlanResponse getPlan(UUID id);
    Page<MaintenancePlanResponse> listPlans(Pageable pageable);
    MaintenancePlanResponse updatePlan(UUID id, MaintenancePlanUpdateCommand command);
    MaintenancePlanResponse activatePlan(UUID id, long version);
    MaintenancePlanResponse suspendPlan(UUID id, long version);
    MaintenancePlanResponse closePlan(UUID id, long version);

    MaintenanceOccurrenceResponse generateOccurrence(MaintenanceOccurrenceCreateCommand command);
    MaintenanceOccurrenceResponse getOccurrence(UUID id);
    Page<MaintenanceOccurrenceResponse> listOccurrences(UUID planId, Pageable pageable);
    MaintenanceOccurrenceResponse linkServiceJob(UUID occurrenceId, long version, UUID serviceJobId);
    MaintenanceOccurrenceResponse completeOccurrence(UUID occurrenceId, long version);
    MaintenanceOccurrenceResponse cancelOccurrence(UUID occurrenceId, long version, String reason);
}
