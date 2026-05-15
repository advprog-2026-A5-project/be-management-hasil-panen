# Manajemen Hasil Panen - Deployment

## Local Run

1. configure environment variables (see `.env.example` if present)
2. run tests:

```bash
./gradlew clean test
```

3. run application:

```bash
./gradlew bootRun
```

## Docker

Build image:

```bash
docker build -t hasil-panen-service:local .
```

Run stack:

```bash
docker compose up -d
```

The compose setup is intended for local service + PostgreSQL development.

## AWS Readiness Handoff

IaC scaffolding is prepared under:

- `infrastructure/terraform`
- `infrastructure/aws`

Expected target architecture:

- ECR image repository
- ECS service
- RDS PostgreSQL
- ALB + security groups
- CloudWatch logs

Apply/deploy is intentionally left to the infra owner team with appropriate AWS credentials.

## Migration Strategy

Schema migration SQL is located at:

- `src/main/resources/db/migration/V1__create_harvest_schema.sql`

Deploy pipelines should execute migration before routing production traffic.

