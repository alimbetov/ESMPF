package com.esmpf.shared.security;

import java.util.Optional;

/**
 * Explicit execution boundary for user and trusted system work.
 */
public interface SecurityExecutionContext {

    ExecutionKind requireExecutionKind();

    Optional<EsmpfPrincipal> currentUserPrincipal();

    enum ExecutionKind {
        USER,
        SYSTEM
    }
}
