package com.esmpf.service.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.esmpf.shared.security.EsmpfPrincipal;
import com.esmpf.shared.security.SecurityExecutionContext;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class DeviceAccessPolicyTests {

    @Test
    void selfPermissionAllowsOnlyOwnDeviceOwner() {
        UUID actorId = UUID.randomUUID();
        DeviceAccessPolicy policy = new DeviceAccessPolicy(userContext(actorId, Set.of("DEVICE_SELF_MANAGE")));

        assertDoesNotThrow(() -> policy.requireAccessToUser(actorId));
        assertThrows(AccessDeniedException.class,
                () -> policy.requireAccessToUser(UUID.randomUUID()));
    }

    @Test
    void deviceAdminCanAccessAnotherUsersDevice() {
        UUID actorId = UUID.randomUUID();
        DeviceAccessPolicy policy = new DeviceAccessPolicy(userContext(actorId, Set.of("DEVICE_ADMIN")));

        assertDoesNotThrow(() -> policy.requireAccessToUser(UUID.randomUUID()));
    }

    @Test
    void missingUserPrincipalFailsClosed() {
        DeviceAccessPolicy policy = new DeviceAccessPolicy(new SecurityExecutionContext() {
            @Override public ExecutionKind requireExecutionKind() { return ExecutionKind.USER; }
            @Override public Optional<EsmpfPrincipal> currentUserPrincipal() { return Optional.empty(); }
        });

        assertThrows(AccessDeniedException.class,
                () -> policy.requireAccessToUser(UUID.randomUUID()));
    }

    @Test
    void explicitSystemExecutionIsTrusted() {
        DeviceAccessPolicy policy = new DeviceAccessPolicy(new SecurityExecutionContext() {
            @Override public ExecutionKind requireExecutionKind() { return ExecutionKind.SYSTEM; }
            @Override public Optional<EsmpfPrincipal> currentUserPrincipal() { return Optional.empty(); }
        });

        assertDoesNotThrow(() -> policy.requireAccessToUser(UUID.randomUUID()));
    }

    private static SecurityExecutionContext userContext(UUID userId, Set<String> permissions) {
        EsmpfPrincipal principal = new EsmpfPrincipal(
                userId, UUID.randomUUID(), Set.of("TECHNICIAN"), permissions);
        return new SecurityExecutionContext() {
            @Override public ExecutionKind requireExecutionKind() { return ExecutionKind.USER; }
            @Override public Optional<EsmpfPrincipal> currentUserPrincipal() { return Optional.of(principal); }
        };
    }
}
