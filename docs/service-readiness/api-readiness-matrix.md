# API readiness matrix

The matrix is scoped to the operational MVP: customer management, equipment, request intake, dispatching and service execution. Payment processing is deferred and does not block operational delivery.

| Module / capability | Verdict | Controller stage | Required before publication |
|---|---|---|---|
| Customer CRUD/archive | READY_FOR_API | Stage 4 | validation, paging, error mapping, MockMvc |
| Service locations | READY_FOR_API | Stage 4 | parent-location negative tests |
| Customer interactions | NEEDS_PROOF | Stage 6 | tenant and related-subject validation proofs |
| Equipment types/job types/UOM | READY_FOR_API | Stage 6 | referenced-item archive policy |
| Checklist templates | NEEDS_PROOF | Stage 6 | published-template immutability matrix |
| Maintenance templates | NEEDS_PROOF | Stage 6 | reference compatibility and archive proofs |
| Equipment CRUD/archive | READY_FOR_API | Stage 4 | DTO/API mapping tests |
| Equipment hierarchy/relations | NEEDS_PROOF | Stage 4 or 6 | self-link, cycle and relation-overlap proofs |
| Equipment issues/readings | READY_FOR_API | Stage 4 | append-only and tenant negative tests |
| Maintenance plans | READY_FOR_API | Stage 6 | lifecycle HTTP contract |
| Manual occurrences | READY_FOR_API | Stage 6 | duplicate-generation error mapping |
| Automatic maintenance generation | BLOCKED_BY_RUNTIME_ADAPTER | Stage 8 | scheduler, locking, catch-up, timezone proof |
| Service requests | READY_FOR_API | Stage 4 | full forbidden-transition matrix |
| Request-to-job conversion | NEEDS_PROOF | Stage 4 | duplicate conversion and rollback proofs |
| Service jobs | READY_FOR_API | Stage 5 | completion rollback and prerequisite matrix |
| Visits | NEEDS_PROOF | Stage 5 | schedule policy, stale and rollback proofs |
| Executions | NEEDS_PROOF | Stage 5 | snapshot immutability and completion proofs |
| Work reports | NEEDS_PROOF | Stage 5 | approval immutability and job prerequisite proofs |
| Identity administration | READY_FOR_API | Stage 6 | API DTOs; later RBAC restrictions |
| Authentication/login | BLOCKED_BY_IMPLEMENTATION | Stage 7 | JWT issuer/resource-server design |
| Estimates as non-binding quotations | READY_FOR_API | Stage 6 | naming, rounding and duplicate-number HTTP proofs |
| Invoices/accounting records | DEFERRED | None | outside operational MVP; no controller/OpenAPI/UI |
| Payments/confirmation/refunds | DEFERRED | None | outside operational MVP; no controller/OpenAPI/UI/worker |
| Report templates | READY_FOR_API | Stage 6 | draft/published immutability proofs |
| Document metadata/status | READY_FOR_API | Stage 6 | authorization contract |
| Document generation transitions | INTERNAL_ONLY | Stage 8 | worker-only internal port; no public controller |
| Attachment metadata | NEEDS_PROOF | Stage 6 | storage/checksum/quarantine policy |
| Notification templates/enqueue | READY_FOR_API | Stage 6 | preference/consent policy |
| Notification delivery transitions | INTERNAL_ONLY | Stage 8 | dispatcher-only internal port |
| Customer feedback | READY_FOR_API | Stage 6 | publication consent tests |
| Public access tokens | NEEDS_PROOF | Stage 6 | token-hash lookup and authorization design |
| Data jobs/outbox/idempotency/sequences | INTERNAL_ONLY | Stage 8/platform | do not expose generic CRUD |
| Audit query | READY_FOR_API | Stage 6 | RBAC, filtering and retention |
| Integration connections | NEEDS_PROOF | Stage 6/admin | secret-reference redaction and health checks |
| Mobile sync | NEEDS_PROOF | Stage 6 | idempotency, conflict and replay matrix |

## Primary controller delivery path

```text
Stage 4:
Customer -> ServiceLocation -> Equipment -> ServiceRequest -> convert-to-job

Stage 5:
ServiceJob -> JobVisit -> JobExecution -> WorkReport -> complete/close
```

## Controller admission rule

An operation can enter a controller PR only when:

- its application contract is stable;
- tenant ownership is derived from trusted context;
- the main positive and negative paths are proven;
- mutations enforce optimistic version where applicable;
- no known concurrency race remains in the in-scope operational flow;
- failures can be mapped to a stable global API error;
- the operation is not worker/internal infrastructure;
- the operation is not classified `DEFERRED`.

## Explicit Payment API exclusion

The current release must not define:

```text
/api/v1/payments
/api/v1/invoices
payment confirmation or refund operations
payment-provider callback endpoints
payment OpenAPI schemas
payment RBAC permissions
```

Existing payment-domain code is dormant and does not affect the readiness verdict of the operational MVP.