package com.esmpf.identity;

import static com.esmpf.identity.IdentityDtos.*;
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
public class IdentityRestController {
    private final IdentityService service;

    @PostMapping("/business")
    @ResponseStatus(HttpStatus.CREATED)
    public BusinessResponse createBusiness(@Valid @RequestBody BusinessCreateCommand command) {
        return service.createBusiness(command);
    }

    @GetMapping("/business")
    public BusinessResponse getCurrentBusiness() {
        return service.getCurrentBusiness();
    }

    @PutMapping("/business")
    public BusinessResponse updateCurrentBusiness(@Valid @RequestBody BusinessUpdateCommand command) {
        return service.updateCurrentBusiness(command);
    }

    @PostMapping("/business/actions/activate")
    public BusinessResponse activateCurrentBusiness(@Valid @RequestBody VersionRequest request) {
        return service.activateCurrentBusiness(request.version());
    }

    @PostMapping("/business/actions/suspend")
    public BusinessResponse suspendCurrentBusiness(@Valid @RequestBody VersionRequest request) {
        return service.suspendCurrentBusiness(request.version());
    }

    @PostMapping("/business/locations")
    @ResponseStatus(HttpStatus.CREATED)
    public BusinessLocationResponse createLocation(@Valid @RequestBody BusinessLocationCommand command) {
        return service.createLocation(command);
    }

    @GetMapping("/business/locations/{locationId}")
    public BusinessLocationResponse getLocation(@PathVariable UUID locationId) {
        return service.getLocation(locationId);
    }

    @GetMapping("/business/locations")
    public Page<BusinessLocationResponse> listLocations(Pageable pageable) {
        return service.listLocations(pageable);
    }

    @PutMapping("/business/locations/{locationId}")
    public BusinessLocationResponse updateLocation(
            @PathVariable UUID locationId,
            @Valid @RequestBody BusinessLocationCommand command
    ) {
        return service.updateLocation(locationId, command);
    }

    @PostMapping("/business/locations/{locationId}/actions/activate")
    public BusinessLocationResponse activateLocation(
            @PathVariable UUID locationId,
            @Valid @RequestBody VersionRequest request
    ) {
        return service.activateLocation(locationId, request.version());
    }

    @PostMapping("/business/locations/{locationId}/actions/deactivate")
    public BusinessLocationResponse deactivateLocation(
            @PathVariable UUID locationId,
            @Valid @RequestBody VersionRequest request
    ) {
        return service.deactivateLocation(locationId, request.version());
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserAccountResponse createUser(@Valid @RequestBody UserAccountCreateCommand command) {
        return service.createUser(command);
    }

    @GetMapping("/users/{userId}")
    public UserAccountResponse getUser(@PathVariable UUID userId) {
        return service.getUser(userId);
    }

    @GetMapping("/users")
    public Page<UserAccountResponse> listUsers(Pageable pageable) {
        return service.listUsers(pageable);
    }

    @PutMapping("/users/{userId}")
    public UserAccountResponse updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UserAccountUpdateCommand command
    ) {
        return service.updateUser(userId, command);
    }

    @PostMapping("/users/{userId}/actions/activate")
    public UserAccountResponse activateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody VersionRequest request
    ) {
        return service.activateUser(userId, request.version());
    }

    @PostMapping("/users/{userId}/actions/deactivate")
    public UserAccountResponse deactivateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody VersionRequest request
    ) {
        return service.deactivateUser(userId, request.version());
    }

    @PostMapping("/users/{userId}/qualifications")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkerQualificationResponse createQualification(
            @PathVariable UUID userId,
            @Valid @RequestBody WorkerQualificationCommand command
    ) {
        if (!userId.equals(command.userId())) {
            throw new IllegalArgumentException("path userId must match command userId");
        }
        return service.createQualification(command);
    }

    @GetMapping("/qualifications/{qualificationId}")
    public WorkerQualificationResponse getQualification(@PathVariable UUID qualificationId) {
        return service.getQualification(qualificationId);
    }

    @GetMapping("/users/{userId}/qualifications")
    public Page<WorkerQualificationResponse> listQualifications(
            @PathVariable UUID userId,
            Pageable pageable
    ) {
        return service.listQualifications(userId, pageable);
    }

    @PutMapping("/qualifications/{qualificationId}")
    public WorkerQualificationResponse updateQualification(
            @PathVariable UUID qualificationId,
            @Valid @RequestBody WorkerQualificationCommand command
    ) {
        return service.updateQualification(qualificationId, command);
    }

    @PostMapping("/qualifications/{qualificationId}/actions/expire")
    public WorkerQualificationResponse expireQualification(
            @PathVariable UUID qualificationId,
            @Valid @RequestBody VersionRequest request
    ) {
        return service.expireQualification(qualificationId, request.version());
    }
}
