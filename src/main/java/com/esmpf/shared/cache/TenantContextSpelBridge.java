package com.esmpf.shared.cache;

import com.esmpf.shared.tenant.TenantContext;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component("tenantContext")
public final class TenantContextSpelBridge {

    private final TenantContext delegate;

    public TenantContextSpelBridge(TenantContext delegate) {
        this.delegate = delegate;
    }

    public UUID requireBusinessId() {
        return delegate.requireBusinessId();
    }
}
