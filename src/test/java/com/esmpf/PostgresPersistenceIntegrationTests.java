package com.esmpf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(properties = {
        "spring.liquibase.enabled=true",
        "spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.sql",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.open-in-view=false"
})
class PostgresPersistenceIntegrationTests {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("esmpf")
            .withUsername("esmpf")
            .withPassword("esmpf");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @Autowired
    PostgresPersistenceIntegrationTests(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Test
    void createsExactlyFortyEightDomainTablesAndCanReapplyLiquibase() throws Exception {
        Integer tableCount = jdbcTemplate.queryForObject("""
                SELECT count(*)
                  FROM information_schema.tables
                 WHERE table_schema = 'public'
                   AND table_type = 'BASE TABLE'
                   AND table_name NOT IN ('databasechangelog', 'databasechangeloglock')
                """, Integer.class);
        assertEquals(48, tableCount);

        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/db.changelog-master.sql");
        liquibase.setShouldRun(true);
        liquibase.afterPropertiesSet();

        Integer secondCount = jdbcTemplate.queryForObject("""
                SELECT count(*)
                  FROM information_schema.tables
                 WHERE table_schema = 'public'
                   AND table_type = 'BASE TABLE'
                   AND table_name NOT IN ('databasechangelog', 'databasechangeloglock')
                """, Integer.class);
        assertEquals(48, secondCount);
    }

    @Test
    void enforcesCompositeTenantForeignKeysAndIdempotencyUniqueness() {
        UUID businessA = createBusiness("TENANT-A-" + UUID.randomUUID());
        UUID businessB = createBusiness("TENANT-B-" + UUID.randomUUID());
        UUID customerId = UUID.randomUUID();
        insertCustomer(businessA, customerId);

        assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.update("""
                INSERT INTO service_location(
                    id,business_id,created_at,updated_at,version,customer_id,name,status
                ) VALUES (?,?,now(),now(),0,?,?,'ACTIVE')
                """, UUID.randomUUID(), businessB, customerId, "Wrong tenant"));

        Timestamp expiresAt = Timestamp.from(Instant.now().plusSeconds(3600));
        jdbcTemplate.update("""
                INSERT INTO idempotency_record(
                    id,business_id,created_at,updated_at,version,idempotency_key,
                    operation,request_hash,status,expires_at
                ) VALUES (?,?,now(),now(),0,'same-key','CREATE_JOB','hash','IN_PROGRESS',?)
                """, UUID.randomUUID(), businessA, expiresAt);

        assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.update("""
                INSERT INTO idempotency_record(
                    id,business_id,created_at,updated_at,version,idempotency_key,
                    operation,request_hash,status,expires_at
                ) VALUES (?,?,now(),now(),0,'same-key','CREATE_JOB','hash','IN_PROGRESS',?)
                """, UUID.randomUUID(), businessA, expiresAt));
    }

    @Test
    void allocatesDocumentNumbersAtomicallyUnderConcurrency() throws Exception {
        UUID businessId = createBusiness("SEQ-" + UUID.randomUUID());
        ExecutorService executor = Executors.newFixedThreadPool(10);
        try {
            List<Callable<Long>> tasks = new ArrayList<>();
            for (int i = 0; i < 40; i++) {
                tasks.add(() -> jdbcTemplate.queryForObject(
                        "SELECT allocate_document_sequence(?,?,?,?)",
                        Long.class,
                        businessId,
                        "INVOICE",
                        2026,
                        "INV"));
            }
            List<Future<Long>> futures = executor.invokeAll(tasks);
            Set<Long> values = new HashSet<>();
            for (Future<Long> future : futures) {
                values.add(future.get());
            }
            assertEquals(40, values.size());
            assertTrue(values.contains(1L));
            assertTrue(values.contains(40L));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void neverConsumesPublicTokenBeyondMaximumUses() throws Exception {
        UUID businessId = createBusiness("TOKEN-" + UUID.randomUUID());
        UUID tokenId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO public_access_token(
                    id,business_id,created_at,updated_at,version,purpose,subject_type,
                    subject_id,token_hash,expires_at,max_uses,used_count
                ) VALUES (?,?,now(),now(),0,'PUBLIC_DOCUMENT','DOCUMENT',?,?,?,5,0)
                """, tokenId, businessId, UUID.randomUUID(), "hash-" + tokenId,
                Timestamp.from(Instant.now().plusSeconds(3600)));

        ExecutorService executor = Executors.newFixedThreadPool(10);
        try {
            List<Callable<Boolean>> tasks = new ArrayList<>();
            for (int i = 0; i < 25; i++) {
                tasks.add(() -> Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                        "SELECT consume_public_access_token(?,?)",
                        Boolean.class,
                        businessId,
                        tokenId)));
            }
            List<Future<Boolean>> futures = executor.invokeAll(tasks);
            long successful = 0;
            for (Future<Boolean> future : futures) {
                if (future.get()) {
                    successful++;
                }
            }
            assertEquals(5, successful);
            Integer usedCount = jdbcTemplate.queryForObject(
                    "SELECT used_count FROM public_access_token WHERE id=?",
                    Integer.class,
                    tokenId);
            assertEquals(5, usedCount);
            assertFalse(Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                    "SELECT consume_public_access_token(?,?)",
                    Boolean.class,
                    businessId,
                    tokenId)));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void claimsOutboxEventsWithoutDuplicateDeliveryBetweenWorkers() throws Exception {
        UUID businessId = createBusiness("OUTBOX-" + UUID.randomUUID());
        for (int i = 0; i < 20; i++) {
            jdbcTemplate.update("""
                    INSERT INTO outbox_event(
                        id,business_id,created_at,updated_at,version,aggregate_type,
                        aggregate_id,event_type,event_version,payload_json,status,
                        attempt_count,next_attempt_at
                    ) VALUES (?,?,now(),now(),0,'JOB',?,'JOB_UPDATED',1,'{}'::jsonb,
                              'PENDING',0,now())
                    """, UUID.randomUUID(), businessId, UUID.randomUUID());
        }

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Callable<List<UUID>> claim = () -> jdbcTemplate.queryForList(
                    "SELECT event_id FROM claim_outbox_events(?,10)",
                    UUID.class,
                    businessId);
            Future<List<UUID>> first = executor.submit(claim);
            Future<List<UUID>> second = executor.submit(claim);
            Set<UUID> claimed = new HashSet<>(first.get());
            assertEquals(10, claimed.size());
            List<UUID> secondBatch = second.get();
            assertEquals(10, secondBatch.size());
            for (UUID id : secondBatch) {
                assertTrue(claimed.add(id));
            }
            assertEquals(20, claimed.size());
            Integer publishing = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM outbox_event WHERE business_id=? AND status='PUBLISHING'",
                    Integer.class,
                    businessId);
            assertEquals(20, publishing);
        } finally {
            executor.shutdownNow();
        }
    }

    private UUID createBusiness(String code) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO business(
                    id,created_at,updated_at,version,name,code,timezone,
                    default_language,currency,status
                ) VALUES (?,now(),now(),0,?,?,'Asia/Almaty','ru','KZT','ACTIVE')
                """, id, code, code);
        return id;
    }

    private void insertCustomer(UUID businessId, UUID customerId) {
        jdbcTemplate.update("""
                INSERT INTO customer(
                    id,business_id,created_at,updated_at,version,type,name,status
                ) VALUES (?,?,now(),now(),0,'COMPANY','Customer','ACTIVE')
                """, customerId, businessId);
    }
}
