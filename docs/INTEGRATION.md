# Manajemen Hasil Panen - Integration

## Cross-Service Communication

This service uses asynchronous and interface-based boundaries:

- `MandorBuruhClient`
- `UserAssignmentClient`
- `KebunClient`

These are abstractions; no direct dependency on other service databases.

## Payroll Integration

On approval, payroll trigger is emitted as outbox event.

Flow:

1. harvest approved in local transaction
2. outbox event persisted as `PENDING`
3. publisher sends event to transport adapter
4. status updated to `SENT` or `FAILED` with retry count

This enforces eventual consistency and decouples approval from payment service availability.

## Shipment Integration

Shipment side should consume internal eligibility API:

- `GET /internal/harvests/{harvestId}/transport-eligibility`

Contract:

- only `APPROVED` harvests are transport-eligible
- `REJECTED` harvests must not be shipped

## Reference Folder Confirmation

`reference/be-managemen-kebun` is used only for reading patterns and was not imported into runtime code.

