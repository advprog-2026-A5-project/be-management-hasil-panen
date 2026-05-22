# be-management-hasil-panen

## Local Run

```powershell
./gradlew clean test
./gradlew bootRun
```

Default port: `8082` (set `PORT` to override).

## API Paths

All public routes are available on both:
- legacy root: `/...`
- recommended module prefix: `/api/hasil-panen/...`

## Key Endpoints

- `POST /harvests` (JSON or multipart)
- `GET /harvests/{harvestId}`
- `GET /harvests/me`
- `GET /mandor/harvests`
- `GET /mandor/buruh/{buruhId}/harvests`
- `POST /harvests/{harvestId}/approve`
- `POST /harvests/{harvestId}/reject`
- `GET /harvest-reports/eligible-for-shipment`
- `GET /internal/harvests/{harvestId}/transport-eligibility`

## Core Configuration

- `AUTH_SERVICE_BASE_URL`
- `KEBUN_SERVICE_BASE_URL`
- `PAYMENT_SERVICE_BASE_URL`
- `PAYROLL_ENDPOINT`
- `CORS_ALLOWED_ORIGINS`

Storage:
- `STORAGE_PROVIDER` (`local` or `cloudinary`)
- `LOCAL_STORAGE_ROOT`
- `MAX_UPLOAD_SIZE_MB`
- `CLOUDINARY_CLOUD_NAME`
- `CLOUDINARY_API_KEY`
- `CLOUDINARY_API_SECRET`
