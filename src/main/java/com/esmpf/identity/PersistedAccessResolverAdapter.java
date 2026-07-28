package com.esmpf.identity;

import com.esmpf.shared.security.PersistedAccessResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
final class PersistedAccessResolverAdapter implements PersistedAccessResolver {
    private final AccessControlQuery accessControlQuery;

    @Override
    public ResolvedAccess resolve(java.util.UUID userId) {
        var access = accessControlQuery.resolveEffectiveAccess(userId);
        return new ResolvedAccess(
                access.userId(),
                access.businessId(),
                access.roleCodes(),
                access.permissions().stream()
                        .map(Enum::name)
                        .collect(java.util.stream.Collectors.toUnmodifiableSet()));
    }
}
