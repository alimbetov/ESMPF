package com.esmpf.service.domain;

import com.esmpf.shared.security.SecurityExecutionContext;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class DeviceAccessPolicy {

    private final SecurityExecutionContext executionContext;

    public void requireAccessToUser(UUID ownerUserId) {
        if (executionContext.requireExecutionKind() == SecurityExecutionContext.ExecutionKind.SYSTEM) {
            return;
        }
        var principal = executionContext.currentUserPrincipal()
                .orElseThrow(() -> new AccessDeniedException("Authenticated ESMPF principal is required"));
        if (principal.userId().equals(ownerUserId)) {
            if (principal.hasPermission("DEVICE_SELF_MANAGE") || principal.hasPermission("DEVICE_ADMIN")) {
                return;
            }
        } else if (principal.hasPermission("DEVICE_ADMIN")) {
            return;
        }
        throw new AccessDeniedException("Mobile device is outside the authenticated user's object scope");
    }
}
