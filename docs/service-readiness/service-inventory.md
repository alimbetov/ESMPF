# Application service inventory

## Core modules

### CustomerService

Public operations: customer create/get/list/update/archive and service-location create/get/list/update/archive.

Assessment:

- tenant ownership is implicit through `TenantContext` rather than command data;
- mutations expose explicit optimistic versions;
- CRUD and archive semantics are suitable for REST;
- customer merge, deduplication, consent enforcement and normalized search are not part of the current contract.

Verdict: `READY_FOR_API` for the existing operations, subject to endpoint-level validation and error-contract tests.

### CatalogService

Covers equipment types, job types, checklist templates, maintenance templates and units of measure.

Assessment:

- explicit template publication is preferable to generic status updates;
- business-key uniqueness is protected in application logic and PostgreSQL;
- catalog reference use needs negative proofs for archiving/deactivation while referenced;
- published-template immutability needs a complete regression matrix.

Verdict: mixed `READY_FOR_API` / `NEEDS_PROOF`.

### EquipmentService

Covers equipment, relations, issues and meter readings.

Assessment:

- cross-module references are validated through module ports and composite tenant foreign keys;
- meter readings are append-only;
- missing structural proofs include self-parent rejection, hierarchy-cycle rejection and overlapping/duplicate relation rules;
- meter reset/rollover and calibration semantics are not represented.

Verdict: base equipment and issue operations `READY_FOR_API`; hierarchy and relation commands `NEEDS_PROOF`.

### MaintenanceService

Covers maintenance plans and occurrences.

Assessment:

- plan lifecycle is explicit;
- generation-key deduplication is implemented;
- manual administration is suitable for REST;
- automatic due evaluation, catch-up, timezone scheduling and distributed execution require workers.

Verdict: plan/occurrence administration `READY_FOR_API`; automatic generation `BLOCKED_BY_RUNTIME_ADAPTER`.

### ServiceManagementService

Covers service requests, jobs, visits, executions and work reports.

Assessment:

- lifecycle commands are explicit and should map to command endpoints;
- optimistic version is present on state changes;
- request-to-job and job/visit flows have integration proofs;
- complete negative transition matrix, worker scheduling conflicts and rollback proofs remain incomplete.

Verdict: principal request/job/visit operations `READY_FOR_API`; some completion and scheduling operations `NEEDS_PROOF`.

## Supporting modules

### IdentityService

Covers business administration, locations, users and worker qualifications.

Verdict: administration `READY_FOR_API`; login, password lifecycle, session management and permissions are outside the current service contract and belong to Security.

### CommercialService

Covers estimates, invoices and payments.

Assessment:

- estimate and invoice lifecycles are explicit;
- payment confirmation can race when concurrent confirmations update the same invoice balance;
- refund is currently a payment-state transition rather than an immutable financial transaction/ledger model.

Verdict: estimates `READY_FOR_API`; invoices mostly `NEEDS_PROOF`; payment confirmation/refund `NEEDS_REPAIR`.

### DocumentService

Covers templates, generated-document state, attachments, links and signatures.

Assessment:

- metadata and lifecycle contracts exist;
- `startGeneration`, `completeGeneration` and `failGeneration` are worker-facing transitions;
- rendering, object storage, malware scanning and signature verification adapters are absent.

Verdict: template/metadata administration `READY_FOR_API`; generation transitions `INTERNAL_ONLY`; complete document capability `BLOCKED_BY_RUNTIME_ADAPTER`.

### CommunicationService

Covers notification templates, queued notifications and customer feedback.

Assessment:

- enqueue and feedback administration are suitable for REST;
- `markSending`, `markSent` and `markFailed` are dispatcher-facing operations;
- actual email/SMS/push providers and delivery callbacks are absent.

Verdict: template/enqueue/feedback `READY_FOR_API`; delivery transitions `INTERNAL_ONLY`; actual delivery `BLOCKED_BY_RUNTIME_ADAPTER`.

### PlatformService

Covers public tokens, data jobs, outbox, audit, idempotency, integrations and document sequences.

Assessment:

- atomic SQL proofs exist for public-token consumption, sequence allocation and outbox claiming;
- outbox and data-job state transitions belong to workers;
- audit is append-only infrastructure;
- idempotency operations are middleware-facing rather than ordinary public resources.

Verdict: narrowly selected administration/query operations may be exposed; worker transitions and sequence/idempotency primitives are `INTERNAL_ONLY`.

### CustomerInteractionService and ServiceSupportService

These contracts add customer timeline entries, recommendations, materials, agreements, warranties, mobile devices and sync operations.

Verdict: read/command operations may enter supporting controllers after operation-by-operation proof mapping; mobile sync requires dedicated conflict and idempotency proofs.
