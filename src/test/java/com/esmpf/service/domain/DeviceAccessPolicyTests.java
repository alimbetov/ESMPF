package com.esmpf.service.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.esmpf.shared.security.EsmpfPrincipal;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class DeviceAccessPolicyTests {
    private final DeviceAccessPolicy policy = new DeviceAccessPolicy();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void selfPermissionAllowsOnlyOwnDeviceOwner() {
        UUID actorId = UUID.randomUUID();
        authenticate(actorId, Set.of("DEVICE_SELF_MANAGE"));

        assertDoesNotThrow(() -> policy.requireAccessToUser(actorId));
        assertThrows(AccessDeniedException.class,
                () -> policy.requireAccessToUser(UUID.randomUUID()));
    }

    @Test
    void deviceAdminCanAccessAnotherUsersDevice() {
        UUID actorId = UUID.randomUUID();
        authenticate(actorId, Set.of("DEVICE_ADMIN"));

        assertDoesNotThrow(() -> policy.requireAccessToUser(UUID.randomUUID()));
    }

    private static void authenticate(UUID userId, Set<String> permissions) {
        EsmpfPrincipal principal = new EsmpfPrincipal(
                userId, UUID.randomUUID(), Set.of("TECHNICIAN"), permissions);
        var authorities = permissions.stream().map(SimpleGrantedAuthority::new).toList();
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal, null, authorities));
    }
}
