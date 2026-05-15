# Manajemen Hasil Panen - API

## Implemented Endpoint

### Internal Transport Eligibility

`GET /internal/harvests/{harvestId}/transport-eligibility`

Response shape:

```json
{
  "harvestId": "uuid",
  "eligible": true,
  "status": "APPROVED",
  "kilogram": 125.5
}
```

Rules:

- `APPROVED` -> `eligible=true`
- `PENDING` or `REJECTED` -> `eligible=false`
- unknown harvest -> `404 Not Found`

## Error Contract

Global error handling returns:

- `400 Bad Request`
- `403 Forbidden`
- `404 Not Found`
- `409 Conflict`
- `500 Internal Server Error`

Error body contains:

- `timestamp`
- `status`
- `error`
- `message`
- `path`

## Planned Public Endpoints (Contract Target)

The following are documented as target contract for next increments:

- `POST /harvests`
- `GET /harvests/me`
- `GET /mandor/harvests`
- `GET /mandor/buruh/{buruhId}/harvests`
- `POST /harvests/{harvestId}/approve`
- `POST /harvests/{harvestId}/reject`

