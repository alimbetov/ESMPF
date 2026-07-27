# ESMPF service readiness audit

This audit evaluates the application-service contracts present on `main` after the PostgreSQL/Liquibase integrity baseline and defines the implementation sequence for the operational MVP.

## Product boundary

The current ESMPF release is focused on:

- customers and service locations;
- equipment;
- service-request intake and triage;
- dispatching and service-job execution;
- visits, checklists and work reports;
- supporting notifications, documents and preventive maintenance.

Payment registration, confirmation, refunds, payment-provider integration and financial reconciliation are deferred. Existing payment code remains dormant and must not receive REST controllers, OpenAPI schemas, permissions, UI routes or workers in the current roadmap.

## Verdict vocabulary

- `READY_FOR_API` — contract is suitable for a REST endpoint after transport DTO and error mapping are added.
- `NEEDS_PROOF` — implementation exists, but a critical positive, negative, tenant, stale-version, rollback or PostgreSQL proof is missing.
- `NEEDS_REPAIR` — a known correctness or concurrency defect must be fixed before publication.
- `BLOCKED_BY_RUNTIME_ADAPTER` — application state model exists, but the user-visible capability depends on a worker or external adapter.
- `INTERNAL_ONLY` — operation belongs to workers or infrastructure administration and must not become a public CRUD endpoint.
- `DEFERRED` — capability intentionally remains outside the current product release and does not block the operational MVP.

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
10. runtime-adapter dependency;
11. current product scope.

## Current conclusion

The service layer is implemented broadly enough to begin REST design, but not every operation may be published yet.

- Customer, Catalog, Equipment and the main Service Management commands are the first REST candidates.
- The next proof stage is limited to request intake, request-to-job conversion, dispatching, visits, executions, reports, rollback and related equipment integrity.
- Maintenance administration is API-ready; automatic occurrence generation remains worker-bound.
- Estimates may later support non-binding quotations, but invoice/payment processing remains deferred.
- Document generation, notification delivery, outbox publication and data-job execution expose internal worker transitions and must remain internal.
- Identity administration is API-ready in principle, while authentication and authorization belong to the later JWT/RBAC stage.

## Updated delivery order

```text
1. Service readiness audit
2. Critical proofs for requests and work execution
3. REST + global error handlers + Swagger/OpenAPI
4. Customer, Equipment and Service Request controllers
5. Service Job, Visit, Execution and Work Report controllers
6. Supporting controllers without Payment API
7. JWT + RBAC + TenantContext
8. Notification, document and maintenance workers
9. Observability and production hardening
```

See:

- `service-inventory.md`
- `api-readiness-matrix.md`
- `lifecycle-matrix.md`
- `critical-repairs.md`
- `implementation-roadmap.md`