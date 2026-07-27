package com.esmpf.shared.security;

import com.esmpf.shared.tenant.TenantContext;
import java.util.UUID;

/**
 * Security-ready replacement contract for anonymous tenant lookup.
 * A future authentication filter will provide the validated actor implementation.
 */
public interface AuthenticatedActorContext extends TenantContext {

    AuthenticatedActor requireActor();

    @Override
    default UUID requireBusinessId() {
        return requireActor().businessId();
    }

    @Override
    default UUID requireUserId() {
        return requireActor().userId();
    }
}
