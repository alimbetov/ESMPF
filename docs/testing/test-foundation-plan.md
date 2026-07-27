# ESMPF test foundation

## Decision

Use two complementary test contours:

1. Fast contour on H2 for unit, Spring service and future MockMvc tests.
2. Proof contour on PostgreSQL Testcontainers for Liquibase, schema validation, constraints and concurrency.

H2 is not treated as a PostgreSQL replacement.

## Commands

```bash
mvn -Dtest='!*PostgresPersistenceIntegrationTests' test
mvn verify
```

## Fast contour contract

- test resources force an in-memory H2 database in PostgreSQL compatibility mode;
- Liquibase is disabled;
- Hibernate creates and drops the schema;
- tests must be independent of execution order;
- reusable fixtures create tenant data;
- smoke tests prove that H2 is actually active and that RBAC role provisioning works.

## PostgreSQL proof contract

- Liquibase applies from the production root changelog;
- Hibernate validates the resulting schema;
- PostgreSQL-specific indexes, constraints, functions and concurrent operations remain covered by Testcontainers;
- the full verification job runs only after the fast contour succeeds.

## Next coverage slices

Add service contract matrices in this order:

1. Identity and RBAC
2. Customer
3. Catalog
4. Content
5. Service request and job lifecycle
6. Visit, execution and work report

Each service matrix must cover happy path, not found, validation, stale version, tenant isolation and rollback where applicable.
