# Critical repairs and missing proofs

This file is the input to the next branch, `agent/esmpf-critical-service-repairs`.

## P0 — blocks safe API publication

### CR-001 Atomic payment confirmation

Risk: two payment confirmations may concurrently observe the same invoice balance and both pass an application-level overpayment check.

Required repair:

- lock the invoice row or use an atomic conditional `UPDATE ... RETURNING`;
- update payment and invoice in one transaction;
- reject an amount that would exceed the invoice total;
- add PostgreSQL concurrency proof with multiple payment confirmations;
- prove rollback when invoice update or payment transition fails.

### CR-002 Duplicate external payment protection

Required repair:

- tenant-scoped uniqueness for a non-null external payment identifier;
- replay semantics must distinguish the same payload from conflicting payload;
- positive and concurrent duplicate tests.

### CR-003 Completion rollback proofs

Required for service jobs, visits, executions, work reports and maintenance occurrences.

Prove that a failed prerequisite or stale dependent aggregate leaves all involved aggregates unchanged.

### CR-004 Internal worker contract separation

Worker-facing operations must not accidentally become public endpoints:

- document start/complete/fail generation;
- notification sending/sent/failed;
- outbox publishing/published/failed;
- data-job start/progress/complete/fail;
- document sequence allocation;
- idempotency state transitions.

Introduce explicit internal application ports or package-level controller exclusions before supporting controllers are generated.

## P1 — required for affected core endpoints

### CR-005 Equipment hierarchy integrity

- reject self-parent;
- reject direct and indirect cycles;
- define duplicate/overlapping relation policy;
- prove cross-tenant hierarchy rejection.

### CR-006 Complete lifecycle matrices

For each stateful aggregate, prove every supported transition and principal forbidden transitions. A generic status update must never be introduced.

### CR-007 Published-template immutability

Checklist, maintenance, report and notification templates require proofs that published/active revisions cannot be edited through draft update operations.

### CR-008 Scheduling conflicts

Define and prove whether overlapping worker/job visits are rejected, warned or allowed. Until defined, scheduling endpoints must be marked limited.

### CR-009 Notification claim concurrency

The SQL function exists; add a PostgreSQL proof equivalent to the outbox duplicate-free batch claim test.

## P2 — required before production hardening

- keyset pagination for audit, interactions and queue timelines;
- stuck `PUBLISHING`/`SENDING` recovery;
- dead-letter thresholds and replay policy;
- idempotency expiration cleanup;
- integration secret redaction;
- attachment checksum/storage reconciliation;
- maintenance scheduler catch-up and timezone behavior;
- load and deadlock tests for high-contention paths.
