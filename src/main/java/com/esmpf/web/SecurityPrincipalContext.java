package com.esmpf.web;

import com.esmpf.shared.security.AuthenticatedActor;
import com.esmpf.shared.security.AuthenticatedActorContext;
import com.esmpf.shared.security.EsmpfPrincipal;
import com.esmpf.shared.security.SecurityAccessException;
import com.esmpf.shared.tenant.TenantContext;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
final class SecurityPrincipalContext implements TenantContext, AuthenticatedActorContext {

    @Override
    public UUID requireBusinessId() {
        return requirePrincipal().businessId();
    }

    @Override
    public UUID requireUserId() {
        return requirePrincipal().userId();
    }

    @Override
    public AuthenticatedActor requireActor() {
        return requirePrincipal().actor();
    }

    private EsmpfPrincipal requirePrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof EsmpfPrincipal principal)) {
            throw new SecurityAccessException("Authenticated ESMPF principal is required");
        }
        return principal;
    }
}
