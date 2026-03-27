# MyBookList

This repository includes GitHub Actions CI/CD to deploy the Spring Boot backend to Fly.io.

## What was added

- `Dockerfile` for backend container image build
- `.dockerignore` to reduce Docker build context
- `fly.toml` Fly.io app configuration
- `.github/workflows/ci.yml` backend test pipeline
- `.github/workflows/deploy-fly.yml` deploy pipeline for `main`

## Required GitHub secrets

Set these in **GitHub -> Settings -> Secrets and variables -> Actions**:

- `FLY_API_TOKEN` (from `flyctl auth token`)
- `FLY_APP_NAME` (your Fly app name)

## Required Fly runtime secrets

Your Spring app needs these environment variables in Fly:

- `SPRING_DATASOURCE_URL` **or** `DATABASE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET_KEY`
- `SUPPORT_EMAIL`
- `APP_PASSWORD`

### Database URL format

The app now accepts either:

- a JDBC URL, such as `jdbc:postgresql://host:5432/db?sslmode=require&prepareThreshold=0`
- a Fly/common Postgres URL, such as `postgres://user:password@host:5432/db?sslmode=require`

If you use `DATABASE_URL` or a non-JDBC `SPRING_DATASOURCE_URL`, the app converts it to JDBC at startup automatically.

## One-time Fly setup (PowerShell)

```powershell
flyctl auth login
flyctl apps create <your-fly-app-name>
flyctl secrets set DATABASE_URL="postgres://<db-user>:<db-password>@<db-host>:5432/<db-name>?sslmode=require" --app <your-fly-app-name>
flyctl secrets set JWT_SECRET_KEY="<jwt-secret>" --app <your-fly-app-name>
flyctl secrets set SUPPORT_EMAIL="<support-email>" --app <your-fly-app-name>
flyctl secrets set APP_PASSWORD="<app-password>" --app <your-fly-app-name>
```

## CI/CD behavior

- Pull requests and pushes run backend tests (`ci.yml`).
- Pushes to `main` run tests, then deploy to Fly (`deploy-fly.yml`).
- You can also run deployment manually with **workflow_dispatch**.

