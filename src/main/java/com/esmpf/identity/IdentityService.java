package com.esmpf.identity;

import static com.esmpf.identity.IdentityDtos.*;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IdentityService {
    BusinessResponse createBusiness(BusinessCreateCommand command);
    BusinessResponse getCurrentBusiness();
    BusinessResponse updateCurrentBusiness(BusinessUpdateCommand command);
    BusinessResponse activateCurrentBusiness(long version);
    BusinessResponse suspendCurrentBusiness(long version);

    BusinessLocationResponse createLocation(BusinessLocationCommand command);
    BusinessLocationResponse getLocation(UUID locationId);
    Page<BusinessLocationResponse> listLocations(Pageable pageable);
    BusinessLocationResponse updateLocation(UUID locationId, BusinessLocationCommand command);
    BusinessLocationResponse deactivateLocation(UUID locationId, long version);

    UserAccountResponse createUser(UserAccountCreateCommand command);
    UserAccountResponse getUser(UUID userId);
    Page<UserAccountResponse> listUsers(Pageable pageable);
    UserAccountResponse updateUser(UUID userId, UserAccountUpdateCommand command);
    UserAccountResponse activateUser(UUID userId, long version);
    UserAccountResponse deactivateUser(UUID userId, long version);

    WorkerQualificationResponse createQualification(WorkerQualificationCommand command);
    Page<WorkerQualificationResponse> listQualifications(UUID userId, Pageable pageable);
    WorkerQualificationResponse updateQualification(UUID qualificationId, WorkerQualificationCommand command);
    WorkerQualificationResponse expireQualification(UUID qualificationId, long version);
}