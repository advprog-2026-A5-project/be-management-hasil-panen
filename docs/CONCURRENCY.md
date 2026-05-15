# Manajemen Hasil Panen - Concurrency Strategy

## Daily Submission Uniqueness

Rule: one harvest report per `buruh_id` per calendar day.

Enforced with two layers:

1. service-level duplicate check (`DuplicateHarvestSubmissionException`)
2. database unique constraint on `(buruh_id, harvest_date)`

Outcome under concurrent requests: at most one create succeeds, others fail with conflict.

## Terminal Status Safety

Harvest lifecycle is single-terminal:

- `PENDING -> APPROVED`
- `PENDING -> REJECTED`
- `APPROVED` and `REJECTED` are terminal

`HarvestTransitionGuard` and approval/rejection services prevent double transition during races.

## Approval Event Idempotency

Payroll integration uses outbox event creation tied to approval transition.

- only successful `PENDING -> APPROVED` transition creates payroll event
- concurrent duplicate approval attempts are rejected by terminal-state checks
- outbox publisher sends pending events asynchronously and tracks retries/sent status

## Why Async Outbox

Approval transaction does not perform synchronous payment calls.  
This avoids distributed transaction coupling and keeps approval latency bounded.

