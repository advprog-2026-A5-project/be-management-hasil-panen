# Manajemen Hasil Panen - Architecture

## Standalone Service Boundary

This repository is a dedicated microservice for **Manajemen Hasil Panen Sawit** only.  
It does not share runtime code, database tables, or package imports with other MySawit modules.

The `reference/be-managemen-kebun` folder is read-only guidance and is not imported by this service.

## Package Structure

- `controller`: HTTP adapters (internal API currently exposed)
- `service`: use-case orchestration and business policies
- `domain`: core harvest entities and invariants
- `repository`: persistence adapters and repository interfaces
- `client`: external boundary interfaces (assignment/authorization context)
- `dto`: API error contract
- `config`: exception mapping and framework wiring
- `event`: asynchronous event integration through outbox semantics (implemented in service layer objects)

## Core Domain

- `HarvestReport`: aggregate containing submission data and approval/rejection lifecycle
- `HarvestStatus`: `PENDING`, `APPROVED`, `REJECTED`
- `OutboxEvent`: asynchronous integration event record for cross-service publishing

## Data Ownership

This service owns harvest-related data:

- harvest submission data
- approval/rejection lifecycle state
- outbox events for asynchronous integration
- transport eligibility view derived from harvest status

No direct DB access to other microservices is allowed.

