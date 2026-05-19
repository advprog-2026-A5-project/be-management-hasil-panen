# Manajemen Hasil Panen - API

## Implemented Endpoints

### Submit Harvest (Buruh)

`POST /harvests`

Body:

```json
{
  "kilogram": 100,
  "reportText": "Panen blok timur",
  "photos": ["proof-1.jpg"]
}
```

Behavior:

- authenticated identity is resolved from Auth (`/api/users/me`)
- caller must have role `BURUH`
- Buruh can only submit for self (no buruhId in request body)
- duplicate same-day submission is rejected
- snapshot fields persisted: `buruhId`, `mandorId`, `kebunCode`, `kebunId` (if available)
- default status: `PENDING`

### My Harvest History (Buruh)

`GET /harvests/me?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD&status=PENDING|APPROVED|REJECTED`

Behavior:

- caller must be `BURUH`
- returns only own harvest history

### Mandor Harvest List

`GET /mandor/harvests?harvestDate=YYYY-MM-DD&buruhName=...`

Behavior:

- caller must be `MANDOR`
- returns harvests under assigned buruhs

### Approve Harvest (Mandor)

`POST /harvests/{harvestId}/approve`

Behavior:

- caller must be `MANDOR`
- authorization is validated using Auth assignment + Kebun context checks in service layer

### Reject Harvest (Mandor)

`POST /harvests/{harvestId}/reject`

Body:

```json
{
  "reason": "Foto bukti tidak valid"
}
```

Behavior:

- caller must be `MANDOR`
- rejection reason is required
- authorization is validated using Auth assignment + Kebun context checks in service layer

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
- `401 Unauthorized`
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

## Service Dependencies

- `auth.service.base-url` (default `http://localhost:8080`)
- `kebun.service.base-url` (default `http://localhost:8081`)
- `server.port` (default `8082`)

