# AWS Deploy Handoff (No Credentials in Repo)

This service is deployment-ready but intentionally does NOT include account credentials, tokens, or auto-apply steps.

## Prerequisites
- AWS account access with IAM permissions for VPC, ECS, ECR, RDS, CloudWatch, ALB, Security Groups
- Terraform >= 1.6
- AWS CLI configured (`aws configure`) by deployment owner
- Docker image already buildable from this repository

## Recommended Deployment Steps
1. Configure terraform variables (create `terraform.tfvars`, do not commit it):
   - `db_password`
   - optional region/subnet overrides
2. Initialize/apply base infrastructure:
   - `terraform -chdir=infrastructure/terraform init`
   - `terraform -chdir=infrastructure/terraform plan`
   - `terraform -chdir=infrastructure/terraform apply`
3. Build and push image to ECR using output `ecr_repository_url`.
4. Create ECS task definition + service wiring (ALB/target group/listener) using your team’s IAM and networking standards.
5. Set runtime environment variables in ECS task:
   - `SPRING_DATASOURCE_URL=jdbc:postgresql://<rds_endpoint>:5432/hasilpanen_db`
   - `SPRING_DATASOURCE_USERNAME=hasilpanen_user`
   - `SPRING_DATASOURCE_PASSWORD=<secure-from-secrets-manager>`
   - `SPRING_FLYWAY_ENABLED=true`
6. Enable CloudWatch logging in task definition.
7. Configure health check path `/health` in ALB target group.

## Migration Strategy
- Flyway migrations run at app startup.
- Run one task first as canary; once healthy, scale desired count.

## Security Notes
- Never commit `terraform.tfvars` with secrets.
- Prefer AWS Secrets Manager / SSM Parameter Store for DB passwords.
- Restrict SG ingress to ALB only for app, and app SG only for RDS 5432.

## Ownership Boundary
This repository includes deploy scaffolding only; final account-specific deploy execution is expected to be done by the infrastructure/deployment owner.
