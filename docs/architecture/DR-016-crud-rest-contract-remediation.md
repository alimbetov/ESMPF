# DR-016 — CRUD / REST Contract Remediation

## Status

Implemented in PR #16.

## Context

ESMPF already contains domain models, application services, REST adapters, fail-closed Spring Security and a partial JWT foundation. A permission layer does not yet exist. PR #16 therefore prepares stable contracts and module boundaries without implementing RBAC, bearer authentication, object authorization or file storage.

## Decision

1. External `/api/v1/**` controllers expose business use cases only.
2. Worker, scheduler and integration callbacks use module-owned `/internal/v1/**` controllers.
3. Internal controllers remain denied by the normal HTTP security chain until trusted internal authentication is designed.
4. Tenant aggregates are loaded using tenant-scoped repository methods such as `findByIdAndBusinessId`.
5. Generic status mutation is prohibited; aggregates expose explicit lifecycle actions.
6. Externally addressable list resources require a justified point read, unless the capability matrix marks the omission intentional.
7. Invoice and Payment REST APIs remain dormant; Estimate is the current commercial MVP boundary.
8. Ordinary user DTOs do not expose credentials, role assignments or external identity bindings.
9. The legacy non-null `user_account.role` column is populated internally with `USER` only as a transition invariant. PR #17 replaces runtime authorization with normalized RBAC assignments.

## HTTP conventions

- `POST` creates resources and returns `201`.
- queued commands return `202`.
- `GET /resources/{id}` performs point reads.
- `GET /resources` or parent-scoped collections perform lists.
- `PUT /resources/{id}` updates mutable resources or drafts.
- `POST /resources/{id}/actions/{action}` performs lifecycle transitions.
- `DELETE` is reserved for removable associations; auditable aggregates use archive/revoke/close actions.

## Excluded

- RBAC and permission evaluation;
- JWT bearer runtime;
- object-level authorization;
- multipart upload and storage;
- MinIO/S3 and quarantine.

## Consequences

PR #17 can add `Permission`, `RolePermission`, `AccessRole`, `UserRoleAssignment`, permission-aware REST controllers and application-service authorization guards without redesigning CRUD or worker boundaries.
