package com.esmpf;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.esmpf.identity.PermissionCode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class RbacArchitectureTests {

    @Test
    void permissionCatalogueDoesNotPublishDormantCommercialCapabilities() {
        Set<String> codes = Arrays.stream(PermissionCode.values()).map(Enum::name).collect(Collectors.toSet());
        assertTrue(codes.stream().noneMatch(code -> code.startsWith("INVOICE_")));
        assertTrue(codes.stream().noneMatch(code -> code.startsWith("PAYMENT_")));
        assertTrue(codes.stream().noneMatch(code -> code.startsWith("REFUND_")));
    }

    @Test
    void rbacModelDoesNotIntroduceGenericScopeMetadata() throws Exception {
        String sources = Files.readString(Path.of("src/main/java/com/esmpf/identity/domain/RbacEntities.java"));
        assertFalse(sources.contains("scopeType"));
        assertFalse(sources.contains("scopeId"));
    }

    @Test
    void legacyRoleIsNotReadByRbacRuntime() throws Exception {
        String service = Files.readString(Path.of("src/main/java/com/esmpf/identity/domain/RbacServiceImpl.java"));
        assertFalse(service.contains("getRole()"));
        assertFalse(service.contains("user.getRole"));
    }
}
