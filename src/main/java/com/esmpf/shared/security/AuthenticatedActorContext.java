package com.esmpf.shared.security;

import java.util.UUID;

/**
 * Provides the authenticated server-side principal for the current execution.
 */
public interface AuthenticatedActorContext {

    AuthenticatedActor requireActor();

    default UUID requireUserId() {
        return requireActor().userId();
    }
}
