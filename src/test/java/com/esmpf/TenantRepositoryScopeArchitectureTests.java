package com.esmpf;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class TenantRepositoryScopeArchitectureTests {

    private static final Path SOURCES = Path.of("src/main/java/com/esmpf");
    private static final List<String> FORBIDDEN = List.of(
            ".deleteById(", ".existsById(", ".findAll()"
    );

    @Test
    void tenantServicesDoNotUseUnscopedRepositoryOperations() throws IOException {
        try (var paths = Files.walk(SOURCES)) {
            for (Path path : paths.filter(p -> p.getFileName().toString().endsWith("ServiceImpl.java")).toList()) {
                String source = Files.readString(path);
                for (String operation : FORBIDDEN) {
                    assertFalse(source.contains(operation), path + " uses " + operation);
                }
                String withoutTenantRootLookup = source.replace("businessRepository.findById(tenant())", "");
                assertFalse(withoutTenantRootLookup.contains("Repository.findById("),
                        path + " uses unscoped findById");
            }
        }
    }
}
