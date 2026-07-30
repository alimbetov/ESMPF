# PR #26 — Maintenance full lifecycle

This change closes the Foundation 1.0 maintenance chain:

Maintenance Template → Plan → Activate → Occurrence → ServiceJob → Visit → Checklist → Work report → Closed job → Completed occurrence.

Key invariants:

- occurrence generation is limited to ACTIVE plans;
- due dates must remain inside the plan activity window;
- a linked ServiceJob must reference the same occurrence;
- ServiceJob equipment and job type must match the plan and maintenance template;
- an occurrence cannot complete until its ServiceJob is CLOSED;
- duplicate generation keys remain rejected.
