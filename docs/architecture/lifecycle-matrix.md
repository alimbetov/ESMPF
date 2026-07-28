# ESMPF lifecycle matrix

| Aggregate | Initial state | Allowed transitions | Terminal / prohibited transitions |
|---|---|---|---|
| Business | ACTIVE | ACTIVE → SUSPENDED; SUSPENDED → ACTIVE | repeated same-state transition rejected |
| BusinessLocation | ACTIVE | ACTIVE → INACTIVE; INACTIVE → ACTIVE | repeated same-state transition rejected |
| UserAccount | ACTIVE | ACTIVE → INACTIVE; INACTIVE → ACTIVE | repeated same-state transition rejected |
| WorkerQualification | ACTIVE | ACTIVE → EXPIRED | EXPIRED → EXPIRED rejected |
| ChecklistTemplate | DRAFT | DRAFT → PUBLISHED; DRAFT/PUBLISHED → ARCHIVED | ARCHIVED terminal |
| MaintenancePlan | DRAFT | DRAFT → ACTIVE; ACTIVE → SUSPENDED; supported state → CLOSED | CLOSED terminal |
| ServiceRequest | DRAFT/NEW | explicit triage, accept, reject, cancel and convert transitions | generic status update prohibited |
| ServiceJob | created | explicit ready, schedule, start, wait, complete, close and cancel transitions | closed/cancelled terminal |
| ServiceAgreement | DRAFT | DRAFT → ACTIVE; ACTIVE → SUSPENDED; supported state → CLOSED | CLOSED terminal |
| WarrantyCase | OPEN | OPEN → APPROVED/REJECTED; APPROVED/REJECTED → CLOSED | decision required before close |
| MobileDevice | ACTIVE | ACTIVE → REVOKED | REVOKED cannot be reactivated; new registration required |
| Estimate | DRAFT | DRAFT → SENT; SENT → APPROVED/REJECTED | approved/rejected terminal for MVP |
| ReportTemplate | DRAFT | DRAFT → PUBLISHED; non-archived → ARCHIVED | ARCHIVED terminal |
| GeneratedDocument | REQUESTED | REQUESTED/FAILED → GENERATING; GENERATING → GENERATED/FAILED; GENERATED → DELIVERED/SUPERSEDED | worker transitions internal |
| Attachment | ACTIVE transitional metadata | ACTIVE → QUARANTINED/ARCHIVED | storage lifecycle replaced in PR #19/#20 |
| Notification | QUEUED | QUEUED → SENDING; SENDING → SENT/FAILED | worker transitions internal |
| DataJob | QUEUED | QUEUED → RUNNING; RUNNING → COMPLETED/FAILED | worker transitions internal |
| OutboxEvent | PENDING | PENDING/FAILED → PUBLISHING; PUBLISHING → PUBLISHED/FAILED | internal only |
| IdempotencyRecord | STARTED | STARTED → COMPLETED/FAILED | internal only |
| IntegrationConnection | INACTIVE | INACTIVE/SUSPENDED → ACTIVE; ACTIVE → SUSPENDED | health callbacks do not change business lifecycle |

## Enforcement

- Every transition checks optimistic-lock `version`.
- Repeated or impossible transitions return a lifecycle conflict.
- Controllers do not expose generic status fields for mutation.
- Negative transition tests are mandatory for mutable lifecycle aggregates.
