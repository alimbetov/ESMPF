package com.esmpf.maintenance;

import static com.esmpf.maintenance.MaintenanceDtos.MaintenanceOccurrenceReference;
import static com.esmpf.maintenance.MaintenanceDtos.MaintenancePlanReference;

import java.util.UUID;

public interface MaintenanceReferenceQuery {
    MaintenancePlanReference requirePlan(UUID planId);
    MaintenanceOccurrenceReference requireOccurrence(UUID occurrenceId);
}
