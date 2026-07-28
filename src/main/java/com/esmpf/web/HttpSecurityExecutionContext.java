package com.esmpf.web;

import com.esmpf.shared.security.EsmpfPrincipal;
import com.esmpf.shared.security.SecurityAccessException;
import com.esmpf.shared.security.SecurityExecutionContext;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
final class HttpSecurityExecutionContext implements SecurityExecutionContext {

    @Override
    public ExecutionKind requireExecutionKind() {
        return ExecutionKind.USER;
    }

    @Override
    public Optional<EsmpfPrincipal> currentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        if (!(authentication.getPrincipal() instanceof EsmpfPrincipal principal)) {
            throw new SecurityAccessException("Trusted ESMPF principal is required");
        }
        return Optional.of(principal);
    }
}
