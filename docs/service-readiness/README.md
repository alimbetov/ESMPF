# ESMPF service readiness audit

This audit evaluates the application-service contracts present on `main` after the PostgreSQL/Liquibase integrity baseline.

## Verdict vocabulary

- `READY_FOR_API` — contract is suitable for a REST endpoint after transport DTO and error mapping are added.
- `NEEDS_PROOF` — implementation exists, but a critical positive, negative, tenant, stale-version, rollback or PostgreSQL proof is missing.
- `NEEDS_REPAIR` — a known correctness or concurrency defect must be fixed before publication.
- `BLOCKED_BY_RUNTIME_ADAPTER` — application state model exists, but the user-visible capability depends on a worker or external adapter.
- `INTERNAL_ONLY` — operation belongs to workers or infrastructure administration and must not become a public CRUD endpoint.

## Audit dimensions

Every public service operation is evaluated against:

1. transaction boundary;
2. trusted tenant context;
3. optimistic version handling for mutations;
4. explicit lifecycle transition;
5. positive integration proof;
6. principal negative-state proof;
7. PostgreSQL integrity proof;
8. concurrency risk;
9. REST contract suitability;
10. runtime-adapter dependency.

## Current conclusion

The service layer is implemented broadly enough to begin REST design, but not every operation may be published yet.

- Customer, Catalog, Equipment and the main Service Management commands are the first REST candidates.
- Maintenance administration is API-ready; automatic occurrence generation remains worker-bound.
- Commercial payment confirmation requires an atomic invoice-balance repair and concurrency proof.
- Document generation, notification delivery, outbox publication and data-job execution expose internal worker transitions and must remain internal.
- Identity administration is API-ready in principle, while authentication and authorization belong to the later JWT/RBAC stage.

See:

- `service-inventory.md`
- `api-readiness-matrix.md`
- `lifecycle-matrix.md`
- `critical-repairs.md`
