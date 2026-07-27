# API readiness matrix

## Product scope for the current release

The current ESMPF release is a service-operations platform focused on:

- customer management;
- equipment and service-location management;
- intake and triage of service requests;
- dispatching and execution of service jobs;
- maintenance planning;
- work reports, recommendations and customer communication.

Online payment processing, payment-provider integration, payment confirmation and refunds are explicitly out of scope. Existing payment-domain code is retained as a deferred capability and must not drive the readiness verdict for the operational MVP.

| Module / capability | Verdict | Controller stage | Required before publication |
|---|---|---|---|
| Customer CRUD/archive | READY_FOR_API | Core | validation, paging, error mapping, MockMvc |
| Service locations | READY_FOR_API | Core | parent-location negative tests |
| Customer interactions | NEEDS_PROOF | Core/supporting | tenant and related-subject validation proofs |
| Equipment types/job types/UOM | READY_FOR_API | Core | referenced-item archive policy |
| Checklist templates | NEEDS_PROOF | Core | published-template immutability matrix |
| Maintenance templates | NEEDS_PROOF | Core | reference compatibility and archive proofs |
| Equipment CRUD/archive | READY_FOR_API | Core | DTO/API mapping tests |
| Equipment hierarchy/relations | NEEDS_PROOF | Core | self-link, cycle and relation-overlap proofs |
| Equipment issues/readings | READY_FOR_API | Core | append-only and tenant negative tests |
| Maintenance plans | READY_FOR_API | Core | lifecycle HTTP contract |
| Manual occurrences | READY_FOR_API | Core | duplicate-generation error mapping |
| Automatic maintenance generation | BLOCKED_BY_RUNTIME_ADAPTER | Workers | scheduler, locking, catch-up, timezone proof |
| Service requests | READY_FOR_API | Core | full forbidden-transition matrix |
| Service jobs | READY_FOR_API | Core | completion rollback and prerequisite matrix |
| Visits/executions/reports | NEEDS_PROOF | Core | schedule conflict and completion rollback proofs |
| Identity administration | READY_FOR_API | Supporting | API DTOs; later RBAC restrictions |
| Authentication/login | BLOCKED_BY_IMPLEMENTATION | Security | JWT issuer/resource-server design |
| Estimates/quotations | READY_FOR_API | Supporting | rounding, revision and duplicate-number HTTP proofs |
| Invoices/accounting records | DEFERRED | Deferred commercial | not required for the operational MVP; no payment semantics |
| Payments, confirmation and refunds | DEFERRED | No controller | excluded from the current product scope and from MVP readiness |
| Report templates | READY_FOR_API | Supporting | draft/published immutability proofs |
| Document metadata/status | READY_FOR_API | Supporting | authorization contract |
| Document generation transitions | INTERNAL_ONLY | Workers | no public CRUD/controller |
| Attachment metadata | NEEDS_PROOF | Supporting | storage/checksum/quarantine policy |
| Notification templates/enqueue | READY_FOR_API | Supporting | preference/consent policy |
| Notification delivery transitions | INTERNAL_ONLY | Workers | dispatcher-only contract |
| Customer feedback | READY_FOR_API | Supporting | publication consent tests |
| Public access tokens | NEEDS_PROOF | Supporting/public | token-hash lookup and authorization design |
| Data jobs/outbox/idempotency/sequences | INTERNAL_ONLY | Workers/platform | do not expose generic CRUD |
| Audit query | READY_FOR_API | Supporting/admin | RBAC, filtering and retention |
| Integration connections | NEEDS_PROOF | Supporting/admin | secret-reference redaction and health checks |
| Mobile sync | NEEDS_PROOF | Supporting | idempotency, conflict and replay matrix |

## Commercial boundary for the MVP

The only commercial capability considered for the current API is a non-binding estimate/quotation used during request handling. It may describe proposed work and expected cost, but it must not:

- initiate a financial transaction;
- claim that money has been received;
- confirm settlement;
- refund funds;
- store payment-provider credentials;
- expose provider callback endpoints.

Any future payment capability requires a separate product decision, threat model, accounting model, audit requirements and dedicated implementation stage.

## Controller admission rule

An operation can enter a controller PR only when:

- its application contract is stable;
- tenant ownership is derived from trusted context;
- the main positive and negative paths are proven;
- mutations enforce optimistic version where applicable;
- no known concurrency race remains within the selected product scope;
- failures can be mapped to a stable global API error;
- the operation is not worker/internal infrastructure;
- the operation is not explicitly deferred from the current release.