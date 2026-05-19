# be-management-hasil-panen

## Local Run

- Default port: `8082`
- Override port: `SERVER_PORT`
- Default DB (local): H2 in-memory
- External dependencies:
  - Auth service base URL: `AUTH_SERVICE_BASE_URL` (default `http://localhost:8080`)
  - Kebun service base URL: `KEBUN_SERVICE_BASE_URL` (default `http://localhost:8081`)

Run:

```powershell
./gradlew bootRun
```

## Public API (Current)

- `POST /harvests`
- `GET /harvests/me`
- `GET /mandor/harvests`
- `POST /harvests/{harvestId}/approve`
- `POST /harvests/{harvestId}/reject`
- `GET /internal/harvests/{harvestId}/transport-eligibility`

## Status Rules

- `PENDING` after submission
- `APPROVED` when validated by authorized mandor
- `REJECTED` when rejected with reason
- Transport eligibility:
  - `APPROVED` => `eligible=true`
  - `PENDING`/`REJECTED` => `eligible=false`

