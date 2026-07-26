package com.esmpf.shared.tenant;

import java.util.UUID;

/**
 * Trusted tenant identity resolved from the authenticated execution context.
 * API clients must never supply these identifiers as business command fields.
 */
public interface TenantContext {

    UUID requireBusinessId();

    UUID requireUserId();
}
