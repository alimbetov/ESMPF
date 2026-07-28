package com.esmpf.identity.domain;

import static com.esmpf.identity.IdentityDtos.*;

import com.esmpf.identity.IdentityReferenceQuery;
import com.esmpf.identity.IdentityService;
import com.esmpf.shared.exception.EntityNotFoundException;
import com.esmpf.shared.exception.StaleEntityException;
import com.esmpf.shared.tenant.TenantContext;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class IdentityServiceImpl implements IdentityService, IdentityReferenceQuery {
    private final TenantContext tenantContext;
    private final BusinessRepository businessRepository;
    private final BusinessLocationRepository locationRepository;
    private final UserAccountRepository userRepository;
    private final WorkerQualificationRepository qualificationRepository;
    private final IdentityMapper mapper;

    @Override @Transactional
    public BusinessResponse createBusiness(BusinessCreateCommand command) {
        requireText(command.code(), "code");
        if (businessRepository.existsByCodeIgnoreCase(command.code())) {
            throw new IllegalArgumentException("Business code already exists");
        }
        return mapper.toResponse(businessRepository.saveAndFlush(mapper.toEntity(command)));
    }

    @Override @Transactional(readOnly = true)
    public BusinessResponse getCurrentBusiness() { return mapper.toResponse(requireBusiness()); }

    @Override @Transactional
    public BusinessResponse updateCurrentBusiness(BusinessUpdateCommand command) {
        Business entity = requireBusiness();
        checkVersion("Business", entity.getId(), command.version(), entity.getVersion());
        mapper.update(command, entity);
        return mapper.toResponse(businessRepository.saveAndFlush(entity));
    }

    @Override @Transactional
    public BusinessResponse activateCurrentBusiness(long version) { return transitionBusiness(version, "ACTIVE"); }

    @Override @Transactional
    public BusinessResponse suspendCurrentBusiness(long version) { return transitionBusiness(version, "SUSPENDED"); }

    @Override @Transactional
    public BusinessLocationResponse createLocation(BusinessLocationCommand command) {
        validateCoordinates(command.latitude(), command.longitude());
        BusinessLocation entity = mapper.toEntity(command);
        entity.setBusinessId(tenant());
        return mapper.toResponse(locationRepository.saveAndFlush(entity));
    }

    @Override @Transactional(readOnly = true)
    public BusinessLocationResponse getLocation(UUID locationId) { return mapper.toResponse(requireLocation(locationId)); }

    @Override @Transactional(readOnly = true)
    public Page<BusinessLocationResponse> listLocations(Pageable pageable) {
        return locationRepository.findAllByBusinessId(tenant(), pageable).map(mapper::toResponse);
    }

    @Override @Transactional
    public BusinessLocationResponse updateLocation(UUID locationId, BusinessLocationCommand command) {
        BusinessLocation entity = requireLocation(locationId);
        checkVersion("BusinessLocation", locationId, command.version(), entity.getVersion());
        validateCoordinates(command.latitude(), command.longitude());
        mapper.update(command, entity);
        return mapper.toResponse(locationRepository.saveAndFlush(entity));
    }

    @Override @Transactional
    public BusinessLocationResponse activateLocation(UUID locationId, long version) {
        return setLocationActive(locationId, version, true);
    }

    @Override @Transactional
    public BusinessLocationResponse deactivateLocation(UUID locationId, long version) {
        return setLocationActive(locationId, version, false);
    }

    @Override @Transactional
    public UserAccountResponse createUser(UserAccountCreateCommand command) {
        validateUserEmail(null, command.email());
        UserAccount entity = mapper.toEntity(command);
        entity.setBusinessId(tenant());
        return mapper.toResponse(userRepository.saveAndFlush(entity));
    }

    @Override @Transactional(readOnly = true)
    public UserAccountResponse getUser(UUID userId) { return mapper.toResponse(requireUserEntity(userId)); }

    @Override @Transactional(readOnly = true)
    public Page<UserAccountResponse> listUsers(Pageable pageable) {
        return userRepository.findAllByBusinessId(tenant(), pageable).map(mapper::toResponse);
    }

    @Override @Transactional
    public UserAccountResponse updateUser(UUID userId, UserAccountUpdateCommand command) {
        UserAccount entity = requireUserEntity(userId);
        checkVersion("UserAccount", userId, command.version(), entity.getVersion());
        validateUserEmail(userId, command.email());
        mapper.update(command, entity);
        return mapper.toResponse(userRepository.saveAndFlush(entity));
    }

    @Override @Transactional
    public UserAccountResponse activateUser(UUID userId, long version) { return setUserActive(userId, version, true); }

    @Override @Transactional
    public UserAccountResponse deactivateUser(UUID userId, long version) { return setUserActive(userId, version, false); }

    @Override @Transactional
    public WorkerQualificationResponse createQualification(WorkerQualificationCommand command) {
        UserAccount user = requireUserEntity(command.userId());
        if (!Boolean.TRUE.equals(user.getWorker())) {
            throw new IllegalArgumentException("Qualification requires a worker user");
        }
        validateDateRange(command.validFrom(), command.validUntil());
        WorkerQualification entity = mapper.toEntity(command);
        entity.setBusinessId(tenant());
        return mapper.toResponse(qualificationRepository.saveAndFlush(entity));
    }

    @Override @Transactional(readOnly = true)
    public WorkerQualificationResponse getQualification(UUID qualificationId) {
        return mapper.toResponse(requireQualification(qualificationId));
    }

    @Override @Transactional(readOnly = true)
    public Page<WorkerQualificationResponse> listQualifications(UUID userId, Pageable pageable) {
        requireUserEntity(userId);
        return qualificationRepository.findAllByBusinessIdAndUserId(tenant(), userId, pageable)
                .map(mapper::toResponse);
    }

    @Override @Transactional
    public WorkerQualificationResponse updateQualification(UUID qualificationId, WorkerQualificationCommand command) {
        WorkerQualification entity = requireQualification(qualificationId);
        checkVersion("WorkerQualification", qualificationId, command.version(), entity.getVersion());
        if (command.userId() != null && !command.userId().equals(entity.getUserId())) {
            throw new IllegalArgumentException("Qualification owner cannot be changed");
        }
        validateDateRange(command.validFrom(), command.validUntil());
        mapper.update(command, entity);
        return mapper.toResponse(qualificationRepository.saveAndFlush(entity));
    }

    @Override @Transactional
    public WorkerQualificationResponse expireQualification(UUID qualificationId, long version) {
        WorkerQualification entity = requireQualification(qualificationId);
        checkVersion("WorkerQualification", qualificationId, version, entity.getVersion());
        if ("EXPIRED".equals(entity.getStatus())) {
            throw new IllegalStateException("Qualification already expired");
        }
        entity.setStatus("EXPIRED");
        return mapper.toResponse(qualificationRepository.saveAndFlush(entity));
    }

    @Override @Transactional(readOnly = true)
    public UserReference requireUser(UUID userId) {
        UserAccount user = requireUserEntity(userId);
        return new UserReference(
                user.getId(),
                Boolean.TRUE.equals(user.getActive()),
                Boolean.TRUE.equals(user.getWorker())
        );
    }

    private BusinessResponse transitionBusiness(long version, String status) {
        Business entity = requireBusiness();
        checkVersion("Business", entity.getId(), version, entity.getVersion());
        if (status.equals(entity.getStatus())) {
            throw new IllegalStateException("Business is already " + status);
        }
        entity.setStatus(status);
        return mapper.toResponse(businessRepository.saveAndFlush(entity));
    }

    private BusinessLocationResponse setLocationActive(UUID id, long version, boolean active) {
        BusinessLocation entity = requireLocation(id);
        checkVersion("BusinessLocation", id, version, entity.getVersion());
        if (Boolean.TRUE.equals(entity.getActive()) == active) {
            throw new IllegalStateException(active ? "Location already active" : "Location already inactive");
        }
        entity.setActive(active);
        return mapper.toResponse(locationRepository.saveAndFlush(entity));
    }

    private UserAccountResponse setUserActive(UUID id, long version, boolean active) {
        UserAccount entity = requireUserEntity(id);
        checkVersion("UserAccount", id, version, entity.getVersion());
        if (Boolean.TRUE.equals(entity.getActive()) == active) {
            throw new IllegalStateException(active ? "User already active" : "User already inactive");
        }
        entity.setActive(active);
        return mapper.toResponse(userRepository.saveAndFlush(entity));
    }

    private Business requireBusiness() {
        return businessRepository.findById(tenant())
                .orElseThrow(() -> new EntityNotFoundException("Business", tenant()));
    }

    private BusinessLocation requireLocation(UUID id) {
        return locationRepository.findByIdAndBusinessId(id, tenant())
                .orElseThrow(() -> new EntityNotFoundException("BusinessLocation", id));
    }

    private UserAccount requireUserEntity(UUID id) {
        return userRepository.findByIdAndBusinessId(id, tenant())
                .orElseThrow(() -> new EntityNotFoundException("UserAccount", id));
    }

    private WorkerQualification requireQualification(UUID id) {
        return qualificationRepository.findByIdAndBusinessId(id, tenant())
                .orElseThrow(() -> new EntityNotFoundException("WorkerQualification", id));
    }

    private UUID tenant() { return tenantContext.requireBusinessId(); }

    private void validateUserEmail(UUID id, String email) {
        if (email != null && (id == null
                ? userRepository.existsByBusinessIdAndEmailIgnoreCase(tenant(), email)
                : userRepository.existsByBusinessIdAndEmailIgnoreCaseAndIdNot(tenant(), email, id))) {
            throw new IllegalArgumentException("Email already exists in tenant");
        }
    }

    private static void validateCoordinates(Double latitude, Double longitude) {
        if ((latitude == null) != (longitude == null)) {
            throw new IllegalArgumentException("Latitude and longitude must be supplied together");
        }
        if (latitude != null && (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180)) {
            throw new IllegalArgumentException("Invalid coordinates");
        }
    }

    private static void validateDateRange(java.time.LocalDate from, java.time.LocalDate until) {
        if (from != null && until != null && until.isBefore(from)) {
            throw new IllegalArgumentException("validUntil must not be before validFrom");
        }
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }

    private static void checkVersion(String type, UUID id, long expected, long actual) {
        if (expected != actual) {
            throw new StaleEntityException(type, id, expected, actual);
        }
    }
}
