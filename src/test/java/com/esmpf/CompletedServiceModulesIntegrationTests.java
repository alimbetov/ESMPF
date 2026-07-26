package com.esmpf;

import static com.esmpf.communication.CommunicationDtos.NotificationTemplateCommand;
import static com.esmpf.document.DocumentDtos.DocumentGenerationCommand;
import static com.esmpf.document.DocumentDtos.ReportTemplateCommand;
import static com.esmpf.identity.IdentityDtos.BusinessCreateCommand;
import static com.esmpf.identity.IdentityDtos.UserAccountCreateCommand;
import static com.esmpf.platform.PlatformDtos.IdempotencyCommand;
import static com.esmpf.platform.PlatformDtos.PublicTokenCommand;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.esmpf.communication.CommunicationService;
import com.esmpf.document.DocumentService;
import com.esmpf.identity.IdentityService;
import com.esmpf.platform.PlatformService;
import com.esmpf.shared.tenant.TenantContext;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(CompletedServiceModulesIntegrationTests.TenantTestConfiguration.class)
@Transactional
class CompletedServiceModulesIntegrationTests {
    private final IdentityService identityService;
    private final DocumentService documentService;
    private final CommunicationService communicationService;
    private final PlatformService platformService;
    private final MutableTenantContext tenantContext;

    @Autowired
    CompletedServiceModulesIntegrationTests(IdentityService identityService,
                                            DocumentService documentService,
                                            CommunicationService communicationService,
                                            PlatformService platformService,
                                            MutableTenantContext tenantContext) {
        this.identityService = identityService;
        this.documentService = documentService;
        this.communicationService = communicationService;
        this.platformService = platformService;
        this.tenantContext = tenantContext;
    }

    @BeforeEach
    void configureTenant() {
        tenantContext.businessId = UUID.fromString("00000000-0000-0000-0000-000000000501");
        tenantContext.userId = UUID.fromString("00000000-0000-0000-0000-000000000502");
    }

    @Test
    void provesIdentityDocumentCommunicationAndPlatformLifecycles() {
        var business = identityService.createBusiness(new BusinessCreateCommand(
                "Service Company", "svc-" + UUID.randomUUID(), "Asia/Almaty", "ru", "KZT", "{}"));
        tenantContext.businessId = business.id();

        var user = identityService.createUser(new UserAccountCreateCommand(
                "worker-" + UUID.randomUUID() + "@example.test", null, "hash", "Worker",
                "TECHNICIAN", true, null, null));
        assertEquals(business.id(), tenantContext.businessId);
        assertEquals("TECHNICIAN", user.role());

        var template = documentService.createTemplate(new ReportTemplateCommand(
                0, "WORK_REPORT", "WORK_REPORT", "ru", 1, "<html/>", null, "{}"));
        template = documentService.publishTemplate(template.id(), template.version());
        var generated = documentService.requestGeneration(new DocumentGenerationCommand(
                "WORK_REPORT", "WR-TEST-1", "SERVICE_JOB", UUID.randomUUID(),
                template.id(), "{}", null));
        assertEquals("REQUESTED", generated.status());

        var notificationTemplate = communicationService.createTemplate(new NotificationTemplateCommand(
                0, "JOB_READY", "EMAIL", "ru", 1, "Ready", "Body"));
        notificationTemplate = communicationService.activateTemplate(notificationTemplate.id(), notificationTemplate.version());
        assertEquals("ACTIVE", notificationTemplate.status());

        String first = platformService.allocateDocumentNumber("INVOICE", 2026, "INV-");
        String second = platformService.allocateDocumentNumber("INVOICE", 2026, "INV-");
        assertNotEquals(first, second);

        var token = platformService.createPublicToken(new PublicTokenCommand(
                "DOCUMENT_VIEW", "DOCUMENT", generated.id(), "hash-" + UUID.randomUUID(),
                Instant.now().plusSeconds(3600), 1));
        platformService.consumePublicToken(token.id());
        assertThrows(IllegalStateException.class, () -> platformService.consumePublicToken(token.id()));

        String key = "request-" + UUID.randomUUID();
        var idempotency = platformService.beginIdempotentOperation(new IdempotencyCommand(
                key, "CREATE_DOCUMENT", "request-hash", Instant.now().plusSeconds(3600)));
        var replay = platformService.beginIdempotentOperation(new IdempotencyCommand(
                key, "CREATE_DOCUMENT", "request-hash", Instant.now().plusSeconds(3600)));
        assertEquals(idempotency.id(), replay.id());
        assertThrows(IllegalArgumentException.class, () -> platformService.beginIdempotentOperation(
                new IdempotencyCommand(key, "CREATE_DOCUMENT", "different-hash", Instant.now().plusSeconds(3600))));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TenantTestConfiguration {
        @Bean @Primary
        MutableTenantContext mutableTenantContext() { return new MutableTenantContext(); }
    }

    static final class MutableTenantContext implements TenantContext {
        private UUID businessId;
        private UUID userId;
        @Override public UUID requireBusinessId() { return businessId; }
        @Override public UUID requireUserId() { return userId; }
    }
}