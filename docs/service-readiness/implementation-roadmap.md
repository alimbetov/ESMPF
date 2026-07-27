# ESMPF operational MVP implementation roadmap

## 1. Purpose

This document turns the service-readiness audit into an implementation plan for the current ESMPF product boundary.

The target product is a multi-tenant service-operations platform that manages:

1. customers and service locations;
2. equipment installed or serviced for those customers;
3. intake and triage of service requests;
4. conversion of accepted requests into service jobs;
5. dispatching and scheduling visits;
6. execution of work and checklist capture;
7. work reports, recommendations and closure;
8. operational notifications, documents and preventive-maintenance automation.

The current release is not a payment platform. Payment registration, payment confirmation, refunds, acquiring, payment-provider callbacks and financial reconciliation are explicitly deferred. Existing payment persistence and service code remains dormant and receives no REST controller, OpenAPI schema, UI route, permission or worker in this roadmap.

## 2. Primary end-to-end business flow

```text
Customer
  -> ServiceLocation
  -> Equipment
  -> ServiceRequest
  -> triage / accept
  -> ServiceJob
  -> schedule
  -> JobVisit
  -> JobExecution
  -> WorkReport
  -> recommendations / materials
  -> complete / close
```

Every implementation stage must improve or protect this flow. A capability that does not support this flow must be explicitly classified as supporting, internal-only or deferred.

## 3. Delivery principles

### 3.1 Thin transport layer

```text
HTTP controller
  -> API mapper
  -> application-service interface
  -> domain/persistence implementation
```

Controllers must never call JPA repositories directly and must never expose persistent entities.

### 3.2 Trusted tenant ownership

Until JWT is introduced, controllers use the existing trusted `TenantContext` abstraction. After the security stage, `businessId` is derived only from a validated JWT principal.

The following are prohibited:

- `businessId` in request DTOs;
- tenant ID in a query parameter;
- an unsigned tenant header supplied by a browser;
- unrestricted repository lookup by aggregate ID alone.

### 3.3 Explicit lifecycle commands

Stateful aggregates expose commands, not generic status mutation.

Preferred:

```text
POST /api/v1/service-requests/{id}/triage
POST /api/v1/service-jobs/{id}/start
POST /api/v1/job-visits/{id}/complete
```

Prohibited:

```text
PATCH /api/v1/service-jobs/{id}
{ "status": "COMPLETED" }
```

### 3.4 Optimistic concurrency

Every mutable aggregate response exposes `version`. Every update or lifecycle command carries the expected version. A stale mutation maps to `409 Conflict`.

### 3.5 OpenAPI is an executable contract

Swagger/OpenAPI is implemented with the REST foundation and validated in tests. It is not generated as an undocumented side effect after controllers are finished.

### 3.6 Worker operations remain internal

Runtime transitions such as `markSending`, `startGeneration`, `markOutboxPublishing` and data-job progress updates must not become public endpoints.

## 4. Updated implementation sequence

```text
1. Service readiness audit
2. Critical proofs for request intake and work execution
3. REST + global error handlers + Swagger/OpenAPI
4. Customer, Equipment and Service Request controllers
5. Service Job, Visit, Execution and Work Report controllers
6. Supporting controllers without Payment API
7. JWT + RBAC + TenantContext
8. Notification, document and maintenance workers
9. Observability and production hardening
```

---

# Stage 1. Service readiness audit

## Objective

Produce a code-grounded classification of application operations before REST publication.

## Branch

```text
agent/esmpf-service-readiness-audit
```

## Deliverables

- service inventory;
- API-readiness matrix;
- lifecycle matrix;
- critical-proof backlog;
- internal/deferred capability classification;
- executable architecture tests for public service contracts;
- this implementation roadmap.

## Required verdicts

```text
READY_FOR_API
NEEDS_PROOF
NEEDS_REPAIR
BLOCKED_BY_RUNTIME_ADAPTER
INTERNAL_ONLY
DEFERRED
```

## Exit criteria

- every application-service capability has a verdict;
- the operational MVP boundary is explicit;
- Payment API is classified `DEFERRED`;
- internal worker methods are identified;
- the next proof branch has a finite, prioritised scope;
- `mvn verify` is green.

---

# Stage 2. Critical proofs for requests and work execution

## Objective

Prove the correctness of the main operational path before exposing it through HTTP.

## Proposed branch

```text
agent/esmpf-operational-flow-proofs
```

This replaces a broad financial `critical-service-repairs` stage. Payment defects are not addressed because payment processing is outside the current product scope.

## 2.1 Service Request proofs

Required lifecycle matrix:

```text
NEW -> TRIAGED
NEW -> CANCELLED
TRIAGED -> ACCEPTED
TRIAGED -> REJECTED
TRIAGED -> CANCELLED
ACCEPTED -> CONVERTED
```

Required forbidden transitions include:

- rejected request cannot be accepted;
- cancelled request cannot be triaged;
- converted request cannot be converted again;
- stale request version is rejected;
- request cannot reference customer, location or equipment of another tenant;
- conversion failure leaves the request unconverted and creates no job.

## 2.2 Service Job proofs

Required lifecycle matrix:

```text
DRAFT -> READY
READY -> SCHEDULED
SCHEDULED -> IN_PROGRESS
IN_PROGRESS -> WAITING
WAITING -> IN_PROGRESS
IN_PROGRESS -> COMPLETED
COMPLETED -> CLOSED
DRAFT/READY/SCHEDULED/IN_PROGRESS/WAITING -> CANCELLED where policy permits
```

Required proofs:

- job readiness prerequisites;
- schedule command validity;
- start only after valid scheduling policy;
- completion requires all mandatory execution/report prerequisites;
- close only after completion;
- cancelled/closed job is immutable except allowed audit-safe fields;
- stale version leaves state unchanged;
- cross-tenant references are rejected;
- a failed multi-aggregate command rolls back completely.

## 2.3 Job Visit proofs

Required proofs:

- visit belongs to the same tenant and job;
- planned start precedes planned end;
- start is allowed only from planned state;
- completion is allowed only after start;
- cancellation rules are explicit;
- double start and double completion are rejected or idempotently handled according to contract;
- schedule-overlap policy is documented and tested;
- stale version is rejected;
- failed visit completion does not partially update the job.

The MVP may initially permit overlapping assignments if that is a deliberate documented policy. Undefined behaviour is not acceptable.

## 2.4 Job Execution proofs

Required proofs:

- execution is created only for an eligible job;
- checklist/template snapshot is immutable after execution starts;
- answers are validated against required checklist items where model supports it;
- execution cannot complete twice;
- stale version is rejected;
- cross-tenant template/job links are rejected;
- failed completion leaves execution and job unchanged.

## 2.5 Work Report proofs

Required proofs:

- report belongs to an eligible job;
- only one active/final report policy is defined;
- approval is explicit and versioned;
- an approved report cannot be modified through draft operations;
- job completion requires the expected report state;
- stale approval fails without side effects;
- cross-tenant references are rejected.

## 2.6 Equipment and request-reference proofs

Because request intake depends on equipment integrity, include:

- equipment self-relation rejection;
- direct and indirect hierarchy cycle rejection where hierarchy is supported;
- service location/customer/equipment consistency;
- archived equipment policy for new requests;
- append-only meter-reading proof;
- issue resolution reference validation.

## 2.7 PostgreSQL and transaction proofs

At minimum:

- stale update using two transactions;
- duplicate request-to-job conversion prevention;
- transaction rollback across request/job creation;
- tenant composite-FK rejection for relevant cross-module references;
- scheduling/index query plans are not required yet, but correctness must run against PostgreSQL Testcontainers.

## Exit criteria

- the main end-to-end flow succeeds in an integration test;
- principal forbidden transitions are covered;
- all mutation responses return the persisted new `version`;
- rollback and tenant-isolation proofs are green on PostgreSQL;
- no known P0 correctness defect remains in the operational path.

---

# Stage 3. REST, global error handlers and Swagger/OpenAPI foundation

## Objective

Create one stable transport foundation before feature controllers proliferate.

## Proposed branch

```text
agent/esmpf-rest-openapi-foundation
```

## 3.1 Dependencies and configuration

- `spring-boot-starter-web` if not already present;
- `spring-boot-starter-validation`;
- compatible `springdoc-openapi-starter-webmvc-ui`;
- test support through MockMvc and JSON assertions.

Base path:

```text
/api/v1
```

OpenAPI endpoints:

```text
/v3/api-docs
/swagger-ui.html
```

Swagger and API docs must be profile/property controlled and disabled by default in production unless an explicit protected deployment policy enables them.

## 3.2 Shared API contracts

Create transport-level types such as:

```text
ApiError
FieldViolation
PageResponse<T>
VersionCommand
CorrelationIdFilter
SortPolicy
```

Suggested `ApiError` fields:

```text
code
message
path
timestamp
correlationId
details
fieldViolations
```

## 3.3 Global exception mapping

Required mappings:

| Failure | HTTP status |
|---|---:|
| malformed JSON / invalid request | 400 |
| Bean Validation failure | 400 |
| authentication absent/invalid | 401 after security stage |
| permission denied | 403 after security stage |
| entity not found in tenant | 404 |
| duplicate business key | 409 |
| stale optimistic version | 409 |
| invalid lifecycle transition | 409 |
| business precondition violation | 422 |
| unsupported sort/filter | 400 |
| unexpected exception | 500 |

The handler must not leak SQL, class names, stack traces or internal tenant identifiers.

## 3.4 Correlation ID

- accept a syntactically valid `X-Correlation-Id`;
- otherwise generate a UUID;
- put it into MDC;
- return it in the response header;
- include it in `ApiError`;
- clear MDC after the request.

## 3.5 Pagination and sorting

- standard page envelope;
- default `page=0`, `size=20`;
- maximum size, for example 100;
- deterministic default sort;
- endpoint-specific sort whitelist;
- no arbitrary property injection into `Sort`.

## 3.6 OpenAPI conventions

Every operation must define:

- unique `operationId`;
- module tag;
- summary and lifecycle semantics;
- request and response schemas;
- validation constraints;
- standard error responses;
- pagination parameters where applicable;
- optimistic-version requirement for mutations.

OpenAPI tests must prove:

- `/v3/api-docs` returns a valid document;
- required metadata exists;
- operation IDs are unique;
- JPA entities do not appear as public schemas;
- common errors reference `ApiError`;
- Payment paths and schemas are absent.

## Exit criteria

- foundation tests pass without feature controllers;
- global errors have one stable JSON shape;
- correlation IDs work for success and failure;
- Swagger UI is usable in local/test profiles;
- OpenAPI contract tests are part of `mvn verify`.

---

# Stage 4. Customer, Equipment and Service Request controllers

## Objective

Expose the intake side of the operational flow.

## Proposed branch

```text
agent/esmpf-intake-rest-controllers
```

## 4.1 Customer API

Recommended resources:

```text
POST   /api/v1/customers
GET    /api/v1/customers/{customerId}
GET    /api/v1/customers
PUT    /api/v1/customers/{customerId}
POST   /api/v1/customers/{customerId}/archive

POST   /api/v1/customers/{customerId}/service-locations
GET    /api/v1/service-locations/{locationId}
GET    /api/v1/customers/{customerId}/service-locations
PUT    /api/v1/service-locations/{locationId}
POST   /api/v1/service-locations/{locationId}/archive
```

Customer interactions may be added only after their related-subject validation proofs are complete.

## 4.2 Equipment API

Recommended resources:

```text
POST   /api/v1/equipment
GET    /api/v1/equipment/{equipmentId}
GET    /api/v1/equipment
PUT    /api/v1/equipment/{equipmentId}
POST   /api/v1/equipment/{equipmentId}/archive

POST   /api/v1/equipment/{equipmentId}/issues
GET    /api/v1/equipment/{equipmentId}/issues
POST   /api/v1/equipment-issues/{issueId}/resolve

POST   /api/v1/equipment/{equipmentId}/meter-readings
GET    /api/v1/equipment/{equipmentId}/meter-readings
```

Relations/hierarchy endpoints enter this PR only if cycle and overlap policy proofs are complete.

## 4.3 Service Request API

Recommended resources:

```text
POST   /api/v1/service-requests
GET    /api/v1/service-requests/{requestId}
GET    /api/v1/service-requests
POST   /api/v1/service-requests/{requestId}/triage
POST   /api/v1/service-requests/{requestId}/accept
POST   /api/v1/service-requests/{requestId}/reject
POST   /api/v1/service-requests/{requestId}/cancel
POST   /api/v1/service-requests/{requestId}/convert-to-job
```

The conversion endpoint returns the created `ServiceJobResponse` and must be atomic.

## 4.4 Controller requirements

Each resource receives:

- request/response DTOs separate from application DTOs where transport semantics differ;
- MapStruct API mapper;
- Bean Validation;
- OpenAPI annotations;
- MockMvc happy-path tests;
- validation and global-error tests;
- stale-version test;
- tenant-not-found behaviour test;
- response headers and status correctness.

## Exit criteria

A user can create a customer, location and equipment, submit a request, triage it and convert it into a job entirely through REST.

---

# Stage 5. Service Job, Visit, Execution and Work Report controllers

## Objective

Expose dispatching and field-work execution.

## Proposed branch

```text
agent/esmpf-execution-rest-controllers
```

## 5.1 Service Job API

```text
POST   /api/v1/service-jobs
GET    /api/v1/service-jobs/{jobId}
GET    /api/v1/service-jobs
POST   /api/v1/service-jobs/{jobId}/ready
POST   /api/v1/service-jobs/{jobId}/schedule
POST   /api/v1/service-jobs/{jobId}/start
POST   /api/v1/service-jobs/{jobId}/hold
POST   /api/v1/service-jobs/{jobId}/resume
POST   /api/v1/service-jobs/{jobId}/complete
POST   /api/v1/service-jobs/{jobId}/close
POST   /api/v1/service-jobs/{jobId}/cancel
```

## 5.2 Visit API

```text
POST   /api/v1/service-jobs/{jobId}/visits
GET    /api/v1/service-jobs/{jobId}/visits
POST   /api/v1/job-visits/{visitId}/start
POST   /api/v1/job-visits/{visitId}/complete
POST   /api/v1/job-visits/{visitId}/cancel
```

## 5.3 Execution API

```text
POST   /api/v1/service-jobs/{jobId}/executions
POST   /api/v1/job-executions/{executionId}/complete
```

The checklist snapshot is returned read-only and never replaced by client-supplied template state.

## 5.4 Work Report API

```text
POST   /api/v1/service-jobs/{jobId}/work-reports
POST   /api/v1/work-reports/{reportId}/approve
```

Read endpoints should be added if application contracts support them; otherwise add explicit query operations rather than bypassing services.

## 5.5 Response design

Operational responses should expose enough information for a UI to render available actions, but business authority stays server-side. A response may include current state and version; the client must not be trusted to determine which transition is valid.

## Exit criteria

The full primary flow from accepted request through closed job is executable through REST and documented in OpenAPI.

---

# Stage 6. Supporting controllers without Payment API

## Objective

Expose administrative and supporting capabilities that directly assist operational work.

## Proposed branch

```text
agent/esmpf-supporting-rest-controllers
```

## Included capabilities

### Catalog administration

- equipment types;
- job types;
- units of measure;
- checklist templates;
- maintenance templates.

Published-template immutability must be proven first.

### Identity administration

- current business profile;
- business locations;
- user accounts;
- worker qualifications.

These endpoints are structurally implemented before JWT but receive real authorization only in Stage 7.

### Service support

- recommendations;
- material usage or material records required by work reports;
- service agreements;
- warranty cases;
- customer interactions after proof completion;
- mobile-sync endpoints after idempotency/conflict proofs.

### Estimates

Estimates may be exposed as non-binding quotations supporting request/job handling. Their naming and documentation must avoid claiming that ESMPF processes money.

Possible routes:

```text
POST /api/v1/estimates
GET  /api/v1/estimates/{estimateId}
GET  /api/v1/estimates
PUT  /api/v1/estimates/{estimateId}
POST /api/v1/estimates/{estimateId}/send
POST /api/v1/estimates/{estimateId}/approve
POST /api/v1/estimates/{estimateId}/reject
```

### Document and communication administration

- report-template administration;
- generated-document metadata/status query;
- attachment metadata where storage policy exists;
- notification-template administration;
- enqueue notification command;
- feedback management;
- audit query with restricted filters.

## Explicitly excluded

No controller, OpenAPI operation, permission or UI action for:

```text
Invoice payment processing
Payment registration
Payment confirmation
Payment failure processing
Refunds
Payment-provider callbacks
Financial reconciliation
```

Also excluded from generic public CRUD:

```text
OutboxEvent
IdempotencyRecord
DocumentSequence
worker delivery transitions
worker generation transitions
DataJob runtime transitions
```

## Exit criteria

Supporting endpoints assist the operational workflow without exposing payment processing or internal runtime protocols.

---

# Stage 7. JWT, RBAC and TenantContext

## Objective

Replace development tenant fallback with authenticated, authorised tenant resolution.

## Proposed branch

```text
agent/esmpf-security-jwt-rbac
```

## 7.1 Recommended security model

Use Spring Security OAuth2 Resource Server for JWT validation. Token issuance may remain external or be implemented separately; the service must not implement an insecure custom JWT parser.

Required trusted claims:

```text
sub
business_id
roles and/or permissions
issuer
audience
expiration
```

## 7.2 Tenant principal

Create an authenticated principal abstraction that exposes:

```text
userId
businessId
roles
permissions
```

`TenantContext` reads `businessId` from this principal. Background workers use an explicit trusted system tenant execution context, never an HTTP header workaround.

## 7.3 RBAC and permissions

Suggested roles:

```text
OWNER
ADMIN
DISPATCHER
TECHNICIAN
VIEWER
```

Prefer granular permissions for endpoint protection:

```text
CUSTOMER_READ
CUSTOMER_WRITE
EQUIPMENT_READ
EQUIPMENT_WRITE
REQUEST_READ
REQUEST_MANAGE
JOB_READ
JOB_DISPATCH
JOB_EXECUTE
REPORT_APPROVE
CATALOG_ADMIN
USER_ADMIN
TEMPLATE_ADMIN
AUDIT_READ
```

Do not create payment permissions in this release.

## 7.4 Security tests

- no token -> 401;
- malformed, expired, wrong issuer/audience -> 401;
- valid token without permission -> 403;
- correct permission -> success;
- tenant A token cannot read or mutate tenant B data;
- archived/deactivated account policy;
- Swagger access policy;
- CORS policy;
- method-security coverage for application entry points where required.

## Exit criteria

Every external endpoint is authenticated except deliberately public endpoints, every operation has a permission rule, and tenant identity can no longer be selected by the caller.

---

# Stage 8. Notification, document and maintenance workers

## Objective

Turn persisted queues and state machines into reliable asynchronous runtime behaviour.

## Proposed branch

```text
agent/esmpf-operational-workers
```

This stage may be split into three stacked PRs if implementation size warrants it.

## 8.1 Notification worker

Flow:

```text
claim due notifications
  -> mark sending
  -> call provider adapter
  -> mark sent
  -> on failure schedule retry
  -> after threshold dead-letter
```

Requirements:

- PostgreSQL `SKIP LOCKED` duplicate-free claim proof;
- configurable batch size and lease/timeout;
- exponential or policy-based retry;
- stuck `SENDING` recovery;
- provider message ID storage;
- idempotency strategy;
- consent/preference policy;
- adapters for configured channels only;
- secrets outside database payloads and logs.

## 8.2 Document worker

Flow:

```text
claim generation request
  -> render from immutable template/data snapshot
  -> write object storage
  -> calculate checksum
  -> register attachment
  -> mark generated
```

Requirements:

- renderer port;
- object-storage port;
- deterministic or idempotent generation policy;
- checksum and storage reconciliation;
- retry/dead-letter;
- file-size and content-type policy;
- signed or authorised download path;
- template/version traceability.

## 8.3 Maintenance worker

Flow:

```text
find due active plans
  -> calculate occurrence key
  -> create missing occurrence
  -> optionally create service request/job according to policy
```

Requirements:

- distributed lock or duplicate-safe generation;
- deterministic generation key;
- timezone-aware due calculation;
- catch-up after downtime;
- suspension/closure handling;
- meter-based and calendar-based policy tests where supported;
- tenant-by-tenant bounded processing;
- links to generated operational work.

## Internal API boundary

Workers call internal application ports directly. They do not call public REST endpoints and their state transitions are absent from public OpenAPI.

## Exit criteria

Workers are restart-safe, duplicate-safe, recoverable, observable and proven against PostgreSQL concurrency.

---

# Stage 9. Observability and production hardening

## Objective

Make the service diagnosable, deployable and recoverable under realistic failure conditions.

## Proposed branch

```text
agent/esmpf-production-hardening
```

## 9.1 Health and metrics

Expose protected or operational endpoints for:

```text
health
liveness
readiness
info
metrics
prometheus
```

Readiness must include required database connectivity and, where appropriate, mandatory external dependencies without making optional providers block all traffic.

Key metrics:

- HTTP latency/error rate by route template;
- database pool usage;
- request intake count;
- request-to-job conversion failures;
- jobs by lifecycle state;
- stale/conflict count;
- notification queue depth and oldest age;
- document queue depth and generation duration;
- maintenance generation lag;
- worker retry and dead-letter counts.

## 9.2 Structured logging

Log fields should include:

```text
timestamp
level
service
environment
correlationId
businessId
userId
operation
subjectType
subjectId
duration
result
errorCode
```

Never log:

- JWTs;
- passwords or secrets;
- provider credentials;
- raw personal documents;
- full notification bodies by default;
- stack traces in client responses.

## 9.3 Tracing

Add OpenTelemetry-compatible tracing for:

- inbound HTTP;
- application operations;
- database calls;
- worker execution;
- notification provider calls;
- object-storage calls.

Propagate correlation and trace identifiers through outbox/queue payload metadata where safe.

## 9.4 Deployment hardening

- reproducible container image;
- non-root runtime user;
- graceful shutdown;
- JVM and container resource policy;
- Kubernetes probes if deployed to Kubernetes;
- externalised configuration validation;
- secrets manager integration;
- Swagger disabled/protected in production;
- database migration on controlled startup/deployment step;
- backup and restore rehearsal;
- rollback and forward-fix migration policy.

## 9.5 Security and resilience hardening

- dependency and container vulnerability scanning;
- rate limiting for intake/public endpoints where needed;
- request body and file size limits;
- timeout and circuit-breaker policy for provider adapters;
- retry only for safe/idempotent operations;
- load tests for list/search and concurrent dispatch;
- failure tests for DB interruption, worker crash and provider timeout;
- retention and cleanup policy for audit, idempotency and queue tables.

## Exit criteria

- deployable artefact and configuration are reproducible;
- health/readiness correctly reflect service state;
- operational dashboards and alerts cover primary flows;
- backup/restore and failure recovery are demonstrated;
- no high-severity unresolved security issue remains;
- performance and concurrency targets are documented and tested.

---

# 5. Recommended stacked PR sequence

```text
main
  -> agent/esmpf-service-readiness-audit
  -> agent/esmpf-operational-flow-proofs
  -> agent/esmpf-rest-openapi-foundation
  -> agent/esmpf-intake-rest-controllers
  -> agent/esmpf-execution-rest-controllers
  -> agent/esmpf-supporting-rest-controllers
  -> agent/esmpf-security-jwt-rbac
  -> agent/esmpf-operational-workers
  -> agent/esmpf-production-hardening
```

Each branch should initially target the immediately preceding branch while stacked. After a lower PR merges, rebase or retarget the next PR to `main`.

# 6. Cross-stage Definition of Done

A stage is complete only when:

- its code and documentation agree;
- public operations have stable DTO and error contracts;
- tenant isolation is tested;
- optimistic concurrency is tested for mutable aggregates;
- principal lifecycle negative paths are tested;
- PostgreSQL-specific behaviour is proven with Testcontainers where relevant;
- Spring Modulith verification remains green;
- OpenAPI includes only intended public contracts;
- no Payment API is introduced;
- `mvn verify` is green in GitHub Actions.

# 7. Operational MVP completion verdict

The operational MVP is ready for controlled deployment when a tenant can securely perform this full scenario:

```text
create customer and location
  -> register equipment
  -> submit and triage request
  -> accept and create job
  -> dispatch and schedule visit
  -> execute work
  -> produce and approve report
  -> notify customer and produce document where configured
  -> close job
  -> observe the process through logs, metrics and audit
```

Payment processing is not part of this verdict and cannot delay it.