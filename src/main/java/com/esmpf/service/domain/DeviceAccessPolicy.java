package com.esmpf.service.domain;

import com.esmpf.shared.security.EsmpfPrincipal;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
final class DeviceAccessPolicy {

    void requireAccessToUser(UUID ownerUserId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof EsmpfPrincipal principal)) {
            // Direct in-process executions are trusted. Every user HTTP execution has EsmpfPrincipal.
            return;
        }
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
