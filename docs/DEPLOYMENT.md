# Manajemen Hasil Panen - Deployment

## Verify locally

```bash
./gradlew clean test
./gradlew clean build
```

## Docker

```bash
docker build -t mysawit-hasil-panen .
```

## Runtime environment variables

### Core
- `PORT`
- `SPRING_PROFILES_ACTIVE` (`prod` for deployment)
- `CORS_ALLOWED_ORIGINS`

### Database
- `DATABASE_URL` or `SPRING_DATASOURCE_URL`
- `DB_USERNAME` / `SPRING_DATASOURCE_USERNAME`
- `DB_PASSWORD` / `SPRING_DATASOURCE_PASSWORD`

### External service URLs
- `AUTH_SERVICE_BASE_URL`
- `AUTH_INTERNAL_SERVICE_TOKEN`
- `KEBUN_SERVICE_BASE_URL`
- `PAYMENT_SERVICE_BASE_URL`
- `PAYROLL_ENDPOINT`

### Storage
- `STORAGE_PROVIDER` (`local` or `cloudinary`)
- `LOCAL_STORAGE_ROOT`
- `MAX_UPLOAD_SIZE_MB`
- `CLOUDINARY_CLOUD_NAME`
- `CLOUDINARY_API_KEY`
- `CLOUDINARY_API_SECRET`

## Notes
- Flyway migrations run on startup.
- Approval emits outbox payroll events and publisher dispatches asynchronously.
- Use `storage.provider=local` for local dev/test when Cloudinary secrets are unavailable.
