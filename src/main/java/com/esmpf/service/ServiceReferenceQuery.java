package com.esmpf.service;

import static com.esmpf.service.ServiceManagementDtos.ServiceJobReference;

import java.util.UUID;

public interface ServiceReferenceQuery {
    ServiceJobReference requireJob(UUID jobId);
}
