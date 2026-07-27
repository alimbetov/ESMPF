# Lifecycle readiness matrix

## Service request

| Command | Expected source states | Target state | Audit verdict |
|---|---|---|---|
| triage | NEW | TRIAGED | READY_FOR_API |
| accept | TRIAGED | ACCEPTED | READY_FOR_API |
| reject | NEW, TRIAGED, ACCEPTED as explicitly implemented | REJECTED | NEEDS_PROOF for full forbidden-state matrix |
| cancel | non-terminal states as explicitly implemented | CANCELLED | NEEDS_PROOF for full forbidden-state matrix |
| convert to job | ACCEPTED | CONVERTED + new job | NEEDS_PROOF for rollback and duplicate conversion |

## Service job

| Command | Expected source states | Target state | Audit verdict |
|---|---|---|---|
| mark ready | DRAFT | READY | READY_FOR_API |
| schedule | READY | SCHEDULED | NEEDS_PROOF for schedule conflicts |
| start | READY/SCHEDULED as implemented | IN_PROGRESS | READY_FOR_API |
| hold | IN_PROGRESS | WAITING | READY_FOR_API |
| resume | WAITING | IN_PROGRESS | READY_FOR_API |
| complete | IN_PROGRESS | COMPLETED | NEEDS_PROOF for execution/report prerequisites and rollback |
| close | COMPLETED | CLOSED | NEEDS_PROOF |
| cancel | allowed non-terminal states | CANCELLED | NEEDS_PROOF for compensation rules |

## Job visit

| Command | Expected source states | Target state | Audit verdict |
|---|---|---|---|
| plan | valid job state | PLANNED | NEEDS_PROOF for overlap policy |
| start | PLANNED | IN_PROGRESS | READY_FOR_API |
| complete | IN_PROGRESS | COMPLETED | NEEDS_PROOF for job aggregation and rollback |
| cancel | PLANNED/IN_PROGRESS as implemented | CANCELLED | NEEDS_PROOF |

## Maintenance plan

| Command | Expected source states | Target state | Audit verdict |
|---|---|---|---|
| activate | DRAFT/SUSPENDED as implemented | ACTIVE | READY_FOR_API |
| suspend | ACTIVE | SUSPENDED | READY_FOR_API |
| close | allowed non-terminal states | CLOSED | NEEDS_PROOF for outstanding occurrences |

## Maintenance occurrence

| Command | Expected source states | Target state | Audit verdict |
|---|---|---|---|
| generate | active plan + unique generation key | pending state | READY_FOR_API for manual command; worker-bound for automation |
| link service job | unlinked occurrence | linked state | NEEDS_PROOF for rollback and duplicate link |
| complete | valid linked/due state | COMPLETED | NEEDS_PROOF |
| cancel | non-terminal state | CANCELLED | NEEDS_PROOF |

## Estimate / invoice / payment

| Aggregate | Transition set | Audit verdict |
|---|---|---|
| Estimate | draft → sent → approved/rejected | READY_FOR_API with remaining forbidden-state proofs |
| Invoice | draft → issued → overdue/void | NEEDS_PROOF for currency, document and payment interactions |
| Payment | registered → confirmed/failed/refunded | NEEDS_REPAIR because invoice balance update must be atomic |

## Templates

Checklist, maintenance, report and notification templates all require explicit draft/published-or-active/archive transitions. Updates after publication/activation must be rejected and proven.

## Worker-managed lifecycles

The following state machines are valid application contracts but must remain internal until workers exist:

- generated document generation lifecycle;
- notification delivery lifecycle;
- outbox publication lifecycle;
- data-job execution lifecycle;
- integration health update lifecycle.
