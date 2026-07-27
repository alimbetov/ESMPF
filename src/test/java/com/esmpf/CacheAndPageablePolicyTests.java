package com.esmpf;

import static com.esmpf.catalog.CatalogDtos.EquipmentTypeCommand;
import static com.esmpf.shared.cache.CacheNames.CATALOG_EQUIPMENT_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.esmpf.catalog.CatalogService;
import com.esmpf.shared.tenant.TenantContext;
import com.esmpf.shared.web.PageablePolicy;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class CacheAndPageablePolicyTests {

    private static final UUID TENANT = UUID.fromString("00000000-0000-0000-0000-000000001001");
    private static final UUID USER = UUID.fromString("00000000-0000-0000-0000-000000001002");

    @MockitoBean
    TenantContext tenantContext;

    @Autowired
    CatalogService catalogService;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    PageablePolicy pageablePolicy;

    @BeforeEach
    void configureTenant() {
        org.mockito.Mockito.when(tenantContext.requireBusinessId()).thenReturn(TENANT);
        org.mockito.Mockito.when(tenantContext.requireUserId()).thenReturn(USER);
        cacheManager.getCacheNames().forEach(name -> {
            var cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        });
    }

    @Test
    void cachesCatalogReadAndEvictsAfterMutation() {
        var created = catalogService.createEquipmentType(new EquipmentTypeCommand(
                0, "CACHE-PUMP", "Pump", "ROTATING", 1, null, null, null));

        catalogService.getEquipmentType(created.id());
        assertNotNull(cacheManager.getCache(CATALOG_EQUIPMENT_TYPE)
                .get(TENANT + ":" + created.id()));

        catalogService.archiveEquipmentType(created.id(), created.version());
        assertEquals(null, cacheManager.getCache(CATALOG_EQUIPMENT_TYPE)
                .get(TENANT + ":" + created.id()));
    }

    @Test
    void boundsPageSizeAndUsesDefaultSort() {
        var normalized = pageablePolicy.normalize(
                PageRequest.of(2, 1_000),
                Sort.by("code").ascending(),
                "code", "name");

        assertEquals(2, normalized.getPageNumber());
        assertEquals(PageablePolicy.MAX_PAGE_SIZE, normalized.getPageSize());
        assertEquals("code", normalized.getSort().iterator().next().getProperty());
    }

    @Test
    void rejectsUnsupportedSortField() {
        assertThrows(IllegalArgumentException.class, () -> pageablePolicy.normalize(
                PageRequest.of(0, 20, Sort.by("passwordHash")),
                Sort.by("code"),
                "code", "name"));
    }
}
