# Manajemen Hasil Panen - API

## Base Path

Compatible routes are exposed at both:
- `/...` (legacy)
- `/api/hasil-panen/...` (recommended)

## Endpoints

### Create harvest report (Buruh)
- `POST /harvests`
- Supports:
  - `application/json` body with `kilogram`, `reportText`, `photos` (URL list)
  - `multipart/form-data` with `kilogram`, `reportText`, and `photos[]` files
- Returns `201 Created`
- Enforces one report per Buruh per day

### Buruh own history
- `GET /harvests/me?startDate=&endDate=&status=`

### Harvest detail
- `GET /harvests/{harvestId}`

### Mandor harvest history
- `GET /mandor/harvests?harvestDate=&buruhName=`

### Mandor specific Buruh history
- `GET /mandor/buruh/{buruhId}/harvests?harvestDate=`

### Approve / reject
- `POST /harvests/{harvestId}/approve`
- `POST /harvests/{harvestId}/reject`

### Shipment eligibility list
- `GET /harvest-reports/eligible-for-shipment`

### Internal transport eligibility by report
- `GET /internal/harvests/{harvestId}/transport-eligibility`

## Status model
- `PENDING`
- `APPROVED`
- `REJECTED`

## Error contract

```json
{
  "timestamp": "...",
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "...",
  "path": "/harvests"
}
```
