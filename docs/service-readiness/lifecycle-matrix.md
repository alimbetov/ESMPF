# Lifecycle readiness matrix

This matrix prioritises the operational MVP path and identifies the lifecycle proofs required before REST publication.

## Service Request

Expected supported transitions:

```text
NEW -> TRIAGED
NEW -> CANCELLED
TRIAGED -> ACCEPTED
TRIAGED -> REJECTED
TRIAGED -> CANCELLED
ACCEPTED -> CONVERTED
```

Required proofs:

- every supported transition;
- rejected, cancelled and converted states reject incompatible commands;
- stale version is rejected;
- duplicate conversion is prevented;
- conversion transaction rolls back completely when job creation fails;
- related customer, location and equipment belong to the active tenant.

Verdict: `NEEDS_PROOF` before Stage 4 publication.

## Service Job

Expected supported transitions:

```text
DRAFT -> READY
READY -> SCHEDULED
SCHEDULED -> IN_PROGRESS
IN_PROGRESS -> WAITING
WAITING -> IN_PROGRESS
IN_PROGRESS -> COMPLETED
COMPLETED -> CLOSED
eligible non-final states -> CANCELLED
```

Required proofs:

- readiness prerequisites;
- schedule command consistency;
- start policy;
- hold/resume reason handling;
- completion prerequisites;
- close policy;
- cancellation policy;
- immutable final states;
- stale version and rollback behaviour.

Verdict: implementation exists; `NEEDS_PROOF` for full Stage 5 publication.

## Job Visit

Expected transitions:

```text
PLANNED -> IN_PROGRESS
IN_PROGRESS -> COMPLETED
PLANNED/IN_PROGRESS -> CANCELLED where policy permits
```

Required proofs:

- planned time validation;
- schedule-overlap policy;
- double start/completion behaviour;
- job-state compatibility;
- tenant consistency;
- stale version;
- rollback when dependent updates fail.

Verdict: `NEEDS_PROOF`.

## Job Execution

Expected transitions:

```text
STARTED -> COMPLETED
```

Required proofs:

- eligible job state;
- immutable checklist/template snapshot;
- required answers where model supports them;
- no duplicate completion;
- stale version;
- cross-tenant reference rejection;
- rollback.

Verdict: `NEEDS_PROOF`.

## Work Report

Expected transitions:

```text
DRAFT/CREATED -> APPROVED
```

Required proofs:

- job ownership/state;
- active/final report cardinality policy;
- approved report immutability;
- job completion prerequisite;
- stale approval;
- transaction rollback.

Verdict: `NEEDS_PROOF`.

## Maintenance Plan

Expected transitions:

```text
DRAFT -> ACTIVE
ACTIVE -> SUSPENDED
SUSPENDED -> ACTIVE
DRAFT/ACTIVE/SUSPENDED -> CLOSED where policy permits
```

Manual administration may be exposed in Stage 6 after HTTP contract tests. Automatic due processing remains Stage 8 worker functionality.

Verdict: administration `READY_FOR_API`; automation `BLOCKED_BY_RUNTIME_ADAPTER`.

## Maintenance Occurrence

Expected transitions include job linking, completion and cancellation according to the current model.

Required proofs:

- deterministic generation-key deduplication;
- link only compatible same-tenant job;
- no duplicate or conflicting job link;
- completion/cancellation policy;
- stale version;
- worker generation safety.

Verdict: manual operations `NEEDS_PROOF`; automatic generation `BLOCKED_BY_RUNTIME_ADAPTER`.

## Templates

Checklist, maintenance, report and notification templates use draft/published or draft/active lifecycles.

Required invariant:

```text
published or active revision cannot be changed by a draft-update command
```

Verdict: `NEEDS_PROOF` before affected Stage 6 controllers.

## Notifications

Template administration and notification enqueue are candidate supporting endpoints.

Delivery transitions:

```text
QUEUED -> SENDING -> SENT
SENDING -> FAILED/QUEUED according to retry policy
terminal retry exhaustion -> DEAD_LETTER or terminal failure
```

Delivery transitions are `INTERNAL_ONLY` and belong to the Stage 8 worker.

## Generated Documents

Public/supporting operations may expose generation request and read-only status/metadata.

Runtime transitions such as start, complete and fail generation are `INTERNAL_ONLY` and belong to Stage 8.

## Commercial scope

Estimate lifecycle may support non-binding quotations in Stage 6 after remaining state proofs.

Invoice and payment lifecycles are `DEFERRED`. Payment registration, confirmation, failure and refund transitions are excluded from:

- operational proofs;
- REST/OpenAPI;
- RBAC permissions;
- UI routes;
- workers;
- operational MVP readiness criteria.