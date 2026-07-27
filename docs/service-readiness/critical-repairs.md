# Critical repairs and missing proofs

This file is the input to the next branch, `agent/esmpf-critical-service-repairs`.

## Current product boundary

The operational MVP manages customers, equipment, requests, dispatching and service execution. Payment-provider integration, payment confirmation and refunds are deferred. Existing payment classes remain dormant domain code and are not part of REST, worker or production-readiness scope for this release.

Consequently, payment concurrency work is not a blocker for the operational MVP. It becomes mandatory only if payment functionality is explicitly reactivated in a future commercial stage.

## P0 — blocks safe API publication

### CR-001 Completion rollback proofs

Required for service jobs, visits, executions, work reports and maintenance occurrences.

Prove that a failed prerequisite or stale dependent aggregate leaves all involved aggregates unchanged.

### CR-002 Internal worker contract separation

Worker-facing operations must not accidentally become public endpoints:

- document start/complete/fail generation;
- notification sending/sent/failed;
- outbox publishing/published/failed;
- data-job start/progress/complete/fail;
- document sequence allocation;
- idempotency state transitions.

Introduce explicit internal application ports or package-level controller exclusions before supporting controllers are generated.

### CR-003 Commercial scope guard

The REST and OpenAPI layers must not publish payment processing in the current release:

- no payment registration endpoint;
- no payment confirmation endpoint;
- no refund endpoint;
- no payment-provider callback;
- no provider credentials or secrets;
- no API wording that claims financial settlement.

Estimates may be exposed only as quotations supporting service-request handling. Invoice/payment entities remain deferred and must be excluded from controller generation and Swagger schemas unless a later product decision reactivates them.

## P1 — required for affected core endpoints

### CR-004 Equipment hierarchy integrity

- reject self-parent;
- reject direct and indirect cycles;
- define duplicate/overlapping relation policy;
- prove cross-tenant hierarchy rejection.

### CR-005 Complete lifecycle matrices

For each stateful aggregate, prove every supported transition and principal forbidden transitions. A generic status update must never be introduced.

### CR-006 Published-template immutability

Checklist, maintenance, report and notification templates require proofs that published/active revisions cannot be edited through draft update operations.

### CR-007 Scheduling conflicts

Define and prove whether overlapping worker/job visits are rejected, warned or allowed. Until defined, scheduling endpoints must be marked limited.

### CR-008 Notification claim concurrency

The SQL function exists; add a PostgreSQL proof equivalent to the outbox duplicate-free batch claim test.

## Deferred commercial hardening

The following work is intentionally outside the current MVP and must be restored as a mandatory gate before any future payment release:

- atomic invoice-balance update;
- duplicate external payment protection;
- idempotent payment-provider callbacks;
- overpayment and refund invariants;
- PostgreSQL concurrency proofs;
- financial audit trail and reconciliation;
- provider-security and compliance review.

## P2 — required before production hardening

- keyset pagination for audit, interactions and queue timelines;
- stuck `PUBLISHING`/`SENDING` recovery;
- dead-letter thresholds and replay policy;
- idempotency expiration cleanup;
- integration secret redaction;
- attachment checksum/storage reconciliation;
- maintenance scheduler catch-up and timezone behavior;
- load and deadlock tests for high-contention paths.