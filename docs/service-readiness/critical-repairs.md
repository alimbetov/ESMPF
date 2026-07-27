# Critical proofs and scoped repairs

This file is the implementation input for the next branch:

```text
agent/esmpf-operational-flow-proofs
```

The current product boundary is customer management, equipment, request intake, dispatching and service execution. Payment processing is deferred and is not part of this repair stage.

## P0 — blocks safe publication of the operational flow

### OP-001 Request lifecycle and duplicate-conversion proof

Required:

- prove all supported request transitions;
- prove principal forbidden transitions;
- reject stale request versions;
- prevent conversion of the same request more than once;
- prove that failed request-to-job conversion leaves the request unconverted and creates no job;
- prove tenant consistency for customer, service location and equipment references.

### OP-002 Service Job prerequisite and rollback proof

Required:

- define and prove readiness prerequisites;
- define scheduling preconditions;
- prove start/hold/resume/complete/close/cancel transitions;
- prove completion prerequisites for execution and work report;
- prove stale-version rejection;
- prove that failed multi-aggregate completion leaves all aggregates unchanged.

### OP-003 Visit scheduling and lifecycle proof

Required:

- validate planned time range;
- define overlap policy: reject, warn or explicitly allow;
- prove plan/start/complete/cancel lifecycle;
- prove double-start and double-completion behaviour;
- prove tenant and job consistency;
- prove failed completion rollback.

### OP-004 Execution snapshot and completion proof

Required:

- validate job eligibility;
- freeze checklist/template snapshot when execution starts;
- prevent client replacement of server snapshot;
- prove required-answer validation where supported;
- prove single completion and stale-version behaviour;
- prove rollback on failed dependent update.

### OP-005 Work Report approval and job-completion proof

Required:

- validate job ownership and state;
- define active/final report cardinality policy;
- prove report approval transition;
- prove approved report immutability;
- prove job completion requires the expected report state;
- prove stale approval and transaction rollback.

### OP-006 Equipment integrity required by intake

Required:

- reject self-relations;
- reject direct and indirect cycles where hierarchy is supported;
- define duplicate/overlapping relation policy;
- prove customer/location/equipment consistency;
- define archived-equipment behaviour for new requests;
- prove append-only meter readings;
- prove issue-resolution reference and tenant consistency.

### OP-007 Internal worker contract separation

Worker-facing operations must not accidentally become public endpoints:

- document start/complete/fail generation;
- notification sending/sent/failed;
- outbox publishing/published/failed;
- data-job start/progress/complete/fail;
- document sequence allocation;
- idempotency state transitions.

Before supporting controllers are generated, introduce explicit internal application ports, marker annotations or controller-generation exclusions as appropriate.

## P1 — required before affected supporting endpoints

### OP-008 Published-template immutability

Checklist, maintenance, report and notification templates require proofs that published or active revisions cannot be edited through draft update operations.

### OP-009 Customer interaction reference integrity

Prove tenant ownership and related-subject validation for interaction records before exposing them through supporting controllers.

### OP-010 Notification claim concurrency

The SQL claim operation exists. Add a PostgreSQL proof equivalent to the outbox duplicate-free batch claim test before implementing the notification worker.

### OP-011 Attachment and document metadata integrity

Define and prove:

- checksum policy;
- quarantine policy;
- object-storage reconciliation;
- allowed content types and size limits;
- authorization for metadata and download.

### OP-012 Mobile sync semantics

Before publication define:

- idempotency key scope;
- conflict response;
- replay handling;
- stale-version policy;
- partial-batch behaviour.

## P2 — required before production hardening

- keyset pagination for audit, interactions and queue timelines;
- stuck `PUBLISHING`/`SENDING` recovery;
- dead-letter thresholds and replay policy;
- idempotency expiration cleanup;
- integration secret redaction;
- attachment checksum/storage reconciliation;
- maintenance scheduler catch-up and timezone behaviour;
- load and deadlock tests for high-contention operational paths.

## Deferred commercial capability

The following are intentionally excluded from the current proof/repair backlog:

```text
payment registration
payment confirmation
payment failure transitions
refunds
invoice balance accounting
payment-provider callbacks
financial reconciliation
```

They do not block the operational MVP and must not receive REST controllers, OpenAPI schemas, RBAC permissions, UI routes or workers. If payment processing is ever activated, it requires a separate product, legal, accounting, security and concurrency design review.